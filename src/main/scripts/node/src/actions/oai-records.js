import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchOaiRecords = (newQuery = null) => (dispatch, getState) => {
    const query = newQuery || getState().oaiRecords.query;

    const queryS = Object.keys(query)
        .filter((key) => query[key] !== null)
        .map((key) => `${key}=${query[key]}`)
        .join("&");

    xhr({
        url: `/records?${new Date().getTime()}${queryS}`,
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
        url: `/records/${encodeURIComponent(identifier)}?${new Date().getTime()}`,
        method: "GET"
    }, (err, resp, body) => dispatch({type: ActionTypes.RECEIVE_OAI_RECORD, data: JSON.parse(body)}));
};

const resetOaiRecord = (identifier) => (dispatch) => {
    xhr({
        url: `/records/${encodeURIComponent(identifier)}/reset?${new Date().getTime()}`,
        method: "PUT"
    }, (err, resp, body) => dispatch({type: ActionTypes.RECEIVE_OAI_RECORD, data: JSON.parse(body)}));
};

const testOaiRecord = (identifier) => (dispatch) => {
    const url = `/records/${encodeURIComponent(identifier)}/test?${new Date().getTime()}`;
    const req = new XMLHttpRequest();

    dispatch({type: ActionTypes.SET_RECORD_TEST_PENDING, identifier: identifier});
    req.open('GET', url);

    const parseJson = (chunk) => {
        try {
            return JSON.parse(chunk);
        } catch (e) {
            return null;
        }
    };

    req.onreadystatechange = function() {
        const payload = req.responseText;
        dispatch({
            type: ActionTypes.UPDATE_RECORD_TEST_RESULTS,
            payload: payload
                .split("!--end-chunk--!")
                .map(chunk => parseJson(chunk))
                .filter(x => x !== null),
            identifier: identifier
        });
    };

    req.onload = function() {
        dispatch({type: ActionTypes.SET_RECORD_TEST_DONE, identifier: identifier});
    };

    req.send();
};

export { fetchOaiRecords, setRecordQueryFilter, setRecordQueryOffset, fetchOaiRecord, testOaiRecord, resetOaiRecord }


