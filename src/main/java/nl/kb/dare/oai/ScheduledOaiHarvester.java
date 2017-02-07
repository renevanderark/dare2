package nl.kb.dare.oai;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.RepositoryDao;

import java.util.concurrent.TimeUnit;

/**
 * Subclasses are guaranteed that the life cycle methods (runOneIteration(), startUp() and shutDown()) will never run concurrently
 */
public class ScheduledOaiHarvester extends AbstractScheduledService {
    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    public ScheduledOaiHarvester(RepositoryDao repositoryDao, HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    @Override
    protected void runOneIteration() throws Exception {
        repositoryDao.list()
                .stream()
                .map(repo -> new ListIdentifiers(repo, httpFetcher, responseHandlerFactory,
                        (repoDone) -> repositoryDao.update(repoDone.getId(), repoDone), // onHarvestDone
                        (errorReport -> System.out.println(errorReport.getFilteredStackTrace())), // onError
                        System.out::println // onOaiRecord
                )).forEach(ListIdentifiers::harvest);

    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 1, TimeUnit.HOURS);
    }
}
