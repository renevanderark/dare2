import ActionTypes from "../action-types";

const initialState = {
    list: []
};


export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.ON_STATUS_UPDATE:
            return {
                ...state,
                list: action.data.repositoryStatus
            };
        default:
    }

    return state;
}