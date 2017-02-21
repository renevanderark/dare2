const rootConnector = (state, routed) => ({

    identifier: routed.params.identifier,
    oaiRecord: {
        ...state.oaiRecords.current,
        collapsed: state.panels["oai-record-panel"].collapsed
    },
    records: {
        ...state.oaiRecords,
        repositories: state.repositories.list,
        collapsed: state.panels["oai-records-panel"].collapsed
    },
});

export default rootConnector;