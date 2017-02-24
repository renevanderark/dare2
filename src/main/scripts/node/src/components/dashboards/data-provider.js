import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import EnableToggle from "../widgets/enable-toggle";
import ButtonWithModalWarning from "../modals/button-with-modal-warning";
import ValidationMarker from "../widgets/validation-marker";

import { Link } from "react-router";
import { urls } from "../../router";

class DataProviderDashboard extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.collapsed !== nextProps.collapsed ||
                this.props.repository !== nextProps.repository ||
                this.props.id !== nextProps.id ||
                this.props.validationResults !== nextProps.validationResults ||
                this.props.underEdit !== nextProps.underEdit;
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
            onFetchDataProvider,
            onDeleteDataProvider
        } = this.props;

        const { setExists, metadataFormatSupported } = validationResults;

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
                                <ValidationMarker validates={setExists}
                                      messageOk="Set exists in this repository"
                                      messageFail="Set does not exist in this repository"
                                />
                            </span>
                        </li>
                        <li className="row list-group-item">
                            <strong className="col-md-4">OAI metadataPrefix</strong>
                            <span className="col-md-16">
                                {repository.metadataPrefix}
                                <ValidationMarker validates={metadataFormatSupported}
                                      messageOk="Metadata format is supported by this repository"
                                      messageFail="Metadata format is not supported by this repository"
                                />
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

                    <ButtonWithModalWarning
                        className="btn btn-danger pull-right"
                        label="Delete"
                        onConfirm={() => onDeleteDataProvider(repository.id)}>
                        <p>This operation will completely purge all records and error reports!</p>
                        <p>Are you sure?</p>
                    </ButtonWithModalWarning>
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