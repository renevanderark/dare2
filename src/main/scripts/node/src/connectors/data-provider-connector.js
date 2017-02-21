const dataProviderConnector = (state, routed) => {

    const { repositories } = state;
    const { recordProcessingStatus } = state.status.status || {};
    const { recordStatus, errorStatus } = recordProcessingStatus || {};

    const set = ((repositories.list || []).find((repo) => "" + repo.id === routed.params.id) || {}).set;

    return {
        workflow: {
            ...(recordStatus || {})[set],
            collapsed: state.panels["workflow-panel"].collapsed
        },
        errors: {
            ...(errorStatus || {})[set],
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