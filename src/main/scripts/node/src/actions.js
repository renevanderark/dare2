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

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onStartOaiHarvester: () => dispatch(startOaiHarvester()),
        onDisableOaiHarvester: () => dispatch(disableOaiHarvester()),
        onStartOaiRecordFetcher: () => dispatch(startOaiRecordFetcher()),
        onDisableOaiRecordFetcher: () => dispatch(disableOaiRecordFetcher()),

        onTogglePanelCollapse: (panelId) => dispatch({type: ActionTypes.ON_TOGGLE_PANEL_COLLAPSE, id: panelId}),

        onEnableRepository: (id) => dispatch(enableRepository(id)),
        onDisableRepository: (id) => dispatch(disableRepository(id))
    };
}