package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.LenientHttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.oai.ScheduledOaiHarvester;
import nl.kb.dare.taskmanagers.ManagedPeriodicTask;
import org.skife.jdbi.v2.DBI;

public class App extends Application<Config> {
    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, config.getDataSourceFactory(), "mysql");

        final HttpFetcher httpFetcher = new LenientHttpFetcher(true);
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();
        final RepositoryDao repositoryDao = jdbi.onDemand(RepositoryDao.class);
        final ErrorReportDao errorReportDao = jdbi.onDemand(ErrorReportDao.class);

        environment.lifecycle().manage(new ManagedPeriodicTask(new ScheduledOaiHarvester(repositoryDao, errorReportDao, httpFetcher, responseHandlerFactory)));

        register(environment, new RepositoriesEndpoint(repositoryDao, new RepositoryValidator(httpFetcher, responseHandlerFactory)));
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
