import React from "react";
import {toHumanFileSize} from "../../../util/format-number";

class Manifest extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.identifier !== nextProps.identifier ||
                this.props.manifest !== nextProps.manifest;
    }

    render() {
        const { manifest } = this.props;
        return manifest.length ? (
            <div>
                <h3>Manifest</h3>
                <ol className="list-group">
                    {manifest.map((entry, i) => (
                        <li key={i} className="list-group-item row">
                            <div className="col-md-1 col-sm-1 col-xs-1 text-center">
                                <span style={{display:"inline-block", width: "55%", textAlign: "right"}}>
                                    {i+1}.
                                </span>
                            </div>
                            <div className="col-md-8 col-sm-8 col-xs-8">
                                <a href={entry.xlinkHref}>
                                    {decodeURIComponent(entry.xlinkHref.replace(/^.*\//, ""))}
                                </a>
                            </div>
                            <div className="col-md-4 col-sm-4 col-xs-4">
                                {toHumanFileSize(entry.size)}
                            </div>
                            <div className="col-md-12 col-sm-12 col-xs-12">
                                {entry.checksumType} checksum: {entry.checksum}
                            </div>
                        </li>
                    ))}
                </ol>

            </div>
        ) : null;
    }
}

Manifest.propTypes = {
    manifest: React.PropTypes.array.isRequired,
    identifier: React.PropTypes.string.isRequired
};

export default Manifest;