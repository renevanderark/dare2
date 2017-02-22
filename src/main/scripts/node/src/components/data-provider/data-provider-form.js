import React from "react";
import Modal from "../modals/modal-with-close-link";
import TextField from "../forms/text-field";
import DatestampField from "../forms/datestamp-field";
import { urls } from "../../router";

class DataProviderForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            repository: props.repository
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState({repository: nextProps.repository})
    }

    componentDidMount() {
        this.setState({repository: this.props.repository})
    }

    onChange(field, ev) {
        this.setState({repository: {
            ...this.state.repository,
            [field]: ev.target.value
        }})
    }


    render() {
        const { repository } = this.state;

        if (!repository) { return null; }


        return (
            <Modal closeLink={urls.dataProvider(repository.id)} title="Edit repository">
                <TextField label="Name" value={repository.name} onChange={this.onChange.bind(this, "name")} />
                <TextField label="Url" value={repository.url} onChange={this.onChange.bind(this, "url")} />
                <TextField label="Set" value={repository.set} onChange={this.onChange.bind(this, "set")} />
                <TextField label="Metadata prefix" value={repository.metadataPrefix} onChange={this.onChange.bind(this, "metadataPrefix")} />
                <DatestampField label="Datestamp" value={repository.dateStamp} onChange={this.onChange.bind(this, "dateStamp")} />
            </Modal>
        );
    }
}

export default DataProviderForm;