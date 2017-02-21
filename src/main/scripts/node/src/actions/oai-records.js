import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchOaiRecords = (newQuery = null) => (dispatch, getState) => {
    const query = newQuery || getState().oaiRecords.query;

    const queryS = Object.keys(query)
        .filter((key) => query[key] !== null)
        .map((key) => `${key}=${query[key]}`)
        .join("&");

    xhr({
        url: `/records?${queryS}`,
        method: "GET",
    }, (err, resp, body) => dispatch({type: ActionTypes.RECEIVE_OAI_RECORDS, data: JSON.parse(body)}));
};

const setRecordQueryFilter = (field, value, repositoryId = null) => (dispatch, getState) => {
    const query = repositoryId
        ? {
            ...getState().oaiRecords.query,
            [field] : value,
            offset: 0,
            repositoryId: repositoryId
        } : {
            ...getState().oaiRecords.query,
            [field] : value,
            offset: 0
        };
    dispatch({type: ActionTypes.SET_OAI_RECORD_QUERY, query: query});
    dispatch(fetchOaiRecords(query));
};

const setRecordQueryOffset = (newOffset) => (dispatch, getState) => {
    const query = {
        ...getState().oaiRecords.query,
        offset: newOffset
    };
    dispatch({type: ActionTypes.SET_OAI_RECORD_QUERY, query: query});
    dispatch(fetchOaiRecords(query));
};

const fetchOaiRecord = (identifier) => (dispatch) => {
    xhr({
        url: `/records/${encodeURIComponent(identifier)}`,
        method: "GET"
    }, (err, resp, body) => dispatch({type: ActionTypes.RECEIVE_OAI_RECORD, data: JSON.parse(body)}));
};

export { fetchOaiRecords, setRecordQueryFilter, setRecordQueryOffset, fetchOaiRecord }


