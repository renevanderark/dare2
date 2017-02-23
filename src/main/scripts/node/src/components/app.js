import React from "react";
import Header from "./layout/header";

class App extends React.Component {

    render() {
        return (
            <div>
                <Header socketClosed={this.props.socketClosed} />
                {this.props.children}
            </div>
        )
    }
}

export default App;