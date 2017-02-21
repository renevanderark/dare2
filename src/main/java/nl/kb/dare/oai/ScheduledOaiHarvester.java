package nl.kb.dare.oai;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.files.FileStorage;
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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Subclasses of AbstractScheduledService are guaranteed that the life cycle methods
 * (runOneIteration(), startUp() and shutDown()) will never run concurrently
 */
public class ScheduledOaiHarvester extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiHarvester.class);
    private static final long DELAY = 3_600_000L;

    private final RepositoryDao repositoryDao;
    private final ErrorReportDao errorReportDao;
    private final OaiRecordDao oaiRecordDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final FileStorage fileStorage;

    private RunState runState;
    private Instant lastRunTime = Instant.now();

    private List<ListIdentifiers> runningHarvesters = Lists.newArrayList();

    public enum RunState {
        RUNNING, WAITING, DISABLING, DISABLED
    }

    public ScheduledOaiHarvester(RepositoryDao repositoryDao, ErrorReportDao errorReportDao, OaiRecordDao oaiRecordDao,
                                 HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                 FileStorage fileStorage) {
        this.repositoryDao = repositoryDao;
        this.errorReportDao = errorReportDao;
        this.oaiRecordDao = oaiRecordDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.fileStorage = fileStorage;
        this.runState = RunState.DISABLED;
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (runState == RunState.DISABLED || getNextRunTime() > 0) {
            return;
        }
        runState = RunState.RUNNING;

        final Stopwatch timer = Stopwatch.createStarted();
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
        runState = runState == RunState.DISABLED || runState == RunState.DISABLING
            ? RunState.DISABLED
            : RunState.WAITING;;
    }

    private void saveOaiRecord(OaiRecord newOaiRecord) {
        final OaiRecord existingRecord = oaiRecordDao.findByIdentifier(newOaiRecord.getIdentifier());
        if (existingRecord == null) {
            if (newOaiRecord.getOaiStatus() != OaiStatus.DELETED) {
                oaiRecordDao.insert(newOaiRecord);
            } // else {
                // Do not save a newly encountered deleted record to save space & make faster queries
            // }
        } else if (!existingRecord.equals(newOaiRecord)) {
            // preferably we do not alter the record if the processing thread is using the dao
            synchronized (oaiRecordDao) {
                // The data provider has updated the record since our last encounter during harvest.

                // Check the processing status of the record we already have.
                switch (existingRecord.getProcessStatus()) {
                    case PENDING: // in this case just overwrite the record with new the data
                    case SKIP:    // in this the record would have been _undeleted_, pretty sure that's bad practice.
                    case FAILED:  // in this case we got lucky, maybe the update fixed the data
                        if (newOaiRecord.getOaiStatus() == OaiStatus.DELETED) {
                            oaiRecordDao.delete(newOaiRecord);
                        } else {
                            oaiRecordDao.update(newOaiRecord);
                            if (existingRecord.getProcessStatus() == ProcessStatus.FAILED) {
                                try {
                                    fileStorage.create(newOaiRecord).deleteFiles();
                                } catch (IOException e) {
                                    LOG.warn("Failed to delete failed record", e);
                                }
                            }
                        }
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

    public void enable() {
        if (runState != RunState.RUNNING) {
            LOG.info("HARVESTERS ENABLED");
            lastRunTime = Instant.now().minus(DELAY, ChronoUnit.MILLIS);
            runState = RunState.WAITING;
        }
    }

    public void disable() {
        runState = runState == RunState.RUNNING
                ? RunState.DISABLING
                : RunState.DISABLED;

        runningHarvesters.forEach(ListIdentifiers::interruptHarvest);
    }

    public RunState getRunState() {
        return runState;
    }

    Long getNextRunTime() {
        if (runState == RunState.WAITING) {
            return DELAY + Duration.between(Instant.now(), lastRunTime).toMillis();
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
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.SECONDS);
    }
}
