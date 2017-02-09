package nl.kb.dare.oai;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class StatusUpdater extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(StatusUpdater.class);

    private final OaiRecordStatusAggregator oaiRecordStatusAggregator;

    public StatusUpdater(OaiRecordStatusAggregator oaiRecordStatusAggregator) {
        this.oaiRecordStatusAggregator = oaiRecordStatusAggregator;

    }

    @Override
    protected void runOneIteration() throws Exception {
        final StatusSocketRegistrations registrations = StatusSocketRegistrations.getInstance();


        if (registrations.hasMembers()) {
            try {
                registrations.broadcast(oaiRecordStatusAggregator.getStatus());
            } catch (Exception e) {
                LOG.error("Status broadcast failed", e);
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
}
