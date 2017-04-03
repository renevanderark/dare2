package nl.kb.dare.model.repository.oracle;

import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryMapper;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(RepositoryMapper.class)
public interface OracleRepositoryDao extends RepositoryDao {
    @SqlUpdate("insert into repositories (id, name, url, metadataPrefix, oai_set, datestamp) " +
            "values (repositories_seq.nextval, :name, :url, :metadataPrefix, :set, :dateStamp)")
    Integer insert(@BindBean Repository repositoryConfig);
}
