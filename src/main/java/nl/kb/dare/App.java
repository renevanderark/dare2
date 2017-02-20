package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.kb.dare.endpoints.OaiHarvesterEndpoint;
import nl.kb.dare.endpoints.OaiRecordFetcherEndpoint;
import nl.kb.dare.endpoints.OaiRecordsEndpoint;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.endpoints.RootEndpoint;
import nl.kb.dare.endpoints.StatusWebsocketServlet;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.LenientHttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.oai.ScheduledOaiHarvester;
import nl.kb.dare.oai.ScheduledOaiRecordFetcher;
import nl.kb.dare.oai.StatusUpdater;
import nl.kb.dare.taskmanagers.ManagedPeriodicTask;
import nl.kb.dare.xslt.PipedXsltTransformer;
import org.skife.jdbi.v2.DBI;

import javax.servlet.Servlet;
import javax.xml.transform.stream.StreamSource;

public class App extends Application<Config> {
    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets"));

        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false))
        );
    }


    @Override
    public void run(Config config, Environment environment) throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI db = factory.build(environment, config.getDataSourceFactory(), "datasource");

        final HttpFetcher httpFetcher = new LenientHttpFetcher(true);
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();

        final RepositoryDao repositoryDao = db.onDemand(RepositoryDao.class);
        final ErrorReportDao errorReportDao = db.onDemand(ErrorReportDao.class);
        final OaiRecordDao oaiRecordDao = db.onDemand(OaiRecordDao.class);
        final FileStorage fileStorage = config.getFileStorageFactory().getFileStorage();
        final StreamSource stripOaiXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/strip_oai_wrapper.xsl"));
        // TODO: from database, reloadable
        final StreamSource didlToMetsXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/didl2mets-experimental-version.xsl"));

        final PipedXsltTransformer xsltTransformer = PipedXsltTransformer.newInstance(stripOaiXslt, didlToMetsXslt);

        final ScheduledOaiHarvester oaiHarvester = new ScheduledOaiHarvester(
                repositoryDao, errorReportDao, oaiRecordDao, httpFetcher, responseHandlerFactory);
        final ScheduledOaiRecordFetcher oaiRecordFetcher = new ScheduledOaiRecordFetcher(
                oaiRecordDao, repositoryDao, errorReportDao, httpFetcher, responseHandlerFactory, fileStorage, xsltTransformer,
                config.getInSampleMode());
        final StatusUpdater statusUpdater = new StatusUpdater(new OaiRecordStatusAggregator(db),
                oaiHarvester, oaiRecordFetcher, repositoryDao);


        environment.lifecycle().manage(new ManagedPeriodicTask(oaiRecordFetcher));

        environment.lifecycle().manage(new ManagedPeriodicTask(oaiHarvester));

        environment.lifecycle().manage(new ManagedPeriodicTask(statusUpdater));

        register(environment, new OaiRecordsEndpoint(oaiRecordDao));
        register(environment, new RepositoriesEndpoint(repositoryDao, oaiRecordDao, new RepositoryValidator(httpFetcher, responseHandlerFactory)));
        register(environment, new OaiHarvesterEndpoint(oaiHarvester));
        register(environment, new OaiRecordFetcherEndpoint(oaiRecordFetcher));
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
