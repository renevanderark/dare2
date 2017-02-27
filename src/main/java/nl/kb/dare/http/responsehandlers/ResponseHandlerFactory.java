package nl.kb.dare.http.responsehandlers;

import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.xslt.XsltTransformer;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.Result;
import java.io.OutputStream;

public class ResponseHandlerFactory {

    public HttpResponseHandler getSaxParsingHandler(DefaultHandler saxHandler) {
        return new SaxParsingResponseHandler(saxHandler);
    }

    public HttpResponseHandler getStreamCopyingResponseHandler(OutputStream... outputStreams) {
        return new StreamCopyingResponseHandler(outputStreams);
    }

    public HttpResponseHandler getXsltTransformingHandler(Result out, XsltTransformer xsltTransformer) {
        return new XsltTransformingResponseHandler(out, xsltTransformer);
    }
}
