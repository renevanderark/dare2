package nl.kb.dare.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.oai.ScheduledOaiHarvester;

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

    @JsonProperty
    public HarvesterStatus harvesterStatus;
}
