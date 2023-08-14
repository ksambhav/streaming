package org.example.stream;

import java.time.Instant;
import java.util.function.Supplier;

public class TimestampSupplier implements Supplier<Instant> {

    private final Instant referenceTimeStamp;
    private int offset = 0;


    public TimestampSupplier(Instant referenceTimeStamp, int initialOffsetSecs) {
        this.referenceTimeStamp = referenceTimeStamp;
        this.offset = initialOffsetSecs;
    }

    @Override
    public Instant get() {
        return referenceTimeStamp.plusSeconds(offset++);
    }
}
