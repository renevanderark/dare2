import ActionTypes from "../action-types";

const initialState = {
    "workers-panel": {collapsed: false},
    "workflow-panel": {collapsed: false}
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
        default:
    }

    return state;
}