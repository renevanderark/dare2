package nl.kb.dare.oai;

import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;

import java.io.InputStream;
import java.net.URL;

public class MockHttpFetcher implements HttpFetcher {

    int count = 0;
    private final InputStream[] mockResponses;

    MockHttpFetcher(InputStream... mockResponses) {
        this.mockResponses = mockResponses;
    }

    @Override
    public void execute(URL url, HttpResponseHandler responseHandler) {
        responseHandler.onResponseData(202, mockResponses[count++], null);
    }

}
