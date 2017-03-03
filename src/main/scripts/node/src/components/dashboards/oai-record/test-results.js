import React from "react";
import ValidationMarker from "../../widgets/validation-marker";

const toHuman = (str) =>
    str.split("_").map(x => x.toLowerCase().replace(/^./, y => y.toUpperCase())).join(" ");

const toPercentage = (download) =>
    `${parseInt((download.currentByteCount / download.expectedFileSize) * 100, 10)}%`;

const toHumanFileSize = (bytes) => {
    const thresh = 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    const units = ['kB','MB','GB','TB','PB','EB','ZB','YB']
    let u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(1) + ' ' + units[u];
};

const TestResult = (props) => {
    const {
        progressStep,
        success,
        result,
        errorStatus,
        errorStatusCode,
        url,
        downloadProgress
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
                {progressStep === 'COLLECT_RESOURCES' && downloadProgress ? (
                    <ul className="list-group col-md-32 col-sm-32 col-xs-32 clearfix" style={{marginTop: 10}}>
                        {downloadProgress.map((dl, i) => (
                            <li className="list-group-item row" key={i}>
                                <div className="col-md-8 col-sm-12 col-xs-15">
                                    {dl.filename} ({dl.fileIndex} / {dl.amountOfFiles})
                                </div>
                                <div className="progress col-md-24 col-sm-20 col-xs-17" style={{marginBottom: 0}}>
                                    <div className={`progress-bar ${dl.currentByteCount < dl.expectedFileSize ? "progress-bar-striped active" : ""}`}
                                         style={{width: toPercentage(dl), transition: "width .3s linear" }} >
                                        {toHumanFileSize(dl.currentByteCount)} /  {toHumanFileSize(dl.expectedFileSize)}
                                        ({toPercentage(dl)})
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : null}
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

const serializeDownloadProgress = (dlp) =>
    dlp.map(dl => dl.currentByteCount).join("-");

class OaiRecordTestResults extends React.Component {

    shouldComponentUpdate(nextProps) {
        return (this.props.results || []).length !== (nextProps.results || []).length ||
            serializeDownloadProgress(nextProps.downloadProgress || []) !==
            serializeDownloadProgress(this.props.downloadProgress || []);
    }

    render() {
        const { results, downloadProgress } = this.props;
        return results ? (
            <div>
                <h3>Test results</h3>
                <ul className="list-group">
                    {results.map((result, i) => (
                        <TestResult key={i} {...result} downloadProgress={downloadProgress} />
                    ))}
                </ul>
            </div>
        ) : null;
    }
}

export default OaiRecordTestResults;