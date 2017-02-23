import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import CounterBadge from "../widgets/counter-badge";

const serializeProps = (props) =>
    Object.keys(props)
        .filter((key) => typeof props[key] === "number")
        .map((key) => `${key}:${props[key]}`)
        .join();

class ErrorReports extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.collapsed !== nextProps.collapsed ||
            serializeProps(nextProps) !== serializeProps(this.props);
    }

    render() {
        // panel actions
        const { onTogglePanelCollapse, onSetRecordQueryFilter } = this.props;

        const errorReports = Object.keys(this.props)
            .filter((key) => typeof this.props[key] === "number")
            .map((key) => ({error: key, amount: this.props[key]}));

        return (
            <CollapsiblePanel id="error-panel" collapsed={this.props.collapsed} title="Error reports"
                              onTogglePanelCollapse={onTogglePanelCollapse}>
                <ul className="list-group">
                    {errorReports.map((report, i) => (
                        <li key={report.error} className="list-group-item">
                            <span>{report.error}</span>
                            <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter} count={report.amount}
                                          filterKey="errorStatus" filterValue={report.error.replace(/\s+-.*$/, "")}/>
                        </li>
                    ))}
                </ul>
            </CollapsiblePanel>
        )
    }
}

export default ErrorReports;