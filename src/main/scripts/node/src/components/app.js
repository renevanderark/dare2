import React from "react";


class App extends React.Component {

    render() {

        return (
            <div className="container container-fluid">
                {this.props.children}
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default App;