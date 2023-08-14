package org.example.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.example.config.CustomSerdes;
import org.example.model.Order;
import org.instancio.InstancioApi;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Properties;
import java.util.stream.Stream;

import static org.example.stream.OrderPaymentStreamJoinTest.ORDER_COUNT;

class OrderGroupStreamTest {

    @Test
    void createTopology() {
        Instant reference = Instant.now().minusSeconds(9999999);
        var timestampSupplier = new TimestampSupplier(reference, 0);
        InstancioApi<Order> orderInstancioApi = MockDataUtil.getGenerate(timestampSupplier);
        Properties config = StreamUtils.getProperties();
        Topology topology = new OrderGroupStream().createTopology();
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, config)) {
            TestInputTopic<Integer, Order> orderTopic = testDriver
                    .createInputTopic("orders", Serdes.Integer().serializer(), CustomSerdes.Order().serializer());
            Stream<Order> order = orderInstancioApi.stream().limit(ORDER_COUNT);
            order.toList().forEach(o -> orderTopic.pipeInput(o.getId(), o));
        }
    }
}