package org.example.stream;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

public class OrderGroupStream {

    public Topology createTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        return streamsBuilder.build();
    }
}
