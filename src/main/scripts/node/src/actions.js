import ActionTypes from "./action-types";

import {
    startOaiRecordFetcher,
    disableOaiRecordFetcher,
    disableOaiHarvester,
    startOaiHarvester
} from "./actions/worker-controls";

import {
    enableRepository,
    disableRepository
} from "./actions/repositories";

import {
    setRecordQueryFilter,
    setRecordQueryOffset,
    fetchOaiRecords,
    fetchOaiRecord
} from "./actions/oai-records";

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onStartOaiHarvester: () => dispatch(startOaiHarvester()),
        onDisableOaiHarvester: () => dispatch(disableOaiHarvester()),
        onStartOaiRecordFetcher: () => dispatch(startOaiRecordFetcher()),
        onDisableOaiRecordFetcher: () => dispatch(disableOaiRecordFetcher()),

        onTogglePanelCollapse: (panelId) => dispatch({type: ActionTypes.ON_TOGGLE_PANEL_COLLAPSE, id: panelId}),

        onEnableRepository: (id) => dispatch(enableRepository(id)),
        onDisableRepository: (id) => dispatch(disableRepository(id)),

        onSetRecordQueryFilter: (field, value, repositoryId = null) => {
            dispatch(setRecordQueryFilter(field, value, repositoryId));
            dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "oai-records-panel"});
            if (field === "processStatus" && value === "failure") {
                dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "error-panel"});
            }
            if (field === "errorStatus") {
                dispatch(setRecordQueryFilter("processStatus", "failure", repositoryId));
            }
        },

        onSetRecordQueryOffset: (newOffset) => dispatch(setRecordQueryOffset(newOffset)),

        onRefetchRecords: () => dispatch(fetchOaiRecords()),

        onFetchOaiRecord: (identifier) => {
            dispatch(fetchOaiRecord(identifier));
            dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "oai-record-panel"})

        }
    };
}