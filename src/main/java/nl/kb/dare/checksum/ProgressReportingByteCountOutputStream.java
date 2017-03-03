package nl.kb.dare.checksum;

public class ProgressReportingByteCountOutputStream extends ByteCountOutputStream {


    public ProgressReportingByteCountOutputStream() {

    }

    @Override
    public synchronized void write(int b) {
        super.write(b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        super.write(b, off, len);
    }

}
