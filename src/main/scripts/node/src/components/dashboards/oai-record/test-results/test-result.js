import React from "react";
import ProgressStep from "./progress-step";
import DownloadProgress from "./download-progress";
import styles from "../../../../util/styles";

const toHuman = (str) =>
    str.split("_").map(x => x.toLowerCase().replace(/^./, y => y.toUpperCase())).join(" ");

const TestResult = (props) => {
    const { progressStep, success, result, errorStatus, errorStatusCode, url, downloadProgress } = props;

    if (typeof progressStep !== 'undefined') {
        const downloadList = progressStep === 'COLLECT_RESOURCES' && downloadProgress
            ? (
                <ul className="list-group col-md-32 col-sm-32 col-xs-32 clearfix" style={{marginTop: 10}}>
                    {downloadProgress.map((dl) => (<DownloadProgress {...dl} key={dl.filename} />))}
                </ul>
            ) : null;
        return (
            <ProgressStep title={toHuman(progressStep)} validates={success}
                          messageOk={`${toHuman(progressStep)} Succeeded`}
                          messageFail={`${toHuman(progressStep)} Failed`}>
                {downloadList}
            </ProgressStep>
        );
    }

    if (typeof result !== 'undefined') {
        return (<ProgressStep title="Result status" validates={result === "PROCESSED"}
                          messageOk="Get Record Succeeded" messageFail="Get Record Failed"/>);
    }

    if (typeof errorStatus !== 'undefined') {
        return (
            <ProgressStep title={`${errorStatusCode} - ${toHuman(errorStatus)}`} validates={false}
                          messageOk="" messageFail={toHuman(errorStatus)}>
                <span style={{...styles.ellipsis, paddingLeft: "10px"}} className="col-md-23 col-sm-19 col-xs-16">
                    <a href={url} target="_blank">{url}</a>
                </span>
            </ProgressStep>
        );
    }

    return null;
};


TestResult.propTypes = {
    progressStep: React.PropTypes.string,
    success: React.PropTypes.bool,
    result: React.PropTypes.string,
    errorStatus: React.PropTypes.string,
    errorStatusCode: React.PropTypes.number,
    url: React.PropTypes.string,
    downloadProgress: React.PropTypes.array
};

export default TestResult;