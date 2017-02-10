package nl.kb.dare.http.responsehandlers;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class StreamCopyingResponseHandler extends ErrorReportingResponseHandler {
    private final OutputStream outputStream;

    StreamCopyingResponseHandler(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void onResponseData(Response.Status status, InputStream responseData) {
        try {
            IOUtils.copy(responseData, outputStream);
        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }
}
