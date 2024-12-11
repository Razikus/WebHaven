package tech.razikus.headlesshaven;

import haven.Coord;
import haven.LimitMessage;
import haven.Message;
import haven.StreamMessage;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

public class ResourceReader {
    private static final Pattern UNSAFE_CHARS = Pattern.compile("[^a-zA-Z0-9-]");
    private static final String CACHE_DIR = "cache";

    public static Coord readCoord(Message msg) {
        return new Coord(msg.int16(), msg.int16());
    }
    public static void main(String[] args) {
        try {
            byte[] resourceData = downloadResource("gfx/terobjs/arch/timberhouse");
            Message msg = new StreamMessage(new ByteArrayInputStream(resourceData));

            byte[] sig = msg.bytes("Haven Resource 1".length()); // xd
            int ver = msg.uint16();

            System.out.println("Resource version: " + ver);

            while (!msg.eom()) {
                String layerType = msg.string();
                int len = msg.int32();
                Message layerMsg = new LimitMessage(msg, len);

                System.out.println("\nLayer: " + layerType + " (length: " + len + ")");

                switch (layerType) {
                    case "neg":
                        Coord cc = readCoord(layerMsg);
                        System.out.println("Click center: " + cc);
                        layerMsg.skip(12);
                        int en = layerMsg.uint8();
                        for (int i = 0; i < en; i++) {
                            int epid = layerMsg.uint8();
                            int cn = layerMsg.uint16();
                            System.out.println("Click points group " + epid + " (type: " + getClickType(epid) + "):");
                            for (int o = 0; o < cn; o++) {
                                Coord point = readCoord(layerMsg);
                                System.out.println("  " + point);
                            }
                        }
                        break;

                    case "obst":
                        int ver2 = layerMsg.uint8();
                        String id = (ver2 >= 2) ? layerMsg.string() : "";
                        int numPolys = layerMsg.uint8();
                        System.out.println("Obstacle data version: " + ver2);
                        System.out.println("Obstacle ID: " + (id.isEmpty() ? "none" : id));
                        System.out.println("Number of polygons: " + numPolys);
                        // Read polygon points
                        for (int i = 0; i < numPolys; i++) {
                            int points = layerMsg.uint8();
                            System.out.println("Polygon " + i + " points:");
                            for (int j = 0; j < points; j++) {
                                float x = layerMsg.float16();
                                float y = layerMsg.float16();
                                System.out.println("  (" + x + ", " + y + ")");
                            }
                        }
                        break;

                    case "props":
                        int propsVer = layerMsg.uint8();
                        Object[] props = layerMsg.list();
                        System.out.println("Properties version: " + propsVer);
                        for (int i = 0; i < props.length - 1; i += 2) {
                            System.out.println("  " + props[i] + " = " + stringify(props[i+1]));
                        }
                        break;

                    case "mesh":
                        System.out.println("Mesh data size: " + len + " bytes");
                        // Could parse vertex data here if needed
                        layerMsg.skip();
                        break;

                    case "mat2":
                        System.out.println("Material definition size: " + len + " bytes");
                        // Could parse material properties here
                        layerMsg.skip();
                        break;

                    default:
                        layerMsg.skip();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getClickType(int epid) {
        switch (epid) {
            case 0: return "Main click area";
            case 1: return "Alternative click area";
            case 2: return "Door/gate";
            case 3: return "Opening mechanism";
            case 4: return "Secondary interaction";
            default: return "Unknown type " + epid;
        }
    }


    private static String stringify(Object obj) {
        if (obj instanceof Object[]) {
            StringBuilder sb = new StringBuilder("[");
            for (Object o : (Object[])obj) {
                if (sb.length() > 1) sb.append(", ");
                sb.append(stringify(o));
            }
            return sb.append("]").toString();
        }
        return String.valueOf(obj);
    }

    private static String getSafeFileName(String name) {
        // Replace slashes with dashes first
        String safe = name.replace('/', '-').replace('\\', '-');
        // Replace any other unsafe characters with dashes
        safe = UNSAFE_CHARS.matcher(safe).replaceAll("-");
        // Collapse multiple dashes into one
        safe = safe.replaceAll("-+", "-");
        // Remove leading/trailing dashes
        safe = safe.replaceAll("^-+|-+$", "");
        return safe;
    }

    private static byte[] getFromCacheOrNull (String name) {
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                return null;
            }

            String safeFileName = getSafeFileName(name);
            Path filePath = cacheDir.resolve(safeFileName + ".cache");

            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to read from cache: " + e.getMessage());
        }
        return null;
    }

    private static void saveToCache (String name, byte[] data) {
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }
            String safeFileName = getSafeFileName(name);
            Path filePath = cacheDir.resolve(safeFileName + ".cache");
            Files.write(filePath, data);
        } catch (IOException e) {
            System.err.println("Failed to write to cache: " + e.getMessage());
        }
    }

    public static byte[] downloadResource(String name) throws Exception {
        byte[] fromCache = getFromCacheOrNull(name);
        if (fromCache != null) {
            return fromCache;
        }
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        String urlStr = "https://game.havenandhearth.com/res/" + name + ".res";
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Haven/1.0");

        try (InputStream in = conn.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            byte[] data = out.toByteArray();
            saveToCache(name, data);
            return data;
        }
    }
}
