import dao.RepositoryDao;
import endpoints.RepositoriesEndpoint;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
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

        register(environment, new RepositoriesEndpoint(repositoryDao));
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
