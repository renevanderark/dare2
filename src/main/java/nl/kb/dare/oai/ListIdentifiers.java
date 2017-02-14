package nl.kb.dare.oai;

import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

class ListIdentifiers {
    private static final Logger LOG = LoggerFactory.getLogger(ListIdentifiers.class);

    private final Repository repositoryConfig;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final Consumer<Repository> onHarvestComplete;
    private final Consumer<ErrorReport> onException;
    private Consumer<OaiRecord> onOaiRecord;

    ListIdentifiers(Repository repositoryConfig, HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                    Consumer<Repository> onHarvestComplete,
                    Consumer<ErrorReport> onException,
                    Consumer<OaiRecord> onOaiRecord) {
        this.repositoryConfig = repositoryConfig;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.onHarvestComplete = onHarvestComplete;
        this.onException = onException;
        this.onOaiRecord = onOaiRecord;
    }

    private URL makeRequestUrl(String resumptionToken) throws MalformedURLException {
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

            String resumptionToken = null;
            String lastDateStamp = repositoryConfig.getDateStamp();

            while (resumptionToken == null || resumptionToken.trim().length() > 0) {
                final ListIdentifiersXmlHandler xmlHandler = ListIdentifiersXmlHandler.getNewInstance(repositoryConfig.getId(), onOaiRecord);
                final HttpResponseHandler responseHandler = responseHandlerFactory.getSaxParsingHandler(xmlHandler);
                final URL requestUrl = makeRequestUrl(resumptionToken);

                LOG.info(requestUrl.toString());

                httpFetcher.execute(requestUrl, responseHandler);
                final Optional<String> optResumptionToken = xmlHandler.getResumptionToken();
                final Optional<String> optDateStamp = xmlHandler.getLastDateStamp();

                if (responseHandler.getExceptions().size() > 0) {
                    responseHandler.getExceptions().forEach(onException);
                    break;
                }

                if (optDateStamp.isPresent()) {
                    lastDateStamp = optDateStamp.get();
                }

                if (optResumptionToken.isPresent()) {
                    resumptionToken = optResumptionToken.get();
                } else {
                    break;
                }

            }

            repositoryConfig.setDateStamp(lastDateStamp);
            onHarvestComplete.accept(repositoryConfig);
            LOG.info("** harvest done for " + repositoryConfig.getId() + " / " + repositoryConfig.getSet());
        } catch (MalformedURLException e) {
            // SEVERE!!
            throw new RuntimeException(e);
        }
    }
}
