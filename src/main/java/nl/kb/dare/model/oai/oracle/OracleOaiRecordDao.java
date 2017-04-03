package nl.kb.dare.model.oai.oracle;

import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(OaiRecordMapper.class)
public interface OracleOaiRecordDao extends OaiRecordDao {

    @SqlQuery("select * from oai_records where process_status_code = :process_status_code AND repository_id = :repository_id AND ROWNUM <= :limit")
    List<OaiRecord> fetchNextWithProcessStatusByRepositoryId(
            @Bind("process_status_code") Integer processStatusCode,
            @Bind("limit") Integer limit,
            @Bind("repository_id") Integer repositoryId);
}
