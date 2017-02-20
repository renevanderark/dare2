import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";

class OaiRecords extends React.Component {

    render() {
        // panel actions
        const { onTogglePanelCollapse } = this.props;

        const query = Object.keys(this.props.query)
            .filter((key) => this.props.query[key] !== null)
            .map((key) => ({key: key, value: this.props.query[key]}));

        return (
            <CollapsiblePanel id="oai-records-panel" collapsed={this.props.collapsed} title="Records browser"
                              onTogglePanelCollapse={onTogglePanelCollapse}>

                <h4>Query</h4>
                <InnerPanel spacing="col-md-32">
                    <div className="row">
                        {query.map(part => (<div className="col-md-8" key={part.key}>
                            <strong>{part.key}</strong>:
                            {part.value}
                        </div>))}
                    </div>
                </InnerPanel>
                <div className="clearfix" />
                <br />
                <h4>
                    <span className="glyphicon glyphicon-refresh" />
                    {" "}
                    Results ({this.props.results.count})
                </h4>
                <ul className="list-group">
                    {(this.props.results.result || []).map((record) => (
                        <li key={record.identifier} className="list-group-item row">
                            <div className="col-md-8">
                                {record.identifier}
                            </div>
                            <div className="col-md-8">
                                {record.dateStamp}
                            </div>
                            <div className="col-md-8">
                                {record.processStatus}
                            </div>
                            <div className="col-md-8">
                                {(this.props.repositories.find((repo) => repo.id === record.repositoryId) || {}).set}
                            </div>
                        </li>
                    ))}
                </ul>
            </CollapsiblePanel>
        )
    }
}

export default OaiRecords;