import {combineReducers} from "redux";
import statusReducer from "./status-reducer";
import panelReducer from "./panel-reducer";
import repositoriesReducer from "./repositories-reducer";
import oaiRecordsReducer from "./oai-records-reducer";

export default combineReducers({
    status: statusReducer,
    panels: panelReducer,
    repositories: repositoriesReducer,
    oaiRecords: oaiRecordsReducer
});
