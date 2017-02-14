package nl.kb.dare.http.responsehandlers;

import nl.kb.dare.xslt.XsltTransformer;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class XsltTransformingResponseHandlerTest {

    @Test
    public void itShouldInvokeTransformWithTheResponse() throws TransformerException, UnsupportedEncodingException {
        final InputStream input = mock(InputStream.class);
        final XsltTransformer xsltTransformer = mock(XsltTransformer.class);
        final Result output = mock(Result.class);
        final XsltTransformingResponseHandler instance = new XsltTransformingResponseHandler(output, xsltTransformer);

        instance.onResponseData(Response.Status.ACCEPTED, input);

        verify(xsltTransformer).transform(input, output);
    }

    @Test
    public void itShouldLogXmlExceptions() throws IOException, TransformerException {
        final InputStream input = mock(InputStream.class);
        final XsltTransformer xsltTransformer = mock(XsltTransformer.class);
        final Result output = mock(Result.class);
        final XsltTransformingResponseHandler instance = new XsltTransformingResponseHandler(output, xsltTransformer);

        doThrow(TransformerException.class).when(xsltTransformer).transform(input, output);

        instance.onResponseData(Response.Status.ACCEPTED, input);

        assertThat(instance.getExceptions().isEmpty(), is(false));
        assertThat(instance.getExceptions().get(0).getException(), is(instanceOf(SAXException.class)));
    }

    @Test
    public void itShouldLogIOExceptions() throws IOException, TransformerException {
        final InputStream input = mock(InputStream.class);
        final XsltTransformer xsltTransformer = mock(XsltTransformer.class);
        final Result output = mock(Result.class);
        final XsltTransformingResponseHandler instance = new XsltTransformingResponseHandler(output, xsltTransformer);

        doThrow(IOException.class).when(xsltTransformer).transform(input, output);

        instance.onResponseData(Response.Status.ACCEPTED, input);

        assertThat(instance.getExceptions().isEmpty(), is(false));
        assertThat(instance.getExceptions().get(0).getException(), is(instanceOf(IOException.class)));
    }
}