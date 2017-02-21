const rootConnector = (state, routed) => ({

    oaiRecord: state.oaiRecords.current,
    identifier: routed.params.identifier,
    collapsed: state.panels["oai-record-panel"].collapsed
});

export default rootConnector;