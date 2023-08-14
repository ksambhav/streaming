package org.example.stream;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.example.model.Order;

public class OrderGroupStream {

    public Topology createTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = StreamUtils.getOrderStream(streamsBuilder);
        return streamsBuilder.build();
    }
}
