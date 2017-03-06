const getExpectedFileSize = (downloadProgress) =>
    downloadProgress ?  downloadProgress.expectedFileSize : 0;

const progressConnector = ({panels, repositories, status: { status: {recordsBeingProcessed}}}) => ({
    progress: {
        collapsed: panels["progress-panel"].collapsed,
        progress: Object.keys(recordsBeingProcessed || {}).map(identifier => ({
            ...recordsBeingProcessed[identifier],
            identifier: identifier,
            repositoryName:  ((repositories.list || [])
                .find(repo => recordsBeingProcessed[identifier].getRecordProgress.repositoryId === repo.id) || {}).name
        })).sort((a, b) => getExpectedFileSize(b.downloadProgress) - getExpectedFileSize(a.downloadProgress))
    }
});


export default progressConnector;