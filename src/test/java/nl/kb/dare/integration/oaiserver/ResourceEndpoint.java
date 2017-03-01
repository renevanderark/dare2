package nl.kb.dare.integration.oaiserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/resource-mock/{filename}")
public class ResourceEndpoint {

    @GET
    @Produces("text/plain")
    public Response get(@PathParam("filename") String filename) {

        return Response.ok(filename).build();
    }
}
