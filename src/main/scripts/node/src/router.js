import React from "react";
import {Router, Route, IndexRoute, browserHistory} from "react-router";
import {Provider, connect} from "react-redux";

import store from "./store";
import actions from "./actions";

import App from "./components/app";
import DashBoards from "./components/dashboards";

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
            <IndexRoute component={connectComponent(defaultConnector)(DashBoards) } />
        </Route>
    </Router>
</Provider>
);
