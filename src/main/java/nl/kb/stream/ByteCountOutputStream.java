package nl.kb.stream;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ByteCountOutputStream extends ByteArrayOutputStream {
    private final AtomicLong byteCount = new AtomicLong(0L);

    @Override
    public synchronized void write(int b) {
        byteCount.getAndIncrement();
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        byteCount.getAndAdd((long) len);
    }

    public long getCurrentByteCount() {
        return byteCount.get();
    }
}
