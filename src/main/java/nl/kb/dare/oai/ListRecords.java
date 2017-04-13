package nl.kb.dare.oai;

import com.google.common.collect.Maps;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.xslt.PipedXsltTransformer;
import org.xml.sax.InputSource;

import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

class ListRecords {


    private final Repository repositoryConfig;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final PipedXsltTransformer xsltTransformer;
    private final Consumer<String> onProgress;
    private final Consumer<Long> onCount;

    ListRecords(Repository repositoryConfig, HttpFetcher httpFetcher,
                ResponseHandlerFactory responseHandlerFactory,
                PipedXsltTransformer xsltTransformer,
                Consumer<String> onProgress, Consumer<Long> onCount) {

        this.repositoryConfig = repositoryConfig;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.onProgress = onProgress;
        this.onCount = onCount;
    }

    private URL makeRequestUrl(String resumptionToken) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(repositoryConfig.getUrl()).append("?").append("verb=ListRecords");

        if (resumptionToken != null) {
            urlBuilder.append("&").append(String.format("resumptionToken=%s", resumptionToken));
        } else {
            urlBuilder
                    .append("&").append(String.format("set=%s", repositoryConfig.getSet()))
                    .append("&").append(String.format("metadataPrefix=%s", repositoryConfig.getMetadataPrefix()));
        }
        return new URL(urlBuilder.toString());
    }

    void harvest()  {
        try {
            String resumptionToken = null;
            while ((resumptionToken == null || resumptionToken.trim().length() > 0)) {
                final URL requestUrl = makeRequestUrl(resumptionToken);
                onProgress.accept(requestUrl.toString());
                AtomicLong recordCount = new AtomicLong(0L);

                final ListIdentifiersXmlHandler xmlHandler = ListIdentifiersXmlHandler.getNewInstance(repositoryConfig.getId(), oaiRecord -> {
                    if (oaiRecord.getOaiStatus() != OaiStatus.DELETED) {
                        recordCount.incrementAndGet();
                    }
                });

                httpFetcher.execute(requestUrl, responseHandlerFactory.getSaxParsingHandler(xmlHandler));

                HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8983/solr/gettingstarted/update")
                        .openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-type", "application/xml;charset=utf8");
                connection.setDoOutput(true);
                final Writer outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), "UTF8");
                httpFetcher.execute(requestUrl, responseHandlerFactory.getBaseHandler(inputStream -> {
                    try {
                        final HashMap<String, String> parameters = Maps.newHashMap();
                        new InputSource(new InputStreamReader(inputStream, "UTF-8"));
                        parameters.put("source", repositoryConfig.getName());
                        parameters.put("source_set", repositoryConfig.getSet());


                        xsltTransformer.transform(
                                inputStream,
                                new StreamResult(outputStreamWriter),
                                parameters
                        );
                        outputStreamWriter.flush();
                        outputStreamWriter.close();
                    } catch (Exception e) {
                        onProgress.accept(e.getMessage());
                        e.printStackTrace();
                    }
                }));

                final InputStream response = connection.getInputStream();
                final BufferedReader resp = new BufferedReader(new InputStreamReader(response));
                while (resp.readLine() != null) { }

                final Optional<String> optResumptionToken = xmlHandler.getResumptionToken();
                onCount.accept(recordCount.get());
                if (optResumptionToken.isPresent()) {
                    resumptionToken = optResumptionToken.get();
                } else {
                    break;
                }
            }
            onProgress.accept("** harvest done for " + repositoryConfig.getId() + " / " + repositoryConfig.getSet());

        } catch (IOException e) {
            onProgress.accept(e.getMessage());
            e.printStackTrace();
        }
    }

}
