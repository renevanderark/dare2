package nl.kb.dare.oai;

import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.RepositoryDao;

// FIXME: should be a scheduled task
public class OaiTaskRunner implements Runnable {

    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private boolean isEnabled = false;

    public OaiTaskRunner(RepositoryDao repositoryDao, HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }



    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public void run() {
        while (true) {
            if (isEnabled) {
                repositoryDao.list()
                        .stream()
                        .map(repo -> new ListIdentifiers(repo, httpFetcher, responseHandlerFactory,
                                (repoDone) -> repositoryDao.update(repoDone.getId(), repoDone),
                                Throwable::printStackTrace))
                        .forEach(ListIdentifiers::harvest);
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
