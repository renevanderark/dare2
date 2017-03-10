package nl.kb.dare.http.responsehandlers;

import com.google.common.collect.Lists;
import nl.kb.dare.oai.ProgressReportingByteCountOutputStream;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

class StreamCopyingResponseHandler extends ErrorReportingResponseHandler {

    private final List<OutputStream> outputStreams;

    StreamCopyingResponseHandler(OutputStream... outputStreams) {
        this.outputStreams = Lists.newArrayList(outputStreams);
    }

    @Override
    public void onResponseData(Response.Status status, InputStream responseData, Map<String, List<String>> headerFields) {
        try {
            outputStreams
                    .stream()
                    .filter(out -> out instanceof ProgressReportingByteCountOutputStream)
                    .forEach(out -> ((ProgressReportingByteCountOutputStream) out).readExpectedFileSize(headerFields));

            byte[] buffer = new byte[1024];
            int numRead;
            do {
                numRead = responseData.read(buffer);
                if (numRead > 0) {
                    for (OutputStream outputStream : outputStreams) {
                        outputStream.write(buffer, 0, numRead);
                    }

                }
            } while (numRead != -1);
            responseData.close();
            for (OutputStream outputStream : outputStreams) {
                outputStream.close();
            }

        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }

}
