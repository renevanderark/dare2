import store from "./store";
import ActionTypes from "./action-types";
import {fetchOaiRecords } from "./actions/oai-records";
import {fetchRepositories} from "./actions/repositories";

let initialized = false;
const initialize = (onInitialize) => {
    store.dispatch(fetchOaiRecords());
    store.dispatch(fetchRepositories());
    initialized = true;
    onInitialize();
};

// Use a web socket to get status updates
const connectSocket = (onInitialize) => {

    const webSocket = new WebSocket(globals.wsProtocol + "://" + globals.hostName + "/status-socket");

    webSocket.onmessage = ({data}) => {
        const status = JSON.parse(data);

        if (status.hasOwnProperty("repositoryStatus")) {
            store.dispatch({type: ActionTypes.ON_REPOSITORY_STATUS_UPDATE, data: status.repositoryStatus});
        }

        store.dispatch({type: ActionTypes.ON_STATUS_UPDATE, data: status});
        if (!initialized) {
            initialize(onInitialize);
        }
    };


    // Keep the websocket alive
    const pingWs = () => {
        webSocket.send("* ping! *");
        window.setTimeout(pingWs, 8000);
    };
    webSocket.onopen = pingWs;

    webSocket.onclose = () => {
        store.dispatch({type: ActionTypes.ON_SOCKET_CLOSED});
        window.setTimeout(connectSocket, 500);
    }
};

export { connectSocket };