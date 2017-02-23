import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import { connectSocket } from "./bootstrap";

document.addEventListener("DOMContentLoaded", () =>
    connectSocket(() => ReactDOM.render(router, document.getElementById("app"))));
