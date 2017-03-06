package nl.kb.dare.oai;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
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

import static java.util.stream.Collectors.toList;

public class ScheduledOaiRecordFetcher extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiRecordFetcher.class);
    private static final Integer MAX_WORKERS = 20;
    private static final Integer MAX_WORKERS_PER_REPO_PER_ITERATION = 6;
    private static AtomicInteger runningWorkers = new AtomicInteger(0);

    private final OaiRecordDao oaiRecordDao;
    private final RepositoryDao repositoryDao;
    private final ErrorReportDao errorReportDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final FileStorage fileStorage;
    private final XsltTransformer xsltTransformer;
    private final OaiRecordStatusAggregator oaiRecordStatusAggregator;
    private final boolean inSampleMode;

    public enum RunState {
        RUNNING, DISABLING, DISABLED
    }

    private RunState runState;

    public ScheduledOaiRecordFetcher(OaiRecordDao oaiRecordDao, RepositoryDao repositoryDao, ErrorReportDao errorReportDao,
                                     HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                     FileStorage fileStorage, XsltTransformer xsltTransformer,
                                     OaiRecordStatusAggregator oaiRecordStatusAggregator, boolean inSampleMode) {

        this.oaiRecordDao = oaiRecordDao;
        this.repositoryDao = repositoryDao;
        this.errorReportDao = errorReportDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.fileStorage = fileStorage;
        this.xsltTransformer = xsltTransformer;
        this.oaiRecordStatusAggregator = oaiRecordStatusAggregator;
        this.inSampleMode = inSampleMode;
        this.runState = RunState.DISABLED;
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (runState == RunState.DISABLED || runState == RunState.DISABLING) {
            checkRunState();
            return;
        }

        final List<OaiRecord> pendingRecords = fetchNextRecords(MAX_WORKERS - runningWorkers.get());
        final List<Thread> workers = Lists.newArrayList();

        for (OaiRecord oaiRecord : pendingRecords) {
            startRecord(oaiRecord);

            final Thread worker = new Thread(() -> {
                final Stopwatch timer = Stopwatch.createStarted();

                ProcessStatus result = GetRecord.getAndRun(
                        repositoryDao, oaiRecord, httpFetcher, responseHandlerFactory, fileStorage, xsltTransformer,
                        (ErrorReport errorReport) -> saveErrorReport(errorReport, oaiRecord), // on error
                        oaiRecordStatusAggregator::digestProgressReport,
                        inSampleMode
                );

                finishRecord(oaiRecord, result, timer.elapsed(TimeUnit.SECONDS));
                runningWorkers.getAndDecrement();
            });
            workers.add(worker);
            worker.start();
            runningWorkers.getAndIncrement();
        }

        if (runState == RunState.DISABLING) {
            for (Thread worker : workers) {
                worker.join();
            }
        }

        checkRunState();
    }

    private void checkRunState() {
        runState = runState == RunState.DISABLED || runState == RunState.DISABLING
                ? runningWorkers.get() > 0 ? RunState.DISABLING : RunState.DISABLED
                : RunState.RUNNING;
    }

    private List<OaiRecord> fetchNextRecords(int limit) {
        final List<OaiRecord> result = Lists.newArrayList();
        final List<Integer> repositoryIds = repositoryDao.list().stream()
                .filter(Repository::getEnabled)
                .map(Repository::getId).collect(toList());

        final int dividedLimit = new Double(Math.ceil(((float) limit / (float) repositoryIds.size()))).intValue();

        for (Integer repositoryId : repositoryIds) {
            final List<OaiRecord> list = oaiRecordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PENDING.getCode(),
                    dividedLimit,
                    repositoryId
            );
            result.addAll(list);
            limit -= list.size();
            if (limit <= 0) {
                return result;
            }
        }

        return result;
    }

    public void enable() {
        LOG.info("FETCH RECORDS ENABLED");
        runState = RunState.RUNNING;
    }

    public void disable() {
        runState = RunState.DISABLING;
    }

    RunState getRunState() {
        return runState;
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
        LOG.error("Failed to process record {} ({})", oaiRecord.getIdentifier(), errorReport.getUrl(), errorReport.getException());
        errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(errorReport, oaiRecord));
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 200, TimeUnit.MILLISECONDS);
    }
}
