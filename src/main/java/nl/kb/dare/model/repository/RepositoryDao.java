package nl.kb.dare.model.repository;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(RepositoryMapper.class)
public interface RepositoryDao {

    @SqlUpdate("insert into repositories (url, metadataPrefix, oai_set, datestamp) " +
            "values (:url, :metadataPrefix, :set, :dateStamp)")
    @GetGeneratedKeys
    Integer insert(@BindBean Repository repositoryConfig);

    @SqlQuery("select id, url, metadataPrefix, oai_set, datestamp from repositories where id = :id")
    Repository findById(@Bind("id") int id);

    @SqlUpdate("delete from repositories where id = :id")
    void remove(@Bind("id") int id);

    @SqlUpdate("update repositories " +
            "set url=:r.url, metadataPrefix=:r.metadataPrefix, oai_set=:r.set, datestamp=:r.dateStamp " +
            "where id = :id")
    void update(@Bind("id") Integer id, @BindBean("r") Repository repositoryConfig);
}
