package nl.kb.dare.integration;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import nl.kb.dare.integration.oaiserver.OaiEndpoint;
import nl.kb.dare.integration.oaiserver.ResourceEndpoint;

public class OaiTestServer extends Application<Config> {

    public static void main(String... args) throws Exception {
        new OaiTestServer().run(args);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {

        register(environment, new OaiEndpoint());
        register(environment, new ResourceEndpoint());
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
