package org.example.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.example.config.CustomSerdes;
import org.example.model.Order;

import java.time.Duration;

@Slf4j
public class OrderGroupStream {

    public Topology createTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = StreamUtils.getOrderStream(streamsBuilder);
        KGroupedStream<String, Order> orderGroupedByItem = orderStream
                .groupBy((k, v) -> v.getItemDescription(),
                        Grouped.with("groupoo", Serdes.String(), CustomSerdes.Order())
                );

        KTable<Windowed<String>, Long> stringLongKTable = orderGroupedByItem
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(15)))
                .count();
//                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(2), Suppressed.BufferConfig.maxRecords(1000)));
        stringLongKTable.toStream().print(Printed.toSysOut());
        final Topology topology = streamsBuilder.build();
        log.info(topology.describe().toString());
        return topology;
    }
}
