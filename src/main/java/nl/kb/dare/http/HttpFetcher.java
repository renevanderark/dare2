package nl.kb.dare.http;

import java.net.URL;

public interface HttpFetcher {
    void execute(URL url, HttpResponseHandler responseHandler);
}
