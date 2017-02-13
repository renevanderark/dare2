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
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledOaiRecordFetcher extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiRecordFetcher.class);
    private static final Integer MAX_WORKERS = 20;
    private static AtomicInteger runningWorkers = new AtomicInteger(0);

    private final OaiRecordDao oaiRecordDao;
    private final RepositoryDao repositoryDao;
    private final ErrorReportDao errorReportDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final FileStorage fileStorage;
    private final XsltTransformer xsltTransformer;

    public ScheduledOaiRecordFetcher(OaiRecordDao oaiRecordDao, RepositoryDao repositoryDao, ErrorReportDao errorReportDao,
                                     HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                     FileStorage fileStorage, XsltTransformer xsltTransformer) {

        this.oaiRecordDao = oaiRecordDao;
        this.repositoryDao = repositoryDao;
        this.errorReportDao = errorReportDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.fileStorage = fileStorage;
        this.xsltTransformer = xsltTransformer;
    }

    @Override
    protected void runOneIteration() throws Exception {
        final List<OaiRecord> pendingRecords = fetchNextRecords(MAX_WORKERS - runningWorkers.get());
        final List<Thread> workers = Lists.newArrayList();

        for (OaiRecord oaiRecord : pendingRecords) {
            final Thread worker = new Thread(() -> {
                final Stopwatch timer = Stopwatch.createStarted();

                final Repository repositoryConfig = repositoryDao.findById(oaiRecord.getRepositoryId());
                if (repositoryConfig == null) {
                    LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", oaiRecord);
                    // TODO error report
                    finishRecord(oaiRecord, ProcessStatus.FAILED, timer.elapsed(TimeUnit.SECONDS));
                    return;
                }

                startRecord(oaiRecord);
                final ProcessStatus result = new GetRecord(
                        oaiRecord,
                        repositoryConfig,
                        fileStorage, httpFetcher, responseHandlerFactory, xsltTransformer,
                        (ErrorReport errorReport) -> saveErrorReport(errorReport, oaiRecord) // on Error
                ).fetch();
                finishRecord(oaiRecord, result, timer.elapsed(TimeUnit.SECONDS));
                runningWorkers.getAndDecrement();
            });
            workers.add(worker);
            worker.start();
            runningWorkers.getAndIncrement();
        }
        LOG.info("RUNNING WORKERS: " + runningWorkers);
    }

    private List<OaiRecord> fetchNextRecords(int limit) {
        return oaiRecordDao.fetchNextWithProcessStatus(ProcessStatus.PENDING.getCode(), limit);
    }

    private void startRecord(OaiRecord oaiRecord) {
        oaiRecord.setProcessStatus(ProcessStatus.PROCESSING);
        oaiRecordDao.update(oaiRecord);
    }

    private void finishRecord(OaiRecord oaiRecord, ProcessStatus processStatus, long elapsed) {
        LOG.info("Finished record {} with status {}  in {} seconds", oaiRecord.getIdentifier(), processStatus, elapsed);
        oaiRecord.setProcessStatus(processStatus);
        oaiRecordDao.update(oaiRecord);
    }

    private void saveErrorReport(ErrorReport errorReport, OaiRecord oaiRecord) {
        LOG.error("Failed to process record {}", oaiRecord.getIdentifier(), errorReport.getException());
        errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(errorReport, oaiRecord));
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 10, TimeUnit.SECONDS);
    }
}
