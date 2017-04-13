package nl.kb.dare.http.responsehandlers;

import com.google.common.collect.Lists;
import nl.kb.dare.http.HeaderConsumer;
import nl.kb.stream.InputStreamSplitter;

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
                    .filter(out -> out instanceof HeaderConsumer)
                    .forEach(out -> ((HeaderConsumer) out).consumeHeaders(headerFields));

            InputStreamSplitter inputStreamSplitter = new InputStreamSplitter(responseData, outputStreams);
            inputStreamSplitter.copy();

        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }

}
