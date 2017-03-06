import React from "react";
import {Router, Route, IndexRoute, browserHistory} from "react-router";
import {Provider, connect} from "react-redux";

import store from "./store";
import actions from "./actions";

import rootConnector from "./connectors/root-connector";
import App from "./components/app";

import dashboardsConnector from "./connectors/dashboards-connector";
import DashBoards from "./components/dashboards";

import oaiRecordConnector from "./connectors/oai-record-connector";
import OaiRecord from "./components/oai-record";

import dataProviderConnector from "./connectors/data-provider-connector";
import DataProvider from "./components/data-provider";
import NewDataProvider from "./components/data-provider/new-data-provider";
import DataProviderForm from "./components/data-provider/data-provider-form";

import progressConnector from "./connectors/progress-connector";
import Progress from "./components/progress";
const urls = {
    root() {
        return "/";
    },
    record(identifier = null) {
        return identifier
            ? `/record-overview/${identifier}`
            : "/record-overview/:identifier"
    },
    dataProvider(id = null) {
        return id
            ? `/data-provider/${id}`
            : "/data-provider/:id"
    },
    editDataProvider(id = null) {
        return id
            ? `/data-provider/${id}/edit`
            : "/data-provider/:id/edit"
    },
    newDataProvider() {
        return "/data-provider/new"
    },
    progress() {
        return "/progress"
    }

};

export { urls };

const navigateTo = (key, args) => browserHistory.push(urls[key].apply(null, args));

const connectComponent = (stateToProps) => connect(stateToProps, dispatch => actions(navigateTo, dispatch));

const newDataProviderConnector = (state) => ({
    underEdit: state.repositories.underEdit || {
        name: "",
        url: "",
        set: "",
        metadataPrefix: "",
        dateStamp: null
    },
    validationResultsUnderEdit: state.repositories.validationResultsUnderEdit || {}
});

export default (
    <Provider store={store}>
        <Router history={browserHistory}>
            <Route path={urls.root()} component={connectComponent(rootConnector)(App)}>
                <IndexRoute component={connectComponent(dashboardsConnector)(DashBoards) } />
                <Route path={urls.progress()} component={connectComponent(progressConnector)(Progress)} />
                <Route path={urls.record()} component={connectComponent(oaiRecordConnector)(OaiRecord) } />
                <Route path={urls.newDataProvider()} component={connectComponent(newDataProviderConnector)(NewDataProvider)} />
                <Route path={urls.dataProvider()} component={connectComponent(dataProviderConnector)(DataProvider)}>
                    <Route path={urls.editDataProvider()} component={connectComponent((state) => ({}))(DataProviderForm)} />
                </Route>
            </Route>
        </Router>
    </Provider>
);
