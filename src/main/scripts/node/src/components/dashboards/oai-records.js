import React from "react";
import { Link } from "react-router";

import { urls } from "../../router";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import { numberFormat } from "../../util/format-number";
import Pagination from "./pagination";

class OaiRecords extends React.Component {

    shouldComponentUpdate(nextProps) {

        return nextProps.query !== this.props.query ||
            nextProps.results !== this.props.results ||
            nextProps.collapsed !== this.props.collapsed ||
            nextProps.activeRecordIdentifier !== this.props.activeRecordIdentifier
    }

    render() {
        // panel actions
        const { onTogglePanelCollapse, onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset } = this.props;

        const { activeRecordIdentifier } = this.props;

        const query = Object.keys(this.props.query)
            .filter((key) => this.props.query[key] !== null && key !== 'limit' && key !== 'offset')
            .map((key) => ({key: key, value: this.props.query[key]}));

        const queryPanel = query.length > 0 ? (
            <InnerPanel spacing="col-md-32">
                {query.map(part => (
                    <span className="badge" title={part.key} key={part.key}
                          onClick={() => onSetRecordQueryFilter(part.key, null)}
                          style={{cursor: "pointer"}}>
                        {part.key === "repositoryId"
                            ? (this.props.repositories.find((repo) => "" + repo.id === part.value) || {}).name
                            : part.value}{" "}
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
                        <li key={`${i}-${record.identifier}`}
                            className={`list-group-item row ${activeRecordIdentifier === record.identifier ? "active" : ""}`}>

                            <div className="col-md-16">
                                <Link to={urls.record(encodeURIComponent(record.identifier))}>
                                    {record.identifier}
                                </Link>
                            </div>
                            <div className="col-md-8">
                                <Link to={urls.record(encodeURIComponent(record.identifier))}>
                                    {(this.props.repositories.find((repo) => repo.id === record.repositoryId) || {}).name}
                                </Link>
                            </div>
                            <div className="col-md-5">
                                {record.dateStamp}
                            </div>
                            <div className="col-md-2">
                                {record.processStatus}
                            </div>

                        </li>
                    ))}
                </ul>
                <Pagination offset={this.props.query.offset}
                            limit={this.props.query.limit}
                            count={this.props.results.count}
                            onPageClick={(newOffset) => onSetRecordQueryOffset(newOffset)}
                />
            </CollapsiblePanel>
        )
    }
}

export default OaiRecords;