package nl.kb.dare.endpoints;

import nl.kb.dare.oai.OaiTaskRunner;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/managed-tasks")
public class ManagedTaskEndpoint {
    private final OaiTaskRunner oaiTaskRunner;

    public ManagedTaskEndpoint(OaiTaskRunner oaiTaskRunner) {
        this.oaiTaskRunner = oaiTaskRunner;
    }


    @PUT
    @Path("/oai-harvester")
    public Response toggleOaiTaskRunnerEnabled() {
        if (oaiTaskRunner.isEnabled()) {
            oaiTaskRunner.setEnabled(false);
        } else {
            oaiTaskRunner.setEnabled(true);
        }

        return Response.ok().build();
    }


}
