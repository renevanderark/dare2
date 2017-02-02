package nl.kb.dare.endpoints;

import com.google.common.collect.Lists;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoriesEndpointTest {

    @Test
    public void createShouldCreateANewRpository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao);
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "setname", "123");
        final Integer id = 123;
        when(dao.insert(repositoryConfig)).thenReturn(id);

        final Response response = instance.create(repositoryConfig);

        verify(dao).insert(repositoryConfig);
        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getHeaderString("Location"), equalTo("/repositories/" + id));

    }

    @Test
    public void deleteShouldDeleteTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao);
        final Integer id = 123;

        final Response response = instance.delete(id);

        verify(dao).remove(id);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void getShouldReturnTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao);
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "setname", "123");
        final Integer id = 123;
        when(dao.findById(id)).thenReturn(repositoryConfig);

        final Response response = instance.get(id);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositoryConfig));
    }

    @Test
    public void getShouldReturnNotFoundWhenRepositoryIsNotFound() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao);
        final Integer id = 123;
        when(dao.findById(id)).thenReturn(null);

        final Response response = instance.get(id);

        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        assertThat(response.getEntity(), hasProperty("message", equalTo("repository not found with id: 123")));
        assertThat(response.getEntity(), hasProperty("code", equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }

    @Test
    public void updateShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao);
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "setname", "123");
        final Integer id = 123;

        final Response response = instance.update(id, repositoryConfig);

        verify(dao).update(id, repositoryConfig);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void indexShouldRespondWithAListOfRepositories() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao);
        final Repository repositoryConfig1 = new Repository("http://example.com", "prefix", "setname", "123", 1);
        final Repository repositoryConfig2 = new Repository("http://example.com", "prefix", "setname", "123", 2);
        final List<Repository> repositories = Lists.newArrayList(repositoryConfig1, repositoryConfig2);

        when(dao.list()).thenReturn(repositories);
        final Response response = instance.index();

        verify(dao).list();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositories));
    }

}