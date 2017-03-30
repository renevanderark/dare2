package nl.kb.dare.oai;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.xslt.PipedXsltTransformer;
import org.xml.sax.InputSource;

import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IndexMetadataTask extends Task {

    private final OaiRecordDao oaiRecordDao;
    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final PipedXsltTransformer xsltTransformer;

    public IndexMetadataTask(OaiRecordDao oaiRecordDao, RepositoryDao repositoryDao, HttpFetcher httpFetcher,
                             ResponseHandlerFactory responseHandlerFactory, PipedXsltTransformer xsltTransformer) {
        super("indexMetadata");
        this.oaiRecordDao = oaiRecordDao;
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
    }


    private void harvestToIndex(PrintWriter printWriter) throws InterruptedException {
        final Iterator<OaiRecord> recordIterator = oaiRecordDao.listAll();

        final AtomicLong count = new AtomicLong(0L);
        final AtomicInteger tCount = new AtomicInteger(0);
        while(recordIterator.hasNext()) {

            while (tCount.get() > 60) {
                Thread.sleep(5L);
            }

            tCount.incrementAndGet();
            new Thread(() -> {
                try {
                    final OaiRecord oaiRecord = recordIterator.next();
                    final Repository repository = repositoryDao.findById(oaiRecord.getRepositoryId());
                    final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                            repository.getUrl(), repository.getMetadataPrefix(), oaiRecord.getIdentifier());

                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8983/solr/gettingstarted/update")
                            .openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-type", "application/xml;charset=utf8");
                    connection.setDoOutput(true);
                    final Writer outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), "UTF8");
                    httpFetcher.execute(new URL(urlStr), responseHandlerFactory.getBaseHandler(inputStream -> {
                        try {
                            final HashMap<String, String> parameters = Maps.newHashMap();
                            new InputSource(new InputStreamReader(inputStream, "UTF-8"));
                            parameters.put("source", repository.getName());
                            parameters.put("source_set", repository.getSet());
                            xsltTransformer.transform(
                                    inputStream,
                                    new StreamResult(outputStreamWriter),
                                    parameters
                            );
                            outputStreamWriter.flush();
                            outputStreamWriter.close();
                        } catch (Exception e) {
                            printWriter.println(e);
                        }

                    }));

                    InputStream response = connection.getInputStream();
                    final BufferedReader resp = new BufferedReader(new InputStreamReader(response));
                    while (resp.readLine() != null) {
                    }

                    printWriter.println(count.incrementAndGet() + ": " + urlStr);
                    printWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tCount.decrementAndGet();
            }).start();
        }
    }


    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        harvestToIndex(printWriter);
    }
}
