import ActionTypes from "../action-types";

const initialState = {
    list: [],
    current: null,
    underEdit: null,
    validationResults: {},
    validationResultsUnderEdit: {}
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
                validationResults: {},
                underEdit: null,
                validationResultsUnderEdit: {}
            };
        case ActionTypes.RECEIVE_REPOSITORY_VALIDATION_RESULTS:
            return {
                ...state,
                validationResults: action.data
            };
        case ActionTypes.RECEIVE_NEW_REPOSITORY_VALIDATION_RESULTS:
            return {
                ...state,
                underEdit: action.underEdit,
                validationResultsUnderEdit: action.data
            };
        default:
    }

    return state;
}