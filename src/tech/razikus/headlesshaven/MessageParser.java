package tech.razikus.headlesshaven;

import haven.MessageBuf;
import haven.OCache;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class MessageParser {
    public static void parseObjDelta(OCache.ObjDelta delta) {
        System.out.println("=== Delta Debug ===");
        System.out.println("Object ID: " + delta.id);
//        System.out.println("Frame: " + delta.frame);
//        System.out.println("Flags: " + delta.fl);

        for (OCache.AttrDelta attr : delta.attrs) {
            System.out.println(getODName(attr.type)+ " " +  "Raw bytes: " + Arrays.toString(attr.rbuf));
        }
    }
    public static String getODName(int id) {
        switch (id) {
            case OCache.OD_REM: return "OD_REM";
            case OCache.OD_MOVE: return "OD_MOVE";
            case OCache.OD_RES: return "OD_RES";
            case OCache.OD_LINBEG: return "OD_LINBEG";
            case OCache.OD_LINSTEP: return "OD_LINSTEP";
            case OCache.OD_SPEECH: return "OD_SPEECH";
            case OCache.OD_COMPOSE: return "OD_COMPOSE";
            case OCache.OD_ZOFF: return "OD_ZOFF";
            case OCache.OD_LUMIN: return "OD_LUMIN";
            case OCache.OD_AVATAR: return "OD_AVATAR";
            case OCache.OD_FOLLOW: return "OD_FOLLOW";
            case OCache.OD_HOMING: return "OD_HOMING";
            case OCache.OD_OVERLAY: return "OD_OVERLAY";
            case OCache.OD_HEALTH: return "OD_HEALTH";
            case OCache.OD_CMPPOSE: return "OD_CMPPOSE";
            case OCache.OD_CMPMOD: return "OD_CMPMOD";
            case OCache.OD_CMPEQU: return "OD_CMPEQU";
            case OCache.OD_ICON: return "OD_ICON";
            case OCache.OD_RESATTR: return "OD_RESATTR";
            case OCache.OD_END: return "OD_END";
            default: return "Unknown(" + id + ")";
        }
    }

    private static void debugResAttr(OCache.AttrDelta attr) {
        try {
            MessageBuf msg = new MessageBuf(attr.rbuf);
            System.out.println("\nDEBUG RESATTR:");

            // Parse resource ID (first 2 bytes)
            int resid = msg.uint16();
            System.out.println("Resource ID: " + resid + " (0x" + Integer.toHexString(resid) + ")");

            // Parse length (next byte)
            int len = msg.uint8();
            System.out.println("Data length: " + len);

            if (len > 0) {
                // Get remaining data
                byte[] data = msg.bytes(len);
                System.out.println("Message data: " + Arrays.toString(data));

                // Print data in different formats for analysis
                System.out.println("Data as shorts: " + bytesToShorts(data));
                System.out.println("Data as ints: " + bytesToInts(data));
                System.out.println("Data as string: " + new String(data, StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            System.out.println("Error parsing RESATTR: " + e);
            e.printStackTrace();
        }
    }

    private static void debugCmpEqu(OCache.AttrDelta attr) {
        try {
            MessageBuf msg = new MessageBuf(attr.rbuf);
            System.out.println("\nDEBUG CMPEQU:");

            while (true) {
                int h = msg.uint8();
                if (h == 255) break;

                int ef = h & 0x80;
                int et = h & 0x7f;
                String at = msg.string();
                int resid = msg.uint16();

                System.out.println(String.format(
                        "Equipment - ef: %d, et: %d, at: %s, resid: %d (0x%x)",
                        ef, et, at, resid, resid
                ));

                if ((resid & 0x8000) != 0) {
                    resid &= ~0x8000;
                    int len = msg.uint8();
                    byte[] sdt = msg.bytes(len);
                    System.out.println("Extra data: " + Arrays.toString(sdt));
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing CMPEQU: " + e);
            e.printStackTrace();
        }
    }

    private static String bytesToShorts(byte[] bytes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bytes.length - 1; i += 2) {
            if (i > 0) sb.append(", ");
            int value = ((bytes[i] & 0xFF) << 8) | (bytes[i + 1] & 0xFF);
            sb.append(value);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String bytesToInts(byte[] bytes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bytes.length - 3; i += 4) {
            if (i > 0) sb.append(", ");
            int value = ((bytes[i] & 0xFF) << 24) |
                    ((bytes[i + 1] & 0xFF) << 16) |
                    ((bytes[i + 2] & 0xFF) << 8) |
                    (bytes[i + 3] & 0xFF);
            sb.append(value);
        }
        sb.append("]");
        return sb.toString();
    }
}