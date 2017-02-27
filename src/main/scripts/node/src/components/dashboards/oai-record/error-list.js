import React from "react";

import ErrorReport from "./error-report";

class ErrorList extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.recordIdentifier !== nextProps.recordIdentifier;
    }

    render() {
        const { errorReports } = this.props;

        return errorReports && errorReports.length > 0 ? (
            <div>
                <h3>Error reports</h3>
                <ul className="list-group">
                    {errorReports.map((errorReport, i) => (
                        <ErrorReport key={i} {...errorReport} />
                    ))}
                </ul>
            </div>
        ) : null;
    }
}

export default ErrorList;