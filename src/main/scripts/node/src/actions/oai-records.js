import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchOaiRecords = () => (dispatch, getState) => {
    const query = getState().oaiRecords.query;

    const queryS = Object.keys(query)
        .filter((key) => query[key] !== null)
        .map((key) => `${key}=${query[key]}`)
        .join("&");

    xhr({
        url: `/records?${queryS}`,
        method: "GET",
    }, (err, resp, body) => dispatch({type: ActionTypes.RECEIVE_OAI_RECORDS, data: JSON.parse(body)}));
};

export { fetchOaiRecords }


