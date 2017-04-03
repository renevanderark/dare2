package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import java.io.PrintWriter;

public class LoadRepositoriesTask extends Task {
    private final RepositoryDao repositoryDao;

    public LoadRepositoriesTask(RepositoryDao repositoryDao) {
        super("load-repositories");
        this.repositoryDao = repositoryDao;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        Lists.newArrayList(
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Utrecht", "nl_didl_norm", "uu:dare", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Nijmegen", "nl_didl_norm", "ru:col_2066_13799", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Groningen", "nl_didl_norm", "rug", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Delft", "nl_didl_norm", "tud:A-set", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Leiden", "nl_didl_norm", "ul:hdl_1887_4539", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Maastricht", "nl_didl_norm", "um", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Twente", "nl_didl_norm", "ut:66756C6C746578743D7075626C6963", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "UvA", "nl_didl_norm", "uva:withfulltext:yes", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Tilburg", "nl_didl_norm", "uvt:withfulltext:yes", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "VU", "nl_didl_norm", "vu", null, false),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Wageningen", "nl_didl_norm", "wur:publickb", null, false)
        ).forEach(this.repositoryDao::insert);
    }
}
