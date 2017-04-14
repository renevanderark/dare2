package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.oaipmh.ListIdentifiers;
import nl.kb.oaipmh.OaiStatus;
import nl.kb.xslt.PipedXsltTransformer;
import org.xml.sax.InputSource;

import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class IndexMetadataTask extends Task {

    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final PipedXsltTransformer xsltTransformer;
    private final String solrURL;

    public IndexMetadataTask(RepositoryDao repositoryDao, HttpFetcher httpFetcher,
                             ResponseHandlerFactory responseHandlerFactory, PipedXsltTransformer xsltTransformer,
                             String solrURL) {
        super("indexMetadata");
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.solrURL = solrURL;
    }


    @Override
    public void execute(ImmutableMultimap<String, String> params, PrintWriter printWriter) throws Exception {
        final List<Repository> repositories = params.containsKey("repositoryId")
                ? repositoryDao.list().stream()
                    .filter(r -> r.getId() == Integer.parseInt(params.get("repositoryId").iterator().next()))
                    .collect(Collectors.toList())
                : repositoryDao.list();
        final List<Thread> threads = Lists.newArrayList();
        final AtomicLong totCount = new AtomicLong(0L);

        for (Repository repository : repositories) {
            final Thread thread = new Thread(() -> {
                printWriter.println("Started harvest for:" + repository.getName());
                printWriter.flush();
                new ListIdentifiers(
                        repository.getUrl(),
                        repository.getSet(),
                        repository.getMetadataPrefix(),
                        null,
                        httpFetcher,
                        responseHandlerFactory,
                        dateStamp -> { printWriter.println("done: " +  repository.getName()); printWriter.flush(); },
                        exception -> { printWriter.println(exception.getMessage()); printWriter.flush(); },
                        oaiRecordHeader -> {
                            if (oaiRecordHeader.getOaiStatus() == OaiStatus.AVAILABLE) {
                                totCount.getAndIncrement();
                            }
                        },
                        progressStr -> {
                            printWriter.println(totCount);
                            printWriter.println(progressStr);
                            if (progressStr.startsWith(repository.getUrl())) {
                                indexBatch(repository.getName(), repository.getSet(), printWriter, progressStr);
                            }
                            printWriter.flush();
                        }


                ).setVerb("ListRecords").harvest();
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    private void indexBatch(String repoName, String repoSet, PrintWriter printWriter, String requestUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(solrURL)
                    .openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/xml;charset=utf8");
            connection.setDoOutput(true);
            final Writer outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), "UTF8");
            httpFetcher.execute(new URL(requestUrl), responseHandlerFactory.getBaseHandler(inputStream -> {
                try {
                    final HashMap<String, String> parameters = Maps.newHashMap();
                    new InputSource(new InputStreamReader(inputStream, "UTF-8"));
                    parameters.put("source", repoName);
                    parameters.put("source_set", repoSet);


                    xsltTransformer.transform(
                            inputStream,
                            new StreamResult(outputStreamWriter),
                            parameters
                    );
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                } catch (Exception e) {
                    printWriter.println(e.getMessage());
                    printWriter.flush();
                    e.printStackTrace();
                }
            }));

            final InputStream response = connection.getInputStream();
            final BufferedReader resp = new BufferedReader(new InputStreamReader(response));
            while (resp.readLine() != null) {
            }
        } catch (Exception e) {
            printWriter.println(e.getMessage());
            printWriter.flush();
            e.printStackTrace();
        }
    }
}
