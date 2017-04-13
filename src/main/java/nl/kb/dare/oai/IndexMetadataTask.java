package nl.kb.dare.oai;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.xslt.PipedXsltTransformer;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class IndexMetadataTask extends Task {

    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final PipedXsltTransformer xsltTransformer;

    public IndexMetadataTask(RepositoryDao repositoryDao, HttpFetcher httpFetcher,
                             ResponseHandlerFactory responseHandlerFactory, PipedXsltTransformer xsltTransformer) {
        super("indexMetadata");
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
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
                new ListRecords(repository, httpFetcher, responseHandlerFactory, xsltTransformer, str -> {
                    printWriter.println(str);
                    printWriter.flush();
                }, cnt -> {
                    printWriter.println("harvested records: " + totCount.addAndGet(cnt));
                    printWriter.flush();
                }).harvest();
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
