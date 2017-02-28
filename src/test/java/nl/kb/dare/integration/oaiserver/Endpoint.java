package nl.kb.dare.integration.oaiserver;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;


@Path("/oai")
public class Endpoint {

    @GET
    @Produces("text/xml")
    public Response get(
            @QueryParam("verb") String verb,
            @QueryParam("resumptionToken") String resumptionToken
    ) {

        switch (verb) {
            case "ListIdentifiers":
                return listIdentifiers(resumptionToken);
            default:
        }
        return Response.ok().build();
    }

    private Response listIdentifiers(String resumptionToken) {
        final InputStream in = resumptionToken == null
                ? Endpoint.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml")
                :  Endpoint.class.getResourceAsStream("/oai/ListIdentifiersWithoutResumptionToken.xml");
        final StreamingOutput responseData = output -> IOUtils.copy(in, output);
        return Response.ok(responseData).build();
    }
}
