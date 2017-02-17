import xhr from "xhr";
import ActionTypes from "../action-types";

const toggleRepositoryAndFetch = (id, operation) => (dispatch) => {
    xhr({
        url: `/repositories/${id}/${operation}`,
        method: "PUT"
    }, () => {
        xhr({
            url: "/repositories",
            method: "GET"
        }, (err, resp, body) => dispatch({type: ActionTypes.RECEIVE_REPOSITORIES, data: JSON.parse(body)}))
    })
};

const enableRepository = (id) => toggleRepositoryAndFetch(id, "enable");

const disableRepository = (id) => toggleRepositoryAndFetch(id, "disable");

export  {
    enableRepository,
    disableRepository
};