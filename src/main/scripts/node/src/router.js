import React from "react";
import {Router, Route, IndexRoute, browserHistory} from "react-router";
import {Provider, connect} from "react-redux";
import xhr from "xhr";

import store from "./store";

import actions from "./actions";
import ActionTypes from "./action-types";

import rootConnector from "./connectors/root-connector";
import App from "./components/app";

import dashboardsConnector from "./connectors/dashboards-connector";
import DashBoards from "./components/dashboards";


// Use a web socket to get status updates
const webSocket = new WebSocket(globals.wsProtocol + "://" + globals.hostName + "/status-socket");
webSocket.onmessage = ({ data }) => store.dispatch({type: ActionTypes.ON_STATUS_UPDATE, data: JSON.parse(data)});

// Keep the websocket alive
const pingWs = () => { webSocket.send("* ping! *"); window.setTimeout(pingWs, 8000); };
webSocket.onopen = pingWs;


const urls = {
    root() {
        return "/";
    }
};

const navigateTo = (key, args) => browserHistory.push(urls[key].apply(null, args));

const connectComponent = (stateToProps) => connect(stateToProps, dispatch => actions(navigateTo, dispatch, webSocket));

export default (
<Provider store={store}>
    <Router history={browserHistory}>
        <Route path={urls.root()} component={connectComponent(rootConnector)(App)}>
            <IndexRoute component={connectComponent(dashboardsConnector)(DashBoards) } />
        </Route>
    </Router>
</Provider>
);
