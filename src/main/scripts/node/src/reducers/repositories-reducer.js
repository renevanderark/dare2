import ActionTypes from "../action-types";

const initialState = {
    list: [],
    current: null,
    validationResults: {}
};


export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.ON_STATUS_UPDATE:
            return {
                ...state,
                list: action.data.repositoryStatus
            };
        case ActionTypes.RECEIVE_DATA_PROVIDER:
            return {
                ...state,
                current: action.data,
                validationResults: {}
            };
        case ActionTypes.RECEIVE_REPOSITORY_VALIDATION_RESULTS:
            return {
                ...state,
                validationResults: action.data
            };
        default:
    }

    return state;
}