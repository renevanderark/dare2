package nl.kb.dare.http.responsehandlers;

import nl.kb.dare.http.HttpResponseHandler;
import org.xml.sax.helpers.DefaultHandler;

public class ResponseHandlerFactory {

    public HttpResponseHandler getSaxParsingHandler(DefaultHandler saxHandler) {
        return new SaxParsingResponseHandler(saxHandler);
    }
}
