import React from "react";

import CollapsiblePanel from "../panels/collapsible-panel";

import Pagination from "./oai-records/pagination";

import Query from "./oai-records/query";
import ResultHeader from "./oai-records/result-header";
import OaiRecordRow from "./oai-records/row";


class OaiRecords extends React.Component {




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