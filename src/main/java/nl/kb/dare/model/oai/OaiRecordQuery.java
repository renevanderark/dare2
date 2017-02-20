package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.LongMapper;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class OaiRecordQuery {
    private final Integer repositoryId;
    private final Integer offset;
    private final Integer limit;
    private final ProcessStatus processStatus;
    private final OaiStatus oaiStatus;

    public OaiRecordQuery(Integer repositoryId, Integer offset, Integer limit, ProcessStatus processStatus,
                          OaiStatus oaiStatus) {

        this.repositoryId = repositoryId;
        this.offset = offset;
        this.limit = limit;
        this.processStatus = processStatus;
        this.oaiStatus = oaiStatus;
    }

    @JsonProperty
    public Integer getRepositoryId() {
        return repositoryId;
    }

    @JsonProperty
    public Integer getOffset() {
        return offset;
    }

    @JsonProperty
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty
    public String getProcessStatus() {
        return processStatus != null ? processStatus.getStatus() : "<< no supported parameter defined >>";
    }

    @JsonProperty
    public String getOaiStatus() {
        return oaiStatus != null ? oaiStatus.getStatus() : "<< no supported parameter defined >>";
    }

    @JsonIgnore
    public List<OaiRecord> getResults(DBI dbi) {
        try (final Handle h = dbi.open()) {
            return new Builder(h, "select * from oai_records")
                    .withFilter("repository_id", repositoryId)
                    .withFilter("process_status_code", processStatus)
                    .withFilter("oai_status_code", oaiStatus)
                    .withLimit(limit)
                    .withOffset(offset)
                    .build()
                    .getResults();
        }
    }

    @JsonIgnore
    public Long getCount(DBI dbi) {
        try (final Handle h = dbi.open()) {
            return new Builder(h, "select count(*) from oai_records")
                    .withFilter("repository_id", repositoryId)
                    .withFilter("process_status_code", processStatus)
                    .withFilter("oai_status_code", oaiStatus)
                    .build()
                    .getCount();
        }
    }

    private class Builder {
        private final Map<String, Object> filters = Maps.newLinkedHashMap();
        private final String selectClause;
        private final Handle h;
        private Integer limit = null;
        private Integer offset = null;
        private Query<Map<String, Object>> query;

        private Builder(Handle h, String selectClause) {
            this.h = h;
            this.selectClause = selectClause;
        }

        Builder withFilter(String field, Object filterValue) {
            if (filterValue != null) {
                if (filterValue instanceof ProcessStatus) {
                    filters.put(field, ((ProcessStatus) filterValue).getCode());
                } else if (filterValue instanceof OaiStatus) {
                    filters.put(field, ((OaiStatus) filterValue).getCode());
                } else {
                    filters.put(field, filterValue);
                }
            }
            return this;
        }

        Builder withLimit(Integer limit) {
            if (limit != null) {
                this.limit = limit;
            }
            return this;
        }

        Builder withOffset(Integer offset) {
            if (offset != null) {
                this.offset = offset;
            }
            return this;
        }

        Builder build() {
            final StringBuilder sb = new StringBuilder(selectClause);

            if (filters.size() > 0) {
                sb.append(" where ");
            }

            sb.append(filters.keySet()
                    .stream()
                    .map(field -> String.format("%s = :%s", field, field))
                    .collect(joining(" and ")));

            if (this.limit != null) {
                sb.append(" limit :limit");
            }
            if (this.offset != null) {
                sb.append(" offset :offset");
            }

            query = h.createQuery(sb.toString());
            filters.keySet().forEach(key -> query.bind(key, filters.get(key)));

            if (this.limit != null) {
                query.bind("limit", limit);
            }
            if (this.offset != null) {
                query.bind("offset", offset);
            }

            return this;
        }

        List<OaiRecord> getResults() {
            return query.map(new OaiRecordMapper()).list();
        }

        Long getCount() {
            return query.map(LongMapper.FIRST).first();
        }
    }
}
