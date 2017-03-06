const progressConnector = ({panels, repositories, status: { status: {recordsBeingProcessed}}}) => ({
    progress: {
        collapsed: panels["progress-panel"].collapsed,
        progress: Object.keys(recordsBeingProcessed || {}).map(identifier => ({
            ...recordsBeingProcessed[identifier],
            identifier: identifier,
            repositoryName:  ((repositories.list || [])
                .find(repo => recordsBeingProcessed[identifier].getRecordProgress.repositoryId === repo.id) || {}).name
        }))
    }
});


export default progressConnector;