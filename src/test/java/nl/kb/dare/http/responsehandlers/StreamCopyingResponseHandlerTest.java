package nl.kb.dare.http.responsehandlers;

import nl.kb.dare.http.HttpResponseHandler;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class StreamCopyingResponseHandlerTest {

    @Test
    public void itShouldCopyTheBytesToTheOutputStream() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();
        final HttpResponseHandler instance = new ResponseHandlerFactory().getStreamCopyingResponseHandler(out, checksumOut);

        instance.onResponseData(Response.Status.OK, StreamCopyingResponseHandler.class.getResourceAsStream("/http/text.txt"));

        assertThat(new String(out.toByteArray(), Charset.forName("UTF8")), is("testing"));
    }

    @Test
    public void itShouldCopyThehecksumToTheOtherOutputStream() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();
        final HttpResponseHandler instance = new ResponseHandlerFactory().getStreamCopyingResponseHandler(out, checksumOut);

        instance.onResponseData(Response.Status.OK, StreamCopyingResponseHandler.class.getResourceAsStream("/http/text.txt"));

        assertThat(new String(checksumOut.toByteArray(), Charset.forName("UTF8")), is("ae2b1fca515949e5d54fb22b8ed95575"));
    }

    @Test
    public void itShouldLogIOExceptions() throws IOException {
        final OutputStream out = mock(OutputStream.class);
        final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();
        final HttpResponseHandler instance = new ResponseHandlerFactory().getStreamCopyingResponseHandler(out, checksumOut);

        doThrow(IOException.class).when(out).write(any(), anyInt(), anyInt());

        instance.onResponseData(Response.Status.OK, StreamCopyingResponseHandler.class.getResourceAsStream("/http/text.txt"));

        assertThat(instance.getExceptions().size(), is(1));
    }
}