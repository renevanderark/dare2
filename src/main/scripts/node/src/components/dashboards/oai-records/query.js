import React from "react";

const serializeQuery = (query) =>
    query.map(({label, key}) => `${key}-${label}`).join();

class Query extends React.Component {

    shouldComponentUpdate(nextProps) {
        return nextProps.query.length !== this.props.query.length ||
                serializeQuery(this.props.query) !== serializeQuery(nextProps.query);
    }

    render() {
        const { query } = this.props;
        const { onSetRecordQueryFilter } = this.props;

        const queryPanel = query.length > 0 ? query.map(part => (
                <span className="badge" title={part.key} key={part.key}
                      onClick={() => onSetRecordQueryFilter(part.key, null)}
                      style={{cursor: "pointer"}}>
                    {part.label}
                    <span className="glyphicon glyphicon-remove" />
            </span>
            )) : null;

        return (
            <div>
                <h4>Query</h4>
                {queryPanel}
            </div>
        );
    }
}

Query.propTypes = {
    query: React.PropTypes.array.isRequired,
    onSetRecordQueryFilter: React.PropTypes.func.isRequired
};

export default Query;