package nl.kb.dare.endpoints;

import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/repositories")
public class RepositoriesEndpoint {
    private RepositoryDao dao;

    public RepositoriesEndpoint(RepositoryDao dao) {

        this.dao = dao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Repository repositoryConfig) {
        final Integer id = dao.insert(repositoryConfig);
        return Response.created(URI.create("/repositories/" + id)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") Integer id) {
        final Repository repositoryConfig = dao.findById(id);

        if (repositoryConfig == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("repository not found with id: " + id, Response.Status.NOT_FOUND.getStatusCode()))
                    .build();
        }
        return Response.ok(repositoryConfig).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {

        dao.remove(id);

        return Response.ok().build();
    }
}
