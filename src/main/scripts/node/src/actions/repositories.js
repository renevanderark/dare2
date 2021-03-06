import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchRepositories = (next = () => {}) => (dispatch) =>
    xhr({url: `/repositories?${new Date().getTime()}`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_REPOSITORIES, data: JSON.parse(body)});
        next();
    });

const toggleRepositoryAndFetch = (id, operation, next = () => {}) => (dispatch) =>
    xhr({url: `/repositories/${id}/${operation}`, method: "PUT"}, next);

const enableRepository = (id, next) => toggleRepositoryAndFetch(id, "enable", next);

const disableRepository = (id, next) => toggleRepositoryAndFetch(id, "disable", next);

const fetchRepository = (id, next = () => {}) => (dispatch) =>
    xhr({url: `/repositories/${id}?${new Date().getTime()}`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_DATA_PROVIDER, data: JSON.parse(body)});
        next()
    });

const validateRepository = (id) => (dispatch) =>
    xhr({url: `/repositories/${id}/validate?${new Date().getTime()}`, method: "GET"}, (err, resp, body) =>
        dispatch({type: ActionTypes.RECEIVE_REPOSITORY_VALIDATION_RESULTS, data: JSON.parse(body)}));

const saveRepository = (next) => (dispatch, getState) => {
    const { underEdit } = getState().repositories;
    if (!underEdit) {
        return;
    }

    xhr({
        url: underEdit.id ? `/repositories/${underEdit.id}` : `/repositories`,
        method: underEdit.id ? "PUT" : "POST",
        headers: { 'Content-type': "application/json", 'Accept': 'application/json'},
        body: JSON.stringify(underEdit)
    }, (err, resp, body) => {
        const savedRepository = JSON.parse(body);
        dispatch({type: ActionTypes.RECEIVE_DATA_PROVIDER, data: savedRepository});
        next(savedRepository.id)
    });
};

const deleteRepository = (id, next = () => {}) => (dispatch) => {
    xhr({
        url: `/repositories/${id}`,
        method: "DELETE"
    }, next);
};

const validateNewRepository = (repository) => (dispatch) =>
    xhr({
        url: `/repositories/validate`,
        method: "POST",
        headers: { "Content-type": "application/json", "Accept": "application/json" },
        body: JSON.stringify(repository)
    }, (err, resp, body) => {
        if (resp.statusCode > 299) {
            dispatch({
                type: ActionTypes.RECEIVE_NEW_REPOSITORY_VALIDATION_RESULTS,
                underEdit: repository,
                data: {
                    urlIsValidOAI: false,
                    setExists: undefined,
                    metadataFormatSupported: undefined
                }
            })
        } else {
            dispatch({
                type: ActionTypes.RECEIVE_NEW_REPOSITORY_VALIDATION_RESULTS,
                underEdit: repository,
                data: {
                    ...JSON.parse(body),
                    urlIsValidOAI: true
                }
            })
        }
    });


export {
    fetchRepositories,
    enableRepository,
    disableRepository,
    fetchRepository,
    validateRepository,
    validateNewRepository,
    saveRepository,
    deleteRepository
};