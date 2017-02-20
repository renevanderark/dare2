import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import { numberFormat } from "../../util/format-number";

class ErrorReports extends React.Component {

    render() {
        // panel actions
        const { onTogglePanelCollapse } = this.props;

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
                            <span className="badge">{numberFormat(report.amount)}</span>
                        </li>
                    ))}
                </ul>
            </CollapsiblePanel>
        )
    }
}

export default ErrorReports;