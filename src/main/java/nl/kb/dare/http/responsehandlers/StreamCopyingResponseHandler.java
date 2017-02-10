package nl.kb.dare.http.responsehandlers;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class StreamCopyingResponseHandler extends ErrorReportingResponseHandler {
    private static final MessageDigest md5;
    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private final OutputStream outputStream;
    private final OutputStream checksumOut;

    StreamCopyingResponseHandler(OutputStream outputStream, OutputStream checksumOut) {
        this.outputStream = outputStream;
        this.checksumOut = checksumOut;
    }

    @Override
    public void onResponseData(Response.Status status, InputStream responseData) {
        try {

            byte[] buffer = new byte[1024];

            int numRead;
            do {
                numRead = responseData.read(buffer);
                if (numRead > 0) {
                    md5.update(buffer, 0, numRead);
                    outputStream.write(buffer, 0, numRead);
                }
            } while (numRead != -1);
            responseData.close();
            outputStream.close();

            final byte[] digest = md5.digest();
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            final PrintWriter pw = new PrintWriter(new OutputStreamWriter(checksumOut));
            pw.write(sb.toString());
            pw.close();
            checksumOut.close();
        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }
}
