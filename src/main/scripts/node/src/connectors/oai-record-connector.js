const rootConnector = (state, routed) => {
    const oaiRecord = state.oaiRecords.current;
    const repositorySet = ((state.repositories.list || [])
        .find(repo => oaiRecord.record.repositoryId === repo.id) || {}).set;

    return {
        identifier: routed.params.identifier,
        oaiRecord: {
            ...oaiRecord,
            repositorySet: repositorySet,
            collapsed: state.panels["oai-record-panel"].collapsed
        },
        records: {
        ...state.oaiRecords,
                repositories: state.repositories.list,
                collapsed: state.panels["oai-records-panel"].collapsed
        },
    }
};

export default rootConnector;