import React from "react";

class App extends React.Component {

    render() {
        return (
            <pre>
                {JSON.stringify(this.props.status, null, 4)}
            </pre>
        )
    }
}

export default App;