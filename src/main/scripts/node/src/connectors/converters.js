const convertRecords = (state) => {
    const repositories = state.repositories.list || [];

    const query = Object.keys(state.oaiRecords.query)
        .filter((key) => state.oaiRecords.query[key] !== null && key !== 'limit' && key !== 'offset')
        .map((key) => ({
            key: key,
            label: key === "repositoryId"
                ? (repositories.find(repo => "" + repo.id === state.oaiRecords.query[key]) || {}).name
                : state.oaiRecords.query[key]
        }));

    return {
        results: {
            count: state.oaiRecords.results.count,
            result: (state.oaiRecords.results.result || []).map(record => ({
                ...record,
                repositoryName: (repositories.find(repo => repo.id === record.repositoryId) || {}).name
            }))
        },
        query: state.oaiRecords.query,
        labeledQuery: query,
        collapsed: state.panels["oai-records-panel"].collapsed
    };
};

export { convertRecords}