package nl.kb.stream;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumOutputStream extends ByteArrayOutputStream {


    private final MessageDigest digest;
    private String checkSumString = null;

    public ChecksumOutputStream(String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public synchronized void write(int b) {
        digest.update((byte) b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        digest.update(b, off, len);
    }

    public byte[] getChecksum() {
        return digest.digest();
    }

    public String getChecksumString() {
        if (checkSumString == null) {
            final StringBuilder sb = new StringBuilder();
            for (byte b : getChecksum()) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            checkSumString = sb.toString();
        }
        return checkSumString;
    }

}
