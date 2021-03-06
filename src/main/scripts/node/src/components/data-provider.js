import React from "react";
import BreadCrumbs from "./layout/breadcrumbs";
import Workflow from "./dashboards/workflow";
import OaiRecords from "./dashboards/oai-records";
import ErrorReports from "./dashboards/error-reports";
import DataProviderDashboard from "./dashboards/data-provider";

class DataProvider extends React.Component {

    componentDidMount() {
        if (!this.props.records.collapsed) {
           this.props.onSetRecordQueryFilter("repositoryId", this.props.params.id, this.props.params.id);
        }
    }

    componentDidUpdate() {
        if (this.props.dataProvider.repository) {
            document.getElementsByTagName("title")[0].innerHTML =
                "Dare 2 - data provider " + this.props.dataProvider.repository.name;
        }
    }

    render() {
        // actions for records
        const { onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset, onResetRecords } = this.props;

        // actions for panels
        const { onTogglePanelCollapse } = this.props;

        // actions for data provider
        const {
            onFetchDataProvider,
            onValidateRepository,
            onEnableRepository,
            onDisableRepository,
            onDeleteDataProvider
        } = this.props;

        const workFlow = (
            <Workflow {...this.props.workflow}
                      onSetRecordQueryFilter={(field, value) => onSetRecordQueryFilter(field, value, this.props.params.id)}
                      onTogglePanelCollapse={onTogglePanelCollapse} />
        );
        const oaiRecords = (
            <OaiRecords {...this.props.records}
                        onSetRecordQueryFilter={onSetRecordQueryFilter}
                        onSetRecordQueryOffset={onSetRecordQueryOffset}
                        onTogglePanelCollapse={onTogglePanelCollapse}
                        onRefetchRecords={onRefetchRecords}
                        onResetRecords={onResetRecords}
            />
        );

        const errorReports = (
            <ErrorReports {...this.props.errors}
                      onSetRecordQueryFilter={(field, value) => onSetRecordQueryFilter(field, value, this.props.params.id)}
                      onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        const dataProvider = (
            <DataProviderDashboard {...this.props.dataProvider}
                       onTogglePanelCollapse={onTogglePanelCollapse}
                       onFetchDataProvider={onFetchDataProvider}
                       onValidateRepository={onValidateRepository}
                       onEnableRepository={onEnableRepository}
                       onDisableRepository={onDisableRepository}
                       onDeleteDataProvider={onDeleteDataProvider}
            />
        );

        return (
            <div className="container container-fluid">
                <BreadCrumbs titles={["Data provider"]} />
                {this.props.children ? React.cloneElement(this.props.children, {...this.props.dataProvider }) : null}
                {dataProvider}
                {workFlow}
                {oaiRecords}
                {errorReports}
            </div>
        );
    }
}

export default DataProvider;