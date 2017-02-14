package nl.kb.dare.http.responsehandlers;

import nl.kb.dare.xslt.XsltTransformer;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

public class XsltTransformingResponseHandler extends ErrorReportingResponseHandler {

    private final Result out;
    private final XsltTransformer xsltTransformer;

    XsltTransformingResponseHandler(Result out, XsltTransformer xsltTransformer) {
        this.out = out;
        this.xsltTransformer = xsltTransformer;
    }

    @Override
    public void onResponseData(Response.Status status, InputStream responseData) {
        try {
            xsltTransformer.transform(responseData, out);
        } catch (TransformerException e) {
            saxExceptions.add(new SAXException("failed to transform xml data with xslt", e));
        }  catch (IOException e) {
            ioExceptions.add(new IOException("I/O exception", e));
        }
    }
}
