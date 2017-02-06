package nl.kb.dare.oai;

import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.Repository;

import java.net.MalformedURLException;
import java.net.URL;

class ListIdentifiers {
    private final Long sleepTime = 1000L;
    private final Repository repositoryConfig;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private String resumptionToken = null;

    ListIdentifiers(Repository repositoryConfig, HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.repositoryConfig = repositoryConfig;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    private URL makeRequestUrl() throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(repositoryConfig.getUrl()).append("?").append("verb=ListIdentifiers");

        if (resumptionToken != null) {
            urlBuilder.append("&").append(String.format("resumptionToken=%s", resumptionToken));
        } else {
            urlBuilder
                    .append("&").append(String.format("set=%s", repositoryConfig.getSet()))
                    .append("&").append(String.format("metadataPrefix=%s", repositoryConfig.getMetadataPrefix()));

            if (repositoryConfig.getDateStamp() != null) {
                urlBuilder.append("&").append(String.format("from=%s", repositoryConfig.getDateStamp()));
            }
        }
        return new URL(urlBuilder.toString());
    }

    void harvest() {
        try {
            while (resumptionToken == null || resumptionToken.trim().length() > 0) {
                final URL requestUrl = makeRequestUrl();
                final ListIdentifiersXmlHandler xmlHandler = new ListIdentifiersXmlHandler();
                final HttpResponseHandler responseHandler = responseHandlerFactory.getSaxParsingHandler(xmlHandler);

                System.out.println(requestUrl);

                httpFetcher.execute(requestUrl, responseHandler);
                resumptionToken = xmlHandler.getResumptionToken();
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException ignored) {
            // SEVERE!!
        } catch (MalformedURLException e) {
            // SEVERE!!
            throw new RuntimeException(e);
        }
    }
}
