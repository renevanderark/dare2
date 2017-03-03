import React from "react";
import TestResult from "./test-results/test-result";

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