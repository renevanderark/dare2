package nl.kb.dare.integration.oaiserver;

import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Map;


@Path("/oai")
public class OaiEndpoint {

    private static final Map<String, String> getRecordRecords = Maps.newHashMap();
    static {
        getRecordRecords.put("ru:oai:repository.ubn.ru.nl:2066/162526", "/oai/getrecord/1.xml");
        getRecordRecords.put("ru:oai:repository.ubn.ru.nl:2066/161841", "/oai/getrecord/2.xml");
        getRecordRecords.put("ru:oai:repository.ubn.ru.nl:2066/161830", "/oai/getrecord/3.xml");
        getRecordRecords.put("ru:oai:repository.ubn.ru.nl:2066/162830", "/oai/getrecord/4.xml");
    }

    @GET
    @Produces("text/xml")
    public Response get(
            @QueryParam("verb") String verb,
            @QueryParam("resumptionToken") String resumptionToken,
            @QueryParam("from") String from,
            @QueryParam("identifier") String identifier
    ) {

        switch (verb) {
            case "ListIdentifiers":
                return listIdentifiers(resumptionToken, from);
            case "GetRecord":
                return getRecord(identifier);
            default:
        }
        return Response.ok().build();
    }

    private Response getRecord(String identifier) {
        final InputStream in = OaiEndpoint.class.getResourceAsStream(getRecordRecords.get(identifier));
        final StreamingOutput responseData = output -> IOUtils.copy(in, output);
        return Response.ok(responseData).build();
    }

    private Response listIdentifiers(String resumptionToken, String from) {

        final InputStream in = resumptionToken == null
                ? from == null
                    ? OaiEndpoint.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml")
                    :  OaiEndpoint.class.getResourceAsStream("/oai/ListIdentifiersWithUpdates.xml")
                :  OaiEndpoint.class.getResourceAsStream("/oai/ListIdentifiersWithoutResumptionToken.xml");
        final StreamingOutput responseData = output -> IOUtils.copy(in, output);
        return Response.ok(responseData).build();
    }
}
