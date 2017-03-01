package nl.kb.dare.integration;

class IntegrationSocketClientStatus {

    private SocketStatusUpdate status;

    void setStatus(SocketStatusUpdate update) {
        this.status = update;
    }


    public SocketStatusUpdate getStatus() {
        return status;
    }
}
