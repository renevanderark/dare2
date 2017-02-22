import xhr from "xhr";
import ActionTypes from "../action-types";

const toggleRepositoryAndFetch = (id, operation) => (dispatch) =>
    xhr({url: `/repositories/${id}/${operation}`, method: "PUT"}, () => {});


const enableRepository = (id) => toggleRepositoryAndFetch(id, "enable");

const disableRepository = (id) => toggleRepositoryAndFetch(id, "disable");

const fetchRepository = (id) => (dispatch) =>
    xhr({url: `/repositories/${id}`, method: "GET"}, (err, resp, body) =>
        dispatch({type: ActionTypes.RECEIVE_DATA_PROVIDER, data: JSON.parse(body)}));

const validateRepository = (id) => (dispatch) =>
    xhr({url: `/repositories/${id}/validate`, method: "GET"}, (err, resp, body) =>
        dispatch({type: ActionTypes.RECEIVE_REPOSITORY_VALIDATION_RESULTS, data: JSON.parse(body)}));

export  {
    enableRepository,
    disableRepository,
    fetchRepository,
    validateRepository
};