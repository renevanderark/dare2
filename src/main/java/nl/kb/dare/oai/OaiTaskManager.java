package nl.kb.dare.oai;

import io.dropwizard.lifecycle.Managed;

public class OaiTaskManager implements Managed {
    private static Thread oaiRunnerThread = null;
    private final OaiTaskRunner oaiTaskRunner;

    public OaiTaskManager(OaiTaskRunner oaiTaskRunner) {

        this.oaiTaskRunner = oaiTaskRunner;
    }

    @Override
    public void start() throws Exception {
        if (oaiRunnerThread == null) {
            oaiRunnerThread = new Thread(oaiTaskRunner);
        } else {
            oaiRunnerThread.interrupt();
        }
        oaiRunnerThread.start();
    }

    @Override
    public void stop() throws Exception {
        oaiRunnerThread.interrupt();
    }
}
