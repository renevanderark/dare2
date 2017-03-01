import ActionTypes from "./action-types";

import {
    startOaiRecordFetcher,
    disableOaiRecordFetcher,
    disableOaiHarvester,
    startOaiHarvester
} from "./actions/worker-controls";

import {
    enableRepository,
    disableRepository,
    fetchRepository,
    validateRepository,
    validateNewRepository,
    saveRepository,
    deleteRepository
} from "./actions/repositories";

import {
    setRecordQueryFilter,
    setRecordQueryOffset,
    fetchOaiRecords,
    fetchOaiRecord,
    testOaiRecord
} from "./actions/oai-records";

import {urls} from "./router"

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onStartOaiHarvester: () => dispatch(startOaiHarvester()),
        onDisableOaiHarvester: () => dispatch(disableOaiHarvester()),
        onStartOaiRecordFetcher: () => dispatch(startOaiRecordFetcher()),
        onDisableOaiRecordFetcher: () => dispatch(disableOaiRecordFetcher()),

        onTogglePanelCollapse: (panelId) => dispatch({type: ActionTypes.ON_TOGGLE_PANEL_COLLAPSE, id: panelId}),

        onEnableRepository: (id, next = () => {}) => dispatch(enableRepository(id, next)),
        onDisableRepository: (id, next = () => {}) => dispatch(disableRepository(id, next)),
        onValidateRepository: (id) => dispatch(validateRepository(id)),
        onValidateNewRepository: (repository) => dispatch(validateNewRepository(repository)),
        onSaveRepository: () => dispatch(saveRepository((id) => navigateTo("dataProvider", [id]))),

        onFetchDataProvider: (id) => dispatch(fetchRepository(id)),
        onDeleteDataProvider: (id) => dispatch(deleteRepository(id, () => navigateTo("root", []))),

        onSetRecordQueryFilter: (field, value, repositoryId = null) => {
            dispatch(setRecordQueryFilter(field, value, repositoryId));
            dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "oai-records-panel"});
            if (field === "processStatus" && value === "failure") {
                dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "error-panel"});
            }
            if (field === "errorStatus" && value !== null) {
                dispatch(setRecordQueryFilter("processStatus", "failure", repositoryId));
            }
        },

        onSetRecordQueryOffset: (newOffset) => dispatch(setRecordQueryOffset(newOffset)),

        onRefetchRecords: () => dispatch(fetchOaiRecords()),

        onFetchOaiRecord: (identifier) => {
            dispatch(fetchOaiRecord(identifier));
            dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "oai-record-panel"})
        },
        onTestRecord: (identifier) => dispatch(testOaiRecord(identifier))
    };
}