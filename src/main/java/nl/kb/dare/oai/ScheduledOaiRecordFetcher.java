package nl.kb.dare.oai;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScheduledOaiRecordFetcher extends AbstractScheduledService {

    private final OaiRecordDao oaiRecordDao;

    public ScheduledOaiRecordFetcher(OaiRecordDao oaiRecordDao) {

        this.oaiRecordDao = oaiRecordDao;
    }

    @Override
    protected void runOneIteration() throws Exception {

        // We do not want to free the dao until processing updates are finished on the record
        synchronized (oaiRecordDao) {

            final Optional<OaiRecord> oaiRecordOptional = fetchNextRecord();

            if (!oaiRecordOptional.isPresent()) {
                return;
            }
            final OaiRecord oaiRecord = oaiRecordOptional.get();

            System.out.println(oaiRecord);

            // mimic some downloads
            Thread.sleep(3000);

            finishRecord(oaiRecord);
        }
    }

    private Optional<OaiRecord> fetchNextRecord() {
        final OaiRecord oaiRecord = oaiRecordDao.fetchNextWithProcessStatus(ProcessStatus.PENDING.getCode());
        if (oaiRecord == null) {
            return Optional.empty();
        }
        oaiRecord.setProcessStatus(ProcessStatus.PROCESSING);
        oaiRecordDao.update(oaiRecord);
        return Optional.of(oaiRecord);
    }


    private void finishRecord(OaiRecord oaiRecord) {
        oaiRecord.setProcessStatus(ProcessStatus.PROCESSED);
        oaiRecordDao.update(oaiRecord);
    }


    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.MILLISECONDS);
    }
}
