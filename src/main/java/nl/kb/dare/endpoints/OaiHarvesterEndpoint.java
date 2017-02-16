package nl.kb.dare.endpoints;

import nl.kb.dare.oai.ScheduledOaiHarvester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/harvesters")
public class OaiHarvesterEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiHarvester.class);
    private final ScheduledOaiHarvester oaiHarvester;

    public OaiHarvesterEndpoint(ScheduledOaiHarvester oaiHarvester) {

        this.oaiHarvester = oaiHarvester;
    }

    @PUT
    @Path("/start")
    public Response start() {
        if (oaiHarvester.getRunState() == ScheduledOaiHarvester.RunState.DISABLED) {
            new Thread(() -> {
                try {
                    oaiHarvester.enableAndStart();
                } catch (Exception e) {
                    LOG.error("Manually started OAI harvest failed", e);
                }
            }).start();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/disable")
    public Response disable() {
        oaiHarvester.disable();
        return Response.ok().build();
    }
}
