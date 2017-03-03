import React from "react";
import ProgressBar from "../../../widgets/progress-bar";
import {toHumanFileSize} from "../../../../util/format-number";

const DownloadProgress = ({fileIndex, expectedFileSize, currentByteCount, filename}) => (
    <li className="list-group-item row">
        <div className="col-md-1 col-sm-1 col-xs-1 text-center">
            <span style={{display:"inline-block", width: "55%", textAlign: "right"}}>
                {fileIndex}.
            </span>
        </div>
        <div className="col-md-23 col-sm-19 col-xs-16">
            {filename} ({toHumanFileSize(expectedFileSize)})
        </div>
        <ProgressBar current={currentByteCount} total={expectedFileSize} className="col-md-8 col-sm-12 col-xs-15">
            {toHumanFileSize(currentByteCount)}
        </ProgressBar>
    </li>
);

DownloadProgress.propTypes = {
    fileIndex: React.PropTypes.number,
    expectedFileSize: React.PropTypes.number,
    currentByteCount: React.PropTypes.number,
    filename: React.PropTypes.string
};

export default DownloadProgress;