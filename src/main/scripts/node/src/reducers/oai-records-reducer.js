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
    current: null,
    testResults: null
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
                testResults: null,
                current: action.data
            };

        case ActionTypes.SET_RECORD_TEST_PENDING:
            return {
                ...state,
                testResults: action.identifier === (state.current.record || {}).identifier
                    ? {pending : true}
                    : null
            };
        case ActionTypes.UPDATE_RECORD_TEST_RESULTS:
            return {
                ...state,
                testResults: action.identifier === (state.current.record || {}).identifier
                    ? {pending : true, results: action.payload}
                    : null
            };
        case ActionTypes.SET_RECORD_TEST_DONE:
            return {
                ...state,
                testResults: action.identifier === (state.current.record || {}).identifier
                    ? {...state.testResults, pending : false}
                    : null
            };
        default:
    }

    return state;
}