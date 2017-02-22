const dataProviderConnector = (state, routed) => {

    const { repositories } = state;
    const { recordProcessingStatus } = state.status.status || {};
    const { recordStatus, errorStatus } = recordProcessingStatus || {};

    return {
        workflow: {
            ...(recordStatus || {})[routed.params.id],
            collapsed: state.panels["workflow-panel"].collapsed
        },
        errors: {
            ...(errorStatus || {})[routed.params.id],
            collapsed: state.panels["error-panel"].collapsed
        },
        records: {
            ...state.oaiRecords,
            repositories: state.repositories.list,
            collapsed: state.panels["oai-records-panel"].collapsed
        },
    }
};

export default dataProviderConnector;