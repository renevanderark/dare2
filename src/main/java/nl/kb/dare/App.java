package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.kb.dare.endpoints.DownloadEndpoint;
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
import nl.kb.dare.model.oai.OaiRecordQueryFactory;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import nl.kb.dare.model.oai.oracle.OracleOaiRecordDao;
import nl.kb.dare.model.oai.oracle.OracleStatusAggregator;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryNotifier;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.model.repository.oracle.OracleRepositoryDao;
import nl.kb.dare.oai.IndexMetadataTask;
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
        final HttpFetcher downloader = new LenientHttpFetcher(false);
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();
        final RepositoryDao repositoryDao = config.getDatabaseProvider().equals("oracle")
                ? db.onDemand(OracleRepositoryDao.class)
                : db.onDemand(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = new RepositoryNotifier();
        final RepositoryValidator repositoryValidator = new RepositoryValidator(httpFetcher, responseHandlerFactory);


        final ErrorReportDao errorReportDao = db.onDemand(ErrorReportDao.class);
        final OaiRecordDao oaiRecordDao =  config.getDatabaseProvider().equals("oracle")
                ? db.onDemand(OracleOaiRecordDao.class)
                : db.onDemand(OaiRecordDao.class);
        final OaiRecordQueryFactory oaiRecordQueryFactory = new OaiRecordQueryFactory(config.getDatabaseProvider());
        final FileStorage fileStorage = config.getFileStorageFactory().getFileStorage();
        final FileStorage sampleFileStorage = config.getFileStorageFactory().sampleFileStorage();
        final StreamSource stripOaiXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/strip_oai_wrapper.xsl"));
        final StreamSource didlToManifestXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/didl-to-manifest.xsl"));

        final OaiRecordStatusAggregator oaiRecordStatusAggregator = config.getDatabaseProvider().equals("oracle")
                ? new OracleStatusAggregator(db, oaiRecordQueryFactory)
                : new OaiRecordStatusAggregator(db, oaiRecordQueryFactory);

        final PipedXsltTransformer xsltTransformer = PipedXsltTransformer.newInstance(stripOaiXslt, didlToManifestXslt);
        final PipedXsltTransformer indexTransformer = PipedXsltTransformer.newInstance(
                new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/oai-to-index.xsl"))
        );

        final ScheduledOaiHarvester oaiHarvester = new ScheduledOaiHarvester(
                repositoryDao, errorReportDao, oaiRecordDao, httpFetcher, responseHandlerFactory, fileStorage,
                repositoryNotifier);

        final ScheduledOaiRecordFetcher oaiRecordFetcher = new ScheduledOaiRecordFetcher(
                oaiRecordDao, repositoryDao, errorReportDao, downloader, responseHandlerFactory, fileStorage, xsltTransformer,
                oaiRecordStatusAggregator, config.getInSampleMode());
        final StatusUpdater statusUpdater = new StatusUpdater(oaiRecordStatusAggregator,
                oaiHarvester, oaiRecordFetcher, repositoryDao, repositoryNotifier);


        environment.lifecycle().manage(new ManagedPeriodicTask(oaiRecordFetcher));

        environment.lifecycle().manage(new ManagedPeriodicTask(oaiHarvester));

        environment.lifecycle().manage(new ManagedPeriodicTask(statusUpdater));


        register(environment, new OaiRecordsEndpoint(db, oaiRecordDao, errorReportDao, oaiRecordQueryFactory,
                fileStorage, repositoryDao, downloader, responseHandlerFactory, xsltTransformer, sampleFileStorage));

        register(environment, new DownloadEndpoint(oaiRecordDao, fileStorage));

        register(environment, new RepositoriesEndpoint(repositoryDao, oaiRecordDao, errorReportDao, repositoryValidator,
                repositoryNotifier, fileStorage));

        register(environment, new OaiHarvesterEndpoint(oaiHarvester));
        register(environment, new OaiRecordFetcherEndpoint(oaiRecordFetcher));
        register(environment, new RootEndpoint(config.getAppTitle(), config.getHostName(), config.getWsProtocol()));

        registerServlet(environment, new StatusWebsocketServlet(), "statusWebsocket");

        environment.admin().addTask(new IndexMetadataTask(repositoryDao, httpFetcher, responseHandlerFactory, indexTransformer));
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }


    private void registerServlet(Environment environment, Servlet servlet, String name) {
        environment.servlets().addServlet(name, servlet).addMapping("/status-socket");
    }
}
