import ActionTypes from "../action-types";

const initialState = {
    list: []
};


export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.RECEIVE_REPOSITORIES:
            return {
                ...state,
                list: action.data
            };
        default:
    }

    return state;
}