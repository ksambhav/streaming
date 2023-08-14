package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

@Slf4j
public class OrderPaymentStream {

    public Topology createStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = streamsBuilder.stream("orders");
        orderStream.print(Printed.toSysOut());
        Topology topology = streamsBuilder.build();
        log.info("{}", topology.describe());
        return topology;
    }

}
