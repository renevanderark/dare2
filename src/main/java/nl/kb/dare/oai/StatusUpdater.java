package nl.kb.dare.oai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatusUpdater extends AbstractScheduledService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(StatusUpdater.class);

    private final OaiRecordStatusAggregator oaiRecordStatusAggregator;
    private final ScheduledOaiHarvester oaiHarvester;
    private final ScheduledOaiRecordFetcher oaiRecordFetcher;

    public StatusUpdater(OaiRecordStatusAggregator oaiRecordStatusAggregator,
                         ScheduledOaiHarvester oaiHarvester, ScheduledOaiRecordFetcher oaiRecordFetcher) {
        this.oaiRecordStatusAggregator = oaiRecordStatusAggregator;
        this.oaiHarvester = oaiHarvester;
        this.oaiRecordFetcher = oaiRecordFetcher;
    }

    @Override
    protected void runOneIteration() throws Exception {
        final StatusSocketRegistrations registrations = StatusSocketRegistrations.getInstance();


        if (registrations.hasMembers()) {
            try {
                final Map<String, Map<String, Map<String, Object>>> records = oaiRecordStatusAggregator.getStatus();

                final Map<String, Object> harvesterState = Maps.newHashMap();
                harvesterState.put("nextRunTime", oaiHarvester.getNextRunTime());
                harvesterState.put("harvesterRunState", oaiHarvester.getRunState());
                harvesterState.put("recordFetcherRunState", oaiRecordFetcher.getRunState());


                final Map<String, Object> statusUpdate = Maps.newHashMap();
                statusUpdate.put("harvesterStatus", harvesterState);
                statusUpdate.put("recordProcessingStatus", records);

                registrations.broadcast(OBJECT_MAPPER.writeValueAsString(statusUpdate));
            } catch (Exception e) {
                LOG.error("Status broadcast failed", e);
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 20, TimeUnit.MILLISECONDS);
    }
}
