package nl.kb.dare.oai;

import com.google.common.collect.Sets;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ListIdentifiers {
    private final RepositoryDao repositoryDao;
    private Set<Harvester> harvesters = Sets.newHashSet();

    public ListIdentifiers(RepositoryDao repositoryDao) {
        this.repositoryDao = repositoryDao;
        initialize();
    }

    private void initialize() {
        harvesters = repositoryDao.list().stream().map(Harvester::new).collect(toSet());
    }

    public void harvestBatches() {
        for (Harvester harvester : harvesters) {
            harvester.harvestBatch();
        }
    }

    private class Harvester {
        private final String url;
        private final String set;
        private final String metadataPrefix;
        private final String from;
        private String resumptionToken = null;

        Harvester(Repository repository) {
            this.url = repository.getUrl();
            this.set = repository.getSet();
            this.metadataPrefix = repository.getMetadataPrefix();
            this.from = repository.getDateStamp();
        }

        void harvestBatch() {
            final StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(url).append("?").append("verb=ListIdentifiers");

            if (resumptionToken != null) {
                urlBuilder.append("&").append(String.format("resumptionToken=%s", resumptionToken));
            } else {
                urlBuilder
                        .append("&").append(String.format("set=%s", set))
                        .append("&").append(String.format("metadataPrefix=%s", metadataPrefix));

                if (from != null) {
                    urlBuilder.append("&").append(String.format("from=%s", from));
                }
            }


            System.out.println(urlBuilder.toString());
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
                final InputStream inputStream = connection.getInputStream();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.err.println("TODO: log error with data!");
                e.printStackTrace();
            }
        }
    }
}
