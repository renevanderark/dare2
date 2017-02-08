package nl.kb.dare.oai;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;

import java.util.concurrent.TimeUnit;

public class StatusUpdater extends AbstractScheduledService {
    private final OaiRecordStatusAggregator oaiRecordStatusAggregator;

    public StatusUpdater(OaiRecordStatusAggregator oaiRecordStatusAggregator) {
        this.oaiRecordStatusAggregator = oaiRecordStatusAggregator;

    }

    @Override
    protected void runOneIteration() throws Exception {
        final StatusSocketRegistrations registrations = StatusSocketRegistrations.getInstance();


        if (registrations.hasMembers()) {
            registrations.broadcast(oaiRecordStatusAggregator.getStatus());
        }
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
}
