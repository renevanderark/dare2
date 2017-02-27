import React from "react";

class ErrorReport extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            traceIsExpanded: false
        }
    }

    toggleStacktrace() {
        this.setState({traceIsExpanded: !this.state.traceIsExpanded});
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.dateStamp !== this.props.dateStamp) {
            this.setState({traceIsExpanded: false});
        }
    }

    shouldComponentUpdate(nextProps, nextState) {
        return this.state.traceIsExpanded !== nextState.traceIsExpanded ||
            this.props.errorStatusCode !== nextProps.errorStatusCode ||
            this.props.errorStatus !== nextProps.errorStatus ||
            this.props.url !== nextProps.url ||
            this.props.message !== nextProps.message ||
            this.props.dateStamp !== nextProps.dateStamp ||
            this.props.filteredStackTrace !== nextProps.filteredStackTrace;
    }
    
    render() {
        
        const {
            errorStatusCode,
            errorStatus,
            url,
            message,
            dateStamp,
            filteredStackTrace
        } = this.props;

        const { traceIsExpanded } = this.state;

        return (
            <li className="list-group-item">
                <div className="row">
                            <span className="col-md-8">
                                {errorStatusCode} - {errorStatus}
                            </span>
                    <span className="col-24">
                                <a target="_blank" href={url}>
                                    {url}
                                </a>
                            </span>
                </div>
                <div className="row">
                            <span className="col-md-32">
                                {message} ({dateStamp})
                                <span style={{cursor: "pointer"}}
                                      onClick={this.toggleStacktrace.bind(this)}
                                      className={`glyphicon glyphicon-${traceIsExpanded ?
                                          "expand" : "collapse-down"} pull-right`} />
                            </span>
                </div>
                { traceIsExpanded ? (<pre>{filteredStackTrace}</pre>) : null }
            </li>
        );
    }
}

export default ErrorReport;