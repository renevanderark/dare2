package nl.kb.dare.integration.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.dare.integration.IntegrationTest;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.repository.Repository;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

public class CrudOperations {
    public static boolean startRecordProcessor() throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPut httpPut = new HttpPut(String.format("%s/workers/start", IntegrationTest.APP_URL));
        httpPut.addHeader("Accept", "application/json");

        final HttpResponse response = httpClient.execute(httpPut);
        return response.getStatusLine().getStatusCode() == 200;    }

    public static boolean startHarvester() throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPut httpPut = new HttpPut(String.format("%s/harvesters/start", IntegrationTest.APP_URL));
        httpPut.addHeader("Accept", "application/json");

        final HttpResponse response = httpClient.execute(httpPut);
        return response.getStatusLine().getStatusCode() == 200;
    }

    public static boolean enableRepository(String locationOfNewlyCreatedRepository) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpPut httpPut = new HttpPut(String.format("%s/%s", locationOfNewlyCreatedRepository, "enable"));
        final HttpResponse response = httpClient.execute(httpPut);
        return response.getStatusLine().getStatusCode() == 200;
    }

    public static String createRepository(Repository repository) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost httpPost = new HttpPost(String.format("%s/repositories", IntegrationTest.APP_URL));
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(repository)));
        final HttpResponse httpResponse = httpClient.execute(httpPost);

        return httpResponse.getFirstHeader("Location").getValue();
    }

    public static OaiRecordResult getRecords() throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpGet httpGet = new HttpGet(String.format("%s/records", IntegrationTest.APP_URL));
        httpGet.addHeader("Accept", "application/json");
        final HttpResponse response = httpClient.execute(httpGet);

        final String data = IOUtils.toString(response.getEntity().getContent(), "UTF8");

        return new ObjectMapper().readValue(data, OaiRecordResult.class);
    }

    public static InputStream download(String identifier) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpGet httpGet = new HttpGet(String.format("%s/records/%s/download", IntegrationTest.APP_URL,
                URLEncoder.encode(identifier, "UTF8")));
        final HttpResponse response = httpClient.execute(httpGet);

        return response.getEntity().getContent();
    }
}
