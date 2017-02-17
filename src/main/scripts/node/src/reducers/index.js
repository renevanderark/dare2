import {combineReducers} from "redux";
import statusReducer from "./status-reducer";
import panelReducer from "./panel-reducer";
import repositoriesReducer from "./repositories-reducer";

export default combineReducers({
    status: statusReducer,
    panels: panelReducer,
    repositories: repositoriesReducer
});
