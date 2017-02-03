package nl.kb.dare.oai;

public class OaiTaskRunner implements Runnable {

    private boolean isEnabled = true;

    public OaiTaskRunner() {
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public void run() {
        while (true) {
            if (isEnabled) {
                System.out.println("tick");
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
