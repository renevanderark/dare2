import {combineReducers} from "redux";
import statusReducer from "./status-reducer";
import panelReducer from "./panel-reducer";

export default combineReducers({
    status: statusReducer,
    panels: panelReducer
});
