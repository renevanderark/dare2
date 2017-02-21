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

        onSetRecordQueryFilter: (field, value) => {
            dispatch(setRecordQueryFilter(field, value));
            dispatch({type: ActionTypes.ON_OPEN_PANEL, id: "oai-records-panel"})
        },

        onSetRecordQueryOffset: (newOffset) => dispatch(setRecordQueryOffset(newOffset)),

        onRefetchRecords: () => dispatch(fetchOaiRecords()),

        onFetchOaiRecord: (identifier) => dispatch(fetchOaiRecord(identifier))
    };
}