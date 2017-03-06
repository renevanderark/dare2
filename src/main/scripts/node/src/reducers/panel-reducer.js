import ActionTypes from "../action-types";

const initialState = {
    "repositories-panel": {collapsed: false},
    "workers-panel": {collapsed: false},
    "workflow-panel": {collapsed: false},
    "oai-records-panel": {collapsed: true },
    "error-panel": {collapsed: true},
    "oai-record-panel": {collapsed: false },
    "data-provider-panel": {collapsed: false},
    "progress-panel": {collapsed: false}
};


export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.ON_TOGGLE_PANEL_COLLAPSE:
            return {
                ...state,
                [action.id]: {
                    ...state[action.id],
                    collapsed: !state[action.id].collapsed
                }
            };
        case ActionTypes.ON_OPEN_PANEL:
            return {
                ...state,
                [action.id]: {
                    ...state[action.id],
                    collapsed: false
                }
            };
        default:
    }

    return state;
}