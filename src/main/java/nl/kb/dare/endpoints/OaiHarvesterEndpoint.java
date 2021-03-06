package nl.kb.dare.endpoints;

import nl.kb.dare.oai.ScheduledOaiHarvester;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/harvesters")
public class OaiHarvesterEndpoint {
    private final ScheduledOaiHarvester oaiHarvester;

    public OaiHarvesterEndpoint(ScheduledOaiHarvester oaiHarvester) {

        this.oaiHarvester = oaiHarvester;
    }


    @PUT
    @Path("/start")
    @Produces("application/json")
    public Response start() {
        oaiHarvester.enable();
        return Response.ok("{}").build();
    }

    @PUT
    @Path("/disable")
    @Produces("application/json")
    public Response disable() {
        oaiHarvester.disable();
        return Response.ok("{}").build();
    }
}
