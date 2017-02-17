package nl.kb.dare.endpoints;

import nl.kb.dare.oai.ScheduledOaiRecordFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/workers")
public class OaiRecordFetcherEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OaiRecordFetcherEndpoint.class);

    private final ScheduledOaiRecordFetcher oaiRecordFetcher;

    public OaiRecordFetcherEndpoint(ScheduledOaiRecordFetcher oaiRecordFetcher) {

        this.oaiRecordFetcher = oaiRecordFetcher;
    }

    @PUT
    @Path("/start")
    public Response start() {
        oaiRecordFetcher.enable();
        return Response.ok().build();
    }

    @PUT
    @Path("/disable")
    public Response disable() {
        oaiRecordFetcher.disable();
        return Response.ok().build();
    }
}
