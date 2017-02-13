package nl.kb.dare.checksum;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ChecksumUtil {
    public static void saveChecksumString(OutputStream out, byte[] checksum) throws IOException {
        final String checksumString = getChecksumString(checksum);
        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
        pw.write(checksumString);
        pw.close();
        out.close();
    }

    public static String getChecksumString(byte[] checksum) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : checksum) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
