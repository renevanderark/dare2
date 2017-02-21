import ActionTypes from "../action-types";

const initialState = {
    query: {
        limit: 10,
        offset: 0,
        processStatus: null,
        repositoryId: null
    },
    results: {
        result: [],
        count: 0
    },
    current: null
};


export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.RECEIVE_OAI_RECORDS:
            return {
                ...state,
                results: action.data
            };
        case ActionTypes.SET_OAI_RECORD_QUERY:
            return {
                ...state,
                query: action.query
            };
        case ActionTypes.RECEIVE_OAI_RECORD:
            return {
                ...state,
                current: action.data
            }
        default:
    }

    return state;
}