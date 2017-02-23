const oaiRecordConnector = (state, routed) => {
    const oaiRecord = state.oaiRecords.current;
    const repositoryName = ((state.repositories.list || [])
        .find(repo => ((oaiRecord || {}).record || {}).repositoryId === repo.id) || {}).name;

    return {
        identifier: routed.params.identifier,
        oaiRecord: {
            ...oaiRecord,
            repositoryName: repositoryName,
            collapsed: state.panels["oai-record-panel"].collapsed
        },
        records: {
            ...state.oaiRecords,
            repositories: state.repositories.list || [],
            collapsed: state.panels["oai-records-panel"].collapsed
        },
    }
};

export default oaiRecordConnector;