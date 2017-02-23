import store from "./store";
import ActionTypes from "./action-types";
import {fetchOaiRecords } from "./actions/oai-records";

let initialized = false;
const initialize = (onInitialize) => {
    store.dispatch(fetchOaiRecords());
    initialized = true;
    onInitialize();
};

// Use a web socket to get status updates
const connectSocket = (onInitialize) => {

    const webSocket = new WebSocket(globals.wsProtocol + "://" + globals.hostName + "/status-socket");

    webSocket.onmessage = ({data}) => {
        store.dispatch({type: ActionTypes.ON_STATUS_UPDATE, data: JSON.parse(data)});
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