package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.processor.TimestampExtractor;

@Slf4j
public class OrderPaymentStream {

    public Topology createStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = streamsBuilder.stream("orders",
                Consumed.with(Serdes.Integer(), CustomSerdes.Order()).withTimestampExtractor(new TimestampExtractor() {
                    @Override
                    public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {
                        return 0;
                    }
                }));
        orderStream
                .filter((k, o) -> o.getAmount().intValue() > 0)
                .print(Printed.toSysOut());
        Topology topology = streamsBuilder.build();
        log.info("{}", topology.describe());
        return topology;
    }

}
