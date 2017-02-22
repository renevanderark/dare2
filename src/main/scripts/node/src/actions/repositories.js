import xhr from "xhr";
import ActionTypes from "../action-types";

const toggleRepositoryAndFetch = (id, operation, next = () => {}) => (dispatch) =>
    xhr({url: `/repositories/${id}/${operation}`, method: "PUT"}, next);


const enableRepository = (id, next) => toggleRepositoryAndFetch(id, "enable", next);

const disableRepository = (id, next) => toggleRepositoryAndFetch(id, "disable", next);

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