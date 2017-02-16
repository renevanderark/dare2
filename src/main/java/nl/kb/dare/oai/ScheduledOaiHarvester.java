package nl.kb.dare.oai;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.HarvesterErrorReport;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Subclasses of AbstractScheduledService are guaranteed that the life cycle methods
 * (runOneIteration(), startUp() and shutDown()) will never run concurrently
 */
public class ScheduledOaiHarvester extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiHarvester.class);

    private final RepositoryDao repositoryDao;
    private final ErrorReportDao errorReportDao;
    private final OaiRecordDao oaiRecordDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    private RunState runState;
    private Instant lastRunTime = Instant.now();

    private List<ListIdentifiers> runningHarvesters = Lists.newArrayList();

    public enum RunState {
        RUNNING, WAITING, DISABLED
    }

    public ScheduledOaiHarvester(RepositoryDao repositoryDao, ErrorReportDao errorReportDao, OaiRecordDao oaiRecordDao,
                                 HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.repositoryDao = repositoryDao;
        this.errorReportDao = errorReportDao;
        this.oaiRecordDao = oaiRecordDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.runState = RunState.DISABLED;
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (runState == RunState.DISABLED) { return; }
        final Stopwatch timer = Stopwatch.createStarted();
        runState = RunState.RUNNING;
        runningHarvesters = repositoryDao.list()
                .stream()
                .filter(Repository::getEnabled)
                .map(repo -> new ListIdentifiers(repo, httpFetcher, responseHandlerFactory,
                        this::saveRepositoryStatus, // onHarvestDone
                        errorReport -> saveErrorReport(errorReport, repo.getId()), // onError
                        this::saveOaiRecord // onOaiRecord
                )).collect(Collectors.toList());

        runningHarvesters.forEach(ListIdentifiers::harvest);
        runningHarvesters = Lists.newArrayList();

        LOG.info("Harvest finished, time taken: {} seconds", timer.stop().elapsed(TimeUnit.SECONDS));
        lastRunTime = Instant.now();
        runState = runState == RunState.DISABLED
            ? RunState.DISABLED
            : RunState.WAITING;;
    }

    private void saveOaiRecord(OaiRecord newOaiRecord) {
        final OaiRecord existingRecord = oaiRecordDao.findByIdentifier(newOaiRecord.getIdentifier());
        if (existingRecord == null) {
            oaiRecordDao.insert(newOaiRecord);
        } else if (!existingRecord.equals(newOaiRecord)) {
            // preferably we do not alter the record if the processing thread is using the dao
            synchronized (oaiRecordDao) {
                // The data provider has updated the record since our last encounter during harvest.

                // Check the processing status of the record we already have.
                switch (existingRecord.getProcessStatus()) {
                    case PENDING: // in this case just overwrite the record with new the data
                    case SKIP:    // in this the record would have been _undeleted_, pretty sure that's bad practice.
                    case FAILED:  // in this case we got lucky, maybe the update fixed the data
                        oaiRecordDao.update(newOaiRecord);
                        break;

                    case DELETED_AFTER_PROCESSING: // in these cases do nothing, there was a problem already
                    case UPDATED_AFTER_PROCESSING:
                        break;

                    case PROCESSED: // in this case we can set the status to deleted|updated after processing
                        if (newOaiRecord.getOaiStatus() == OaiStatus.DELETED) {
                            errorReportDao.insertOaiRecordError(getOaiRecordErrorReport(
                                    newOaiRecord, ErrorStatus.DELETED_AFTER_PROCESSING));
                            newOaiRecord.setProcessStatus(ProcessStatus.DELETED_AFTER_PROCESSING);
                        } else {
                            errorReportDao.insertOaiRecordError(getOaiRecordErrorReport(
                                    newOaiRecord, ErrorStatus.UPDATED_AFTER_PROCESSING));
                            newOaiRecord.setProcessStatus(ProcessStatus.UPDATED_AFTER_PROCESSING);
                        }
                        oaiRecordDao.update(newOaiRecord);
                        break;

                    case PROCESSING:
                    default: // in all other cases the record is already being processed, so log the error. (maybe add pending status to be set after processing is finished?)
                        if (newOaiRecord.getOaiStatus() == OaiStatus.DELETED) {
                            errorReportDao.insertOaiRecordError(getOaiRecordErrorReport(
                                    newOaiRecord, ErrorStatus.DELETED_AFTER_PROCESSING));
                            // TODO? newOaiRecord.setPendingStatus(ProcessStatus.DELETED_AFTER_PROCESSING)
                        } else {
                            errorReportDao.insertOaiRecordError(getOaiRecordErrorReport(
                                    newOaiRecord, ErrorStatus.UPDATED_AFTER_PROCESSING));
                            // TODO? newOaiRecord.setPendingStatus(ProcessStatus.UPDATED_AFTER_PROCESSING)
                        }
                        // TODO? oaiRecordDao.update(newOaiRecord);
                        break;
                }
            }
        }
    }

    public void enableAndStart() throws Exception {
        if (runState != RunState.RUNNING) {
            LOG.info("HARVESTERS STARTED");
            runState = RunState.WAITING;
            runOneIteration();
        }
    }

    public void disable() {
        runState = RunState.DISABLED;
        runningHarvesters.forEach(ListIdentifiers::interruptHarvest);
    }

    public RunState getRunState() {
        return runState;
    }

    Long getNextRunTime() {
        if (runState == RunState.WAITING) {
            return 3_600_000L + Duration.between(Instant.now(), lastRunTime).toMillis();
        }
        return 0L;
    }


    private OaiRecordErrorReport getOaiRecordErrorReport(OaiRecord oaiRecord, ErrorStatus errorStatus) {
        return new OaiRecordErrorReport(
                errorStatus.getStatus(),
                "",
                Instant.now().toString(),
                "",
                errorStatus,
                oaiRecord.getIdentifier());
    }

    private void saveErrorReport(ErrorReport errorReport, Integer repositoryId) {
        LOG.error("Oai Harvester error", errorReport.getException());
        errorReportDao.insertHarvesterError(new HarvesterErrorReport(errorReport, repositoryId));
    }

    private void saveRepositoryStatus(Repository repoDone) {
        repositoryDao.update(repoDone.getId(), repoDone);
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.HOURS);
    }
}
