import React from "react";


class App extends React.Component {

    render() {

        return (
            <div>
                <div className="navbar navbar-default">
                    <div className="container container-fluid">
                        <div className="navbar-brand">
                            Dare 2
                        </div>
                        <div className="navbar-right navbar-text">
                            <span style={{transform: "rotate(90deg)", color: this.props.socketClosed ? "red" : "green"}}
                                className="glyphicon glyphicon-transfer" />
                        </div>
                    </div>
                </div>
                <div className="container container-fluid">
                    {this.props.children}
                </div>
            </div>
        )
    }
}

export default App;