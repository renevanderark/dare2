package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.endpoints.RootEndpoint;
import nl.kb.dare.endpoints.StatusWebsocketServlet;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.LenientHttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.oai.ScheduledOaiHarvester;
import nl.kb.dare.oai.StatusUpdater;
import nl.kb.dare.taskmanagers.ManagedPeriodicTask;
import org.skife.jdbi.v2.DBI;

import javax.servlet.Servlet;

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
        final OaiRecordDao oaiRecordDao = jdbi.onDemand(OaiRecordDao.class);
        final ScheduledOaiHarvester oaiHarvester = new ScheduledOaiHarvester(repositoryDao, errorReportDao, oaiRecordDao, httpFetcher, responseHandlerFactory);
        final StatusUpdater statusUpdater = new StatusUpdater(new OaiRecordStatusAggregator(jdbi));

        environment.lifecycle().manage(new ManagedPeriodicTask(oaiHarvester));

        environment.lifecycle().manage(new ManagedPeriodicTask(statusUpdater));

        register(environment, new RepositoriesEndpoint(repositoryDao, new RepositoryValidator(httpFetcher, responseHandlerFactory)));
        register(environment, new RootEndpoint(config.getAppTitle(), config.getHostName(), config.getWsProtocol()));

        registerServlet(environment, new StatusWebsocketServlet(), "statusWebsocket");

    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }


    private void registerServlet(Environment environment, Servlet servlet, String name) {
        environment.servlets().addServlet(name, servlet).addMapping("/status-socket");
    }
}
