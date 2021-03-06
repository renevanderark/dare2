import {convertRecords} from "./converters";
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
        records: convertRecords(state),
        dataProvider: {
            repository: state.repositories.current,
            underEdit:  state.repositories.underEdit,
            validationResults: state.repositories.validationResults,
            validationResultsUnderEdit: state.repositories.validationResultsUnderEdit,
            id: routed.params.id,
            collapsed: state.panels["data-provider-panel"].collapsed
        }
    }
};

export default dataProviderConnector;