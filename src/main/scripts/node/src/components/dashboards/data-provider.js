import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import EnableToggle from "./enable-toggle";

import { Link } from "react-router";
import { urls } from "../../router";

class DataProviderDashboard extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.collapsed !== nextProps.collapsed ||
                this.props.repository !== nextProps.repository ||
                this.props.id !== nextProps.id ||
                this.props.validationResults !== nextProps.validationResults;
    }

    componentWillReceiveProps(nextProps) {
        const {onFetchDataProvider} = this.props;

        if (nextProps.id !== this.props.id) {
            onFetchDataProvider(nextProps.id);
        }
    }

    componentDidMount() {
        const { id, repository, onFetchDataProvider } = this.props;


        if (!repository || `${repository.id}` !== id) {
            onFetchDataProvider(id);
        }
    }

    render() {
        const { collapsed, repository, validationResults } = this.props;
        const {
            onTogglePanelCollapse,
            onValidateRepository,
            onEnableRepository,
            onDisableRepository,
            onFetchDataProvider
        } = this.props;

        const { setExists, metadataFormatSupported } = validationResults;

        const setExistsMarker = typeof setExists === 'undefined'
            ? null
            : setExists
                ? <span title="Set exists in this repository" className="glyphicon glyphicon-ok pull-right"
                        style={{color: "green", cursor: "pointer"}} />
                : <span title="Set does not exist in this repository" className="glyphicon glyphicon-remove pull-right"
                        style={{color: "red", cursor: "pointer"}} />;

        const metadataFormatSupportedMarker = typeof metadataFormatSupported === 'undefined'
            ? null
            : metadataFormatSupported
                ? <span title="Metadata format is supported by this repository"
                        className="glyphicon glyphicon-ok pull-right"
                        style={{color: "green", cursor: "pointer"}} />
                : <span title="Metadata format is not supported by this repository"
                        className="glyphicon glyphicon-remove pull-right"
                        style={{color: "red", cursor: "pointer"}} />;

        const enableToggle = repository
            ? <EnableToggle enabled={repository.enabled}
                            onEnableClick={() => onEnableRepository(repository.id, () => onFetchDataProvider(repository.id)) }
                            onDisableClick={() => onDisableRepository(repository.id, () => onFetchDataProvider(repository.id)) }/>
            : null;

        const body = repository
            ? (
                <div>
                    <h3>{repository.name}</h3>
                    <ul className="list-group">
                        <li className="row list-group-item">
                            <strong className="col-md-4">Name</strong>
                            <span className="col-md-16">{repository.name}</span>
                        </li>
                        <li className="row list-group-item">
                            <strong className="col-md-4">Url</strong>
                            <span className="col-md-16">{repository.url}</span>
                        </li>
                        <li className="row list-group-item">
                            <strong className="col-md-4">OAI set</strong>
                            <span className="col-md-16">
                                {repository.set}
                                {setExistsMarker}
                            </span>
                        </li>
                        <li className="row list-group-item">
                            <strong className="col-md-4">OAI metadataPrefix</strong>
                            <span className="col-md-16">
                                {repository.metadataPrefix}
                                {metadataFormatSupportedMarker}
                            </span>
                        </li>
                        <li className="row list-group-item">
                            <strong title="Both latest datestamp encountered in harvest and start date for next harvest"
                                    className="col-md-4">
                                Datestamp<sup>1</sup>
                            </strong>
                            <span className="col-md-16">
                              {repository.dateStamp || "- none harvested yet -"}
                            </span>
                        </li>
                        <li className="row list-group-item">
                            <strong className="col-md-4">Enabled</strong>
                            <span className="col-md-16">{enableToggle}</span>
                        </li>
                    </ul>
                    <button className="btn btn-default" onClick={() => onValidateRepository(repository.id)}>
                        Test settings
                    </button>
                    <Link to={urls.editDataProvider(repository.id)} className="btn btn-default">
                        Edit
                    </Link>
                </div>
            ) : (<div>Loading...</div>);
        return (
            <CollapsiblePanel id="data-provider-panel" title="Data provider" collapsed={collapsed}
                              onTogglePanelCollapse={onTogglePanelCollapse}>
                {body}
            </CollapsiblePanel>
        );
    }
}

export default DataProviderDashboard;