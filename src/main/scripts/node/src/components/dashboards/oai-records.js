import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import { numberFormat } from "../../util/format-number";

class OaiRecords extends React.Component {

    render() {
        // panel actions
        const { onTogglePanelCollapse, onSetRecordQueryFilter, onRefetchRecords } = this.props;

        const query = Object.keys(this.props.query)
            .filter((key) => this.props.query[key] !== null && key !== 'limit' && key !== 'offset')
            .map((key) => ({key: key, value: this.props.query[key]}));

        const queryPanel = query.length > 0 ? (
            <InnerPanel spacing="col-md-32">
                {query.map(part => (
                    <span className="badge" title={part.key} key={part.key}
                          onClick={() => onSetRecordQueryFilter(part.key, null)}
                          style={{cursor: "pointer"}}>
                        {part.value}{" "}
                        <span className="glyphicon glyphicon-remove" />
                    </span>
                ))}
            </InnerPanel>
        ) : null;

        return (
            <CollapsiblePanel id="oai-records-panel" collapsed={this.props.collapsed} title="Records browser"
                              onTogglePanelCollapse={onTogglePanelCollapse}>

                <h4>Query</h4>
                {queryPanel}
                <div className="clearfix" />
                <br />
                <h4>
                    <span className="glyphicon glyphicon-refresh" style={{cursor: "pointer"}}
                          onClick={() => onRefetchRecords()}
                    />
                    {" "}
                    Results ({numberFormat(this.props.results.count)})
                </h4>
                <ul className="list-group">
                    {(this.props.results.result || []).map((record, i) => (
                        <li key={`${i}-${record.identifier}`} className="list-group-item row">
                            <div className="col-md-16">
                                {record.identifier}
                            </div>
                            <div className="col-md-8">
                                {record.dateStamp}
                            </div>
                            <div className="col-md-4">
                                {record.processStatus}
                            </div>
                            <div className="col-md-4">
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