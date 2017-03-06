import React from "react";
import ProgressBar from "../../widgets/progress-bar";
import {toHumanFileSize} from "../../../util/format-number";
import styles from "../../../util/styles";

const DownloadProgress = ({fileIndex, amountOfFiles, expectedFileSize, currentByteCount, filename}) => (
    <div>
        <div className="col-md-20 col-sm-20 col-xs-20" style={styles.ellipsis}>
            Downloading {fileIndex}/{amountOfFiles}: {filename} ({toHumanFileSize(expectedFileSize)})
        </div>
        <ProgressBar current={currentByteCount} total={expectedFileSize} className="col-md-12 col-sm-12 col-xs-12">
            {toHumanFileSize(currentByteCount)}
        </ProgressBar>
    </div>
);

DownloadProgress.propTypes = {
    fileIndex: React.PropTypes.number,
    amountOfFiles: React.PropTypes.number,
    expectedFileSize: React.PropTypes.number,
    currentByteCount: React.PropTypes.number,
    filename: React.PropTypes.string
};

export default DownloadProgress;