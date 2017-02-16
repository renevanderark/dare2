import React from "react";
import {Router, Route, IndexRoute, browserHistory} from "react-router";
import {Provider, connect} from "react-redux";

import store from "./store";
import actions from "./actions";

import App from "./components/app";
import ActionTypes from "./action-types";

const webSocket = new WebSocket(globals.wsProtocol + "://" + globals.hostName + "/status-socket");

webSocket.onmessage = ({ data }) => store.dispatch({type: ActionTypes.ON_STATUS_UPDATE, data: JSON.parse(data)});


const urls = {
    root() {
        return "/";
    }
};

const navigateTo = (key, args) => browserHistory.push(urls[key].apply(null, args));

const defaultConnector = (state) => state;
const connectComponent = (stateToProps) => connect(stateToProps, dispatch => actions(navigateTo, dispatch, webSocket));

export default (
<Provider store={store}>
    <Router history={browserHistory}>
    <Route path={urls.root()} component={connectComponent(defaultConnector)(App)}>
</Route>
</Router>
</Provider>
);

/*
var webSocket = new WebSocket(globals.wsProtocol + "://" + globals.hostName + "/status-socket");

function getTotals(data) {
    var recordTotals = {};

    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            for (var status in data[k]) {
                recordTotals[status] = recordTotals[status] || 0;
                recordTotals[status] += data[k][status];
            }
        }
    }
    return recordTotals;
}
webSocket.onmessage = function (payload) {
    var data = JSON.parse(payload.data);
    var recordTotals = getTotals(data["record processing"].recordStatus);
    var errorTotals = getTotals(data["record processing"].errorStatus);
    document.getElementById("status").innerHTML = JSON.stringify({
        "HELLO": data,
        totals: {
            records: recordTotals,
            errors: errorTotals
        }
    }, null, 4);
};

function pingWs() {
    webSocket.send("* ping! *");
    window.setTimeout(pingWs, 8000);
}

webSocket.onopen = pingWs;*/
