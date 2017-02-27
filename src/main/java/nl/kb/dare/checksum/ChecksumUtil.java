package nl.kb.dare.checksum;

public class ChecksumUtil {

    public static String getChecksumString(byte[] checksum) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : checksum) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
