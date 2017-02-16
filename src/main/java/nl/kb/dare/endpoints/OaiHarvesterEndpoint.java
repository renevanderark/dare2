package nl.kb.dare.endpoints;

import nl.kb.dare.oai.ScheduledOaiHarvester;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/harvesters")
public class OaiHarvesterEndpoint {
    private final ScheduledOaiHarvester oaiHarvester;

    public OaiHarvesterEndpoint(ScheduledOaiHarvester oaiHarvester) {

        this.oaiHarvester = oaiHarvester;
    }

    @PUT
    @Path("/start")
    public Response start() {
        try {
            oaiHarvester.enableAndStart();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/disable")
    public Response disable() {
        oaiHarvester.disable();
        return Response.ok().build();
    }
}
