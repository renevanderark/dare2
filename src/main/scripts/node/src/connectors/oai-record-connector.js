import {convertRecords} from "./converters";
const oaiRecordConnector = (state, routed) => {
    const oaiRecord = state.oaiRecords.current;
    const repositoryName = ((state.repositories.list || [])
        .find(repo => ((oaiRecord || {}).record || {}).repositoryId === repo.id) || {}).name;

    return {
        identifier: routed.params.identifier,
        oaiRecord: {
            ...oaiRecord,
            testResults: state.oaiRecords.testResults,
            manifest: state.oaiRecords.manifest,
            repositoryName: repositoryName,
            collapsed: state.panels["oai-record-panel"].collapsed
        },
        records: convertRecords(state)
    }
};

export default oaiRecordConnector;