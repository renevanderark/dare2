import React from "react";
import ValidationMarker from "../../widgets/validation-marker";

const toHuman = (str) =>
    str.split("_").map(x => x.toLowerCase().replace(/^./, y => y.toUpperCase())).join(" ");

const TestResult = (props) => {
    const {
        progressStep,
        success,
        result,
        errorStatus,
        errorStatusCode,
        url
    } = props;

    if (typeof progressStep !== 'undefined') {
        return (
            <li className="list-group-item row">
                <span className="col-md-8 col-sm-12 col-xs-15">
                    {toHuman(progressStep)}
                </span>
                <span className="col-md-1 col-sm-1 col-xs-1">
                  <ValidationMarker validates={success}
                      messageOk={`${toHuman(progressStep)} Succeeded`}
                      messageFail={`${toHuman(progressStep)} Failed`}
                  />
                </span>
            </li>
        );
    } else if (typeof result !== 'undefined') {
        return (
            <li className="list-group-item row">
                <span className="col-md-8 col-sm-12 col-xs-15">
                    Result status
                </span>
                <span className="col-md-1 col-sm-1 col-xs-1">
                  <ValidationMarker validates={result === "PROCESSED"}
                                    messageOk={`Get Record Succeeded`}
                                    messageFail={`Get Record Failed`}
                  />
                </span>
            </li>
        );
    } else if (typeof errorStatus !== 'undefined') {
        return (
            <li className="list-group-item row">
                <span className="col-md-8 col-sm-12 col-xs-15">
                    {errorStatusCode} - {toHuman(errorStatus)}
                </span>
                <span className="col-md-1 col-sm-1 col-xs-1">
                  <ValidationMarker validates={false}
                                    messageOk=""
                                    messageFail={toHuman(errorStatus)}
                  />
                </span>
                <span style={{
                        paddingLeft: "10px",
                        display: "inline-block",
                        textOverflow: "ellipsis",
                        overflow: "hidden",
                        whiteSpace: "nowrap"
                    }} className="col-md-23 col-sm-19 col-xs-16">
                    <a href={url} target="_blank">{url}</a>
                </span>
            </li>
        );
    }

    return null;
};


class OaiRecordTestResults extends React.Component {

    shouldComponentUpdate(nextProps) {
        return (this.props.results || []).length !== (nextProps.results || []).length;
    }

    render() {
        const { results} = this.props;
        return results ? (
            <div>
                <h3>Test results</h3>
                <ul className="list-group">
                    {results.map((result, i) => (
                        <TestResult key={i} {...result} />
                    ))}
                </ul>
            </div>
        ) : null;
    }
}

export default OaiRecordTestResults;