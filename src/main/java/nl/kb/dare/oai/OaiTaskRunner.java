package nl.kb.dare.oai;

public class OaiTaskRunner implements Runnable {

    private final ListIdentifiers listIdentifiers;
    private boolean isEnabled = false;

    public OaiTaskRunner(ListIdentifiers listIdentifiers) {
        this.listIdentifiers = listIdentifiers;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }



    @Override
    public void run() {
        while (true) {
            if (isEnabled) {
                listIdentifiers.harvestBatches();
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
