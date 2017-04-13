package nl.kb.stream;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class InputStreamSplitter {

    private final InputStream inputStream;
    private final List<OutputStream> outputStreams;

    public InputStreamSplitter(InputStream inputStream, List<OutputStream> outputStreams) {
        this.inputStream = inputStream;
        this.outputStreams = outputStreams;
    }

    public InputStreamSplitter(InputStream inputStream, OutputStream... outputStreams) {
        this(inputStream, Lists.newArrayList(outputStreams));
    }

    public void copy() throws IOException {
        byte[] buffer = new byte[1024];
        int numRead;
        do {
            numRead = inputStream.read(buffer);
            if (numRead > 0) {
                for (OutputStream outputStream : outputStreams) {
                    outputStream.write(buffer, 0, numRead);
                }

            }
        } while (numRead != -1);
        inputStream.close();

        for (OutputStream outputStream : outputStreams) {
            outputStream.close();
        }
    }
}
