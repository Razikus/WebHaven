package tech.razikus.headlesshaven;

import haven.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMapCache {
    public static final Coord cmaps = new Coord(100, 100);  // Size of each grid
    private final Map<Coord, GridData> grids = new ConcurrentHashMap<>();
    private final Map<Integer, Defrag> fragbufs = new TreeMap<>();
    private final ResourceManager resourceManager;
    private PlayerHandler handler;
    private Map<Integer, NameVersion> tileResources = new HashMap<>();


    class Defrag {
        private final byte[] buf;
        private int off = 0;
        private final int len;
        public long last = System.currentTimeMillis();

        public Defrag(int len) {
            this.len = len;
            this.buf = new byte[len];
        }

        public void add(byte[] src, int off) {
            System.arraycopy(src, 0, buf, off, src.length);
            this.off += src.length;
        }

        public boolean done() {
            return(off >= len);
        }

        public Message msg() {
            if(!done())
                return(null);
            return(new MessageBuf(buf));
        }
    }

    public class GridData {
        public final int[] tiles;      // Tile IDs
        public final Coord coord;      // Grid coordinates
        public long id;
        public final float z[] = new float[cmaps.x * cmaps.y];

        public GridData(Coord coord) {
            this.coord = coord;
            this.tiles = new int[cmaps.x * cmaps.y];
        }

        public void fill(Message msg) {
            int ver = msg.uint8();
            if (ver == 1) {
                subfill(msg);
            } else {
                System.out.println("Unknown map version: " + ver);
            }
        }

        private void filltiles(Message buf) {
            while(true) {
                int tileid = buf.uint8();
                if(tileid == 255)
                    break;
                String resnm = buf.string();
                int resver = buf.uint16();
                resourceManager.loadResource(new NameVersion(resnm, resver));
                tileResources.put(tileid, new NameVersion(resnm, resver));
            }
            for(int i = 0; i < tiles.length; i++) {
                tiles[i] = buf.uint8();
            }
        }

        private void filltiles2(Message buf) {
            int[] tileids = new int[1];
            int maxid = 0;
            while(true) {
                int encid = buf.uint16();
                if(encid == 65535)
                    break;
                maxid = Math.max(maxid, encid);
                int tileid = buf.uint16();
                if(encid >= tileids.length)
                    tileids = Utils.extend(tileids, Integer.highestOneBit(encid) * 2);
                tileids[encid] = tileid;
                String resnm = buf.string();
                int resver = buf.uint16();
                resourceManager.loadResource(new NameVersion(resnm, resver));
                tileResources.put(tileid, new NameVersion(resnm, resver));
            }
            boolean lg = maxid >= 256;
            for(int i = 0; i < tiles.length; i++) {
                tiles[i] = tileids[lg ? buf.uint16() : buf.uint8()];
            }
        }

        private void fillz(Message buf) {
            int fmt = buf.uint8();
            if(fmt == 0) {
                float z = buf.float32() * 11;
                for(int i = 0; i < this.z.length; i++)
                    this.z[i] = z;
            } else if(fmt == 1) {
                float min = buf.float32() * 11, q = buf.float32() * 11;
                for(int i = 0; i < z.length; i++)
                    z[i] = min + (buf.uint8() * q);
            } else if(fmt == 2) {
                float min = buf.float32() * 11, q = buf.float32() * 11;
                for(int i = 0; i < z.length; i++)
                    z[i] = min + (buf.uint16() * q);
            } else if(fmt == 3) {
                for(int i = 0; i < z.length; i++)
                    z[i] = buf.float32() * 11;
            } else {
                throw(new Message.FormatError(String.format("Unknown z-map format: %d", fmt)));
            }
        }


        private void decplots(Message buf) {
        }
        private void fillplots(Message buf) {
        }

        public void subfill(Message msg) {
            while(!msg.eom()) {
                String layer = msg.string();
                int len = msg.uint8();
                if((len & 0x80) != 0)
                    len = msg.int32();
                Message buf = new LimitMessage(msg, len);
                switch(layer) {
                    case "z":
                        subfill(new ZMessage(buf));
                        break;
                    case "m":
                        this.id = buf.int64();
                        break;
                    case "t":
                        filltiles(buf);
                        break;
                    case "t2":
                        filltiles2(buf);
                        break;
                    case "h":
                        fillz(buf);
                        break;
                    case "pi":
                        decplots(buf);
                        break;
                    case "p":
                        fillplots(buf);
                        break;
                }
                buf.skip();
            }
        }
    }

    public SimpleMapCache(ResourceManager resourceManager, PlayerHandler playerHandler) {
        this.resourceManager = resourceManager;
        this.handler = playerHandler;
    }

    public void reqArea(Coord ul, Coord br) {
        for(int y = ul.y; y <= br.y; y++) {
            for(int x = ul.x; x <= br.x; x++) {
                this.handler.requestMap(new Coord(x, y));
            }
        }
    }
    public void reqAreaAround(Coord2d pos, int radius) {
        Coord gc = new Coord((int)(pos.x / (11.0 * 100.0)),
                (int)(pos.y / (11.0 * 100.0)));
        reqArea(gc.sub(radius, radius),
                gc.add(radius, radius));
    }

    public void mapdata(Message msg) {
        long now = System.currentTimeMillis();
        int pktid = msg.int32();
        int off = msg.uint16();
        int len = msg.uint16();


        synchronized(fragbufs) {
            Defrag fragbuf = fragbufs.get(pktid);
            if(fragbuf == null) {
                fragbuf = new Defrag(len);
                fragbufs.put(pktid, fragbuf);
            }
            fragbuf.add(msg.bytes(), off);
            fragbuf.last = now;

            if(fragbuf.done()) {
                mapdata2(fragbuf.msg());
                fragbufs.remove(pktid);
            }

            // Cleanup old buffers
            fragbufs.entrySet().removeIf(e ->
                    now - e.getValue().last > 10000);
        }
    }

    private void mapdata2(Message msg) {
        Coord c = msg.coord();
        GridData grid = grids.computeIfAbsent(c, GridData::new);
        grid.fill(msg);
    }



}