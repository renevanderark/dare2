import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";

import { Link } from "react-router";
import { urls } from "../../router";
import styles from "../../util/styles";

import DownloadProgress from "./progress/download-progress";
import ProgressBar from "../widgets/progress-bar";

const ProgressOrder = [
    "DOWNLOAD_METADATA",
    "GENERATE_MANIFEST",
    "COLLECT_RESOURCES",
    "DOWNLOAD_RESOURCES",
    "FINALIZE_MANIFEST"
];
const toHuman = (str) =>
    str.split("_")
        .map(x => x.toLowerCase()).join(" ")
        .replace(/^[a-zA-Z]+/, x => `${x.replace(/e$/, "")}ing`)
        .replace(/^./, y => y.toUpperCase());

class ProgressDashboard extends React.Component {

    render() {
        const { onTogglePanelCollapse, progress } = this.props;

        if (progress.length === 0) {
            return (
                <CollapsiblePanel id="progress-panel" collapsed={this.props.collapsed} title="Processing progress"
                                                onTogglePanelCollapse={onTogglePanelCollapse}>
                    <i>List is empty</i>
                </CollapsiblePanel>
            )
        }

        return (
            <CollapsiblePanel id="progress-panel" collapsed={this.props.collapsed} title="Processing progress"
                              onTogglePanelCollapse={onTogglePanelCollapse}>
                <ul className="list-group" style={{height: 800, backgroundColor: "rgba(128,128,255,.1)"}}>
                    {progress.map(prog => (
                       <li key={prog.identifier} className="list-group-item row">
                           <div className="col-md-8" style={styles.ellipsis}>
                               <Link to={urls.record(encodeURIComponent(prog.identifier))}>
                                   {prog.identifier}
                               </Link>
                           </div>
                           <div className="col-md-2">
                               {prog.repositoryName}
                           </div>
                           <div className="col-md-8">
                                <span className="col-md-18">
                                    {toHuman(
                                        ProgressOrder[
                                            ProgressOrder.indexOf(prog.getRecordProgress.progressStep) + 1
                                        ] || ""
                                    )}
                                </span>
                               {prog.downloadProgress
                                   ? <ProgressBar current={prog.downloadProgress.fileIndex}
                                        total={prog.downloadProgress.amountOfFiles} className="col-md-13"
                                        indicator={(cur, tot) => `${cur} / ${tot}`}
                                    />
                                   : null}
                           </div>
                           <div className="col-md-14">
                               {prog.downloadProgress ? <DownloadProgress {...prog.downloadProgress} /> : null}
                           </div>
                       </li>
                    ))}
                </ul>
            </CollapsiblePanel>
        );
    }
}

export default ProgressDashboard;