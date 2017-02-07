package nl.kb.dare.oai;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.HarvesterErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Subclasses are guaranteed that the life cycle methods (runOneIteration(), startUp() and shutDown()) will never run concurrently
 */
public class ScheduledOaiHarvester extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiHarvester.class);

    private final RepositoryDao repositoryDao;
    private final ErrorReportDao errorReportDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    public ScheduledOaiHarvester(RepositoryDao repositoryDao, ErrorReportDao errorReportDao,
                                 HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.repositoryDao = repositoryDao;
        this.errorReportDao = errorReportDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;

    }

    @Override
    protected void runOneIteration() throws Exception {
        repositoryDao.list()
                .stream()
                .map(repo -> new ListIdentifiers(repo, httpFetcher, responseHandlerFactory,
                        this::saveRepositoryStatus, // onHarvestDone
                        errorReport -> saveErrorReport(errorReport, repo.getId()), // onError
                        System.out::println // onOaiRecord
                )).forEach(ListIdentifiers::harvest);

    }

    private void saveErrorReport(ErrorReport errorReport, Integer repositoryId) {
        LOG.error("Oai Harvester error", errorReport.getException());
        errorReportDao.removeHarvesterErrorsForRepositoryWithId(repositoryId);
        errorReportDao.insertHarvesterError(new HarvesterErrorReport(errorReport, repositoryId));
    }

    private void saveRepositoryStatus(Repository repoDone) {
        repositoryDao.update(repoDone.getId(), repoDone);
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 1, TimeUnit.HOURS);
    }
}
