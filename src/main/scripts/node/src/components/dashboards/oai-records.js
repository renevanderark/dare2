import React from "react";

import CollapsiblePanel from "../panels/collapsible-panel";

import Pagination from "./oai-records/pagination";

import Query from "./oai-records/query";
import ResultHeader from "./oai-records/result-header";
import OaiRecordRow from "./oai-records/row";

const shouldUpdateForPagination = (props, nextProps) =>
    props.query.offset !== nextProps.query.offset ||
    props.query.limit !== nextProps.query.limit ||
    props.results.count !== nextProps.results.count;

const serializeQuery = (query) =>
    query.map(({label, key}) => `${key}-${label}`).join();

const serializeResults = (results) => results.map(result =>
    result.identifier + "-" +
    result.repositoryName + "-" +
    result.dateStamp + "-" +
    result.processStatus + "-").join("|");

    class OaiRecords extends React.Component {

    shouldComponentUpdate(nextProps) {
        return  this.props.collapsed !== nextProps.collapsed ||
            this.props.activeRecordIdentifier !== nextProps.activeRecordIdentifier ||
            shouldUpdateForPagination(this.props, nextProps) ||
            serializeQuery(this.props.labeledQuery) !== serializeQuery(nextProps.labeledQuery) ||
            serializeResults(this.props.results.result) !== serializeResults(nextProps.results.result);
    }

    render() {
        // panel actions
        const { onTogglePanelCollapse, onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset } = this.props;

        const { activeRecordIdentifier, labeledQuery, collapsed, results: { count, result }, query } = this.props;

        return (
            <CollapsiblePanel id="oai-records-panel" collapsed={collapsed} title="Records browser"
                              onTogglePanelCollapse={onTogglePanelCollapse}>

                <Query query={labeledQuery} onSetRecordQueryFilter={onSetRecordQueryFilter} />
                <ResultHeader count={count} onRefetchRecords={onRefetchRecords} />

                <ul className="list-group">
                    {(result || []).map((record, i) => (
                        <OaiRecordRow {...record} key={i} activeRecordIdentifier={activeRecordIdentifier} />
                    ))}
                </ul>
                <Pagination offset={query.offset}
                            limit={query.limit}
                            count={count}
                            onPageClick={(newOffset) => onSetRecordQueryOffset(newOffset)}
                />
            </CollapsiblePanel>
        )
    }
}

export default OaiRecords;