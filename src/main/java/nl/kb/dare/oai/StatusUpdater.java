package nl.kb.dare.oai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import nl.kb.dare.model.repository.RepositoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class StatusUpdater extends AbstractScheduledService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(StatusUpdater.class);

    private final OaiRecordStatusAggregator oaiRecordStatusAggregator;
    private final ScheduledOaiHarvester oaiHarvester;
    private final ScheduledOaiRecordFetcher oaiRecordFetcher;
    private final RepositoryDao repositoryDao;

    public StatusUpdater(OaiRecordStatusAggregator oaiRecordStatusAggregator,
                         ScheduledOaiHarvester oaiHarvester,
                         ScheduledOaiRecordFetcher oaiRecordFetcher,
                         RepositoryDao repositoryDao) {
        this.oaiRecordStatusAggregator = oaiRecordStatusAggregator;
        this.oaiHarvester = oaiHarvester;
        this.oaiRecordFetcher = oaiRecordFetcher;
        this.repositoryDao = repositoryDao;
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
                statusUpdate.put("repositoryStatus",
                    repositoryDao.list().stream().map(repository -> {
                        final Map<String, Object> repoStatus = Maps.newHashMap();
                        repoStatus.put("name", repository.getName());
                        repoStatus.put("dateStamp", repository.getDateStamp());
                        repoStatus.put("enabled", repository.getEnabled());
                        repoStatus.put("id", repository.getId());
                        return repoStatus;
                    }).collect(toList()));

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
