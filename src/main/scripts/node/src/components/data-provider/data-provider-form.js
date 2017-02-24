import React from "react";
import Modal from "../modals/modal-with-close-link";
import ModalBody from "../modals/modal-body";
import ModalFooter from "../modals/modal-footer-with-close-link";

import TextField from "../forms/text-field";
import DatestampField from "../forms/datestamp-field";
import { validateDateStamp } from "../forms/datestamp-field";

import ValidationMarker from "../widgets/validation-marker";

import { urls } from "../../router";

class DataProviderForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            repository: props.underEdit || props.repository,
            changed: false
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.underEdit) {
            this.setState({
                changed: false,
                repository: nextProps.underEdit
            });
        } else {
            this.setState({
                changed: false,
                repository: nextProps.repository
            });
        }
    }

    componentDidMount() {
        if (this.props.underEdit) {
            this.setState({
                changed: false,
                repository: this.props.underEdit
            });
        } else {
            this.setState({
                changed: false,
                repository: this.props.repository
            });
        }
    }

    onChange(field, ev) {
        this.setState({
            changed: true,
            repository: {
            ...this.state.repository,
            [field]: ev.target.value
        }});
    }


    render() {
        const { repository, changed } = this.state;
        const { onValidateNewRepository, validationResultsUnderEdit, onSaveRepository } = this.props;

        const {urlIsValidOAI, setExists, metadataFormatSupported } = validationResultsUnderEdit;

        if (!repository) { return null; }

        const allowedToSave =
            validateDateStamp(repository.dateStamp) &&
            !changed &&
            urlIsValidOAI &&
            setExists &&
            metadataFormatSupported;

        return (
            <Modal closeLink={repository.id ? urls.dataProvider(repository.id) : "/"}
                   title={repository.id ? "Edit repository" : "Create new repository"}>
                <ModalBody>
                    <TextField label="Name" value={repository.name} onChange={this.onChange.bind(this, "name")} />
                    <TextField label="Url" value={repository.url} onChange={this.onChange.bind(this, "url")}>
                        <ValidationMarker validates={urlIsValidOAI}
                            messageOk="Url is a valid OAI endpoint" messageFail="Url is not a valid OAI endpoint" />
                    </TextField>
                    <TextField label="Set" value={repository.set} onChange={this.onChange.bind(this, "set")}>
                        <ValidationMarker validates={setExists}
                                          messageOk="Set exists in this repository"
                                          messageFail="Set does not exist in this repository" />
                    </TextField>
                    <TextField label="Metadata prefix" value={repository.metadataPrefix} onChange={this.onChange.bind(this, "metadataPrefix")}>
                        <ValidationMarker validates={metadataFormatSupported}
                                          messageOk="Metadata format is supported by this repository"
                                          messageFail="Metadata format is not supported by this repository"
                        />
                    </TextField>
                    <DatestampField label="Datestamp" value={repository.dateStamp} onChange={this.onChange.bind(this, "dateStamp")} />
                </ModalBody>
                <ModalFooter closeLink={repository.id ? urls.dataProvider(repository.id) : "/"}>
                    <button className="btn btn-default"
                            disabled={!validateDateStamp(repository.dateStamp)}
                            onClick={() => onValidateNewRepository(repository)}>
                        Test settings
                    </button>
                    <button className="btn btn-default"
                            disabled={!allowedToSave}
                            onClick={onSaveRepository}>
                        Save
                    </button>
                </ModalFooter>
            </Modal>
        );
    }
}

export default DataProviderForm;