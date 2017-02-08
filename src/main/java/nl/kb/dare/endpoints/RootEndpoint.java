package nl.kb.dare.endpoints;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class RootEndpoint {

    private static final String HTML_TEMPLATE;

    static {
        final InputStream resource =
                RootEndpoint.class.getClassLoader().getResourceAsStream("assets/html/root-template.html");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(resource))) {
            HTML_TEMPLATE = buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final String appTitle;
    private final String hostName;
    private final String wsProtocol;

    public RootEndpoint(String appTitle, String hostName, String wsProtocol) {

        this.appTitle = appTitle;
        this.hostName = hostName;
        this.wsProtocol = wsProtocol;
    }

    @GET
    public Response getHtml() {
        return Response.ok(parseHtmlTemplate()).build();
    }

    @GET
    @Path("{param1}")
    public Response getHtml(@PathParam("param1") String param1) {
        return Response.ok(parseHtmlTemplate(param1)).build();
    }

    @GET
    @Path("{param1}/{param2}")
    public Response getHtml(@PathParam("param1") String param1, @PathParam("param2") String param2) {
        return Response.ok(parseHtmlTemplate(param1, param2)).build();
    }

    @GET
    @Path("{param1}/{param2}/{param3}")
    public Response getHtml(@PathParam("param1") String param1, @PathParam("param2") String param2,
                            @PathParam("param3") String param3) {
        return Response.ok(parseHtmlTemplate(param1, param2, param3)).build();
    }

    @GET
    @Path("{param1}/{param2}/{param3}/{param4}")
    public Response getHtml(@PathParam("param1") String param1, @PathParam("param2") String param2,
                            @PathParam("param3") String param3, @PathParam("param4") String param4) {
        return Response.ok(parseHtmlTemplate(param1, param2, param3, param4)).build();
    }

    private String parseHtmlTemplate(String... pathParams) {
        final JsEnv env = new JsEnv(pathParams, hostName, wsProtocol);

        try {
            final String jsEnv = new ObjectMapper().writeValueAsString(env);
            return HTML_TEMPLATE
                    .replace("<%= APP_TITLE %>", appTitle)
                    .replace("<%= JS_ENVIRONMENT %>", String.format("var globals = %s;", jsEnv));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return HTML_TEMPLATE
                    .replace("<%= APP_TITLE %>", appTitle)
                    .replace("<%= JS_ENVIRONMENT %>", String.format("var globals = %s;", "{}"));
        }

    }

    private class JsEnv {
        @JsonProperty
        private final String[] pathParams;
        @JsonProperty
        private final String hostName;
        @JsonProperty
        private final String wsProtocol;

        JsEnv(String[] pathParams, String hostName, String wsProtocol) {

            this.pathParams = pathParams;
            this.hostName = hostName;
            this.wsProtocol = wsProtocol;
        }
    }
}