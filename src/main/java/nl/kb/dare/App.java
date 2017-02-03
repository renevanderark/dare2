package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import nl.kb.dare.endpoints.ManagedTaskEndpoint;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.oai.ListIdentifiers;
import nl.kb.dare.oai.OaiTaskManager;
import nl.kb.dare.oai.OaiTaskRunner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.skife.jdbi.v2.DBI;

public class App extends Application<Config> {
    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, config.getDataSourceFactory(), "mysql");
        final RepositoryDao repositoryDao = jdbi.onDemand(RepositoryDao.class);
        final CloseableHttpClient oaiValidateClient = new HttpClientBuilder(environment)
                .build("oai-validate-client");

        final OaiTaskRunner oaiTaskRunner = new OaiTaskRunner(new ListIdentifiers(repositoryDao));

        environment.lifecycle().manage(new OaiTaskManager(oaiTaskRunner));

        register(environment, new RepositoriesEndpoint(repositoryDao, new RepositoryValidator(oaiValidateClient)));
        register(environment, new ManagedTaskEndpoint(oaiTaskRunner));
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
