import React from "react";

import CollapsiblePanel from "../panels/collapsible-panel";

import Pagination from "./oai-records/pagination";

import Query from "./oai-records/query";
import ResultHeader from "./oai-records/result-header";
import OaiRecordRow from "./oai-records/row";
import ButtonWithModalWarning from "../modals/button-with-modal-warning";
import {numberFormat} from "../../util/format-number";

class OaiRecords extends React.Component {

    render() {
        // panel actions
        const {
            onTogglePanelCollapse,
            onSetRecordQueryFilter,
            onRefetchRecords,
            onResetRecords,
            onSetRecordQueryOffset
        } = this.props;

        const {
            activeRecordIdentifier,
            labeledQuery,
            collapsed,
            results: { count, result },
            query,
            bulkResetEnabled
        } = this.props;

        const bulkResetButton = bulkResetEnabled ?
            (
                <ButtonWithModalWarning className="btn btn-default" label="Reset selection to pending"
                                        onConfirm={(doClose) => onResetRecords(doClose)}>
                    Are you sure you want to reset all {numberFormat(count)} records in selection to pending?
                </ButtonWithModalWarning>
            ) : (
                <button className="btn btn-default" disabled={true} title="Please turn off object harversted first">
                    Reset selection to pending
                </button>
            );
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
                <div className="panel-footer">
                    {bulkResetButton}
                </div>
            </CollapsiblePanel>
        )
    }
}

export default OaiRecords;