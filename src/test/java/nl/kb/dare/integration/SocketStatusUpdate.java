package nl.kb.dare.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.oai.ScheduledOaiHarvester;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SocketStatusUpdate {
    public SocketStatusUpdate() {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class HarvesterStatus {
        public HarvesterStatus() {

        }
        @JsonProperty
        public ScheduledOaiHarvester.RunState harvesterRunState;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class RecordProcessingStatus {
        public RecordProcessingStatus() {

        }
        @JsonProperty
        public Map<String, Map<String, Long>> recordStatus;
    }


    @JsonProperty
    public HarvesterStatus harvesterStatus;

    @JsonProperty
    public RecordProcessingStatus recordProcessingStatus;
}
