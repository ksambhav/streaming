package org.example.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.example.config.CustomSerdes;
import org.example.mock.MockDataUtil;
import org.example.mock.TimestampSupplier;
import org.example.model.Order;
import org.example.model.Payment;
import org.instancio.InstancioApi;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Properties;
import java.util.stream.Stream;

@Slf4j
class OrderPaymentStreamJoinTest {


    @Test
    void testJoin() {
        Instant reference = Instant.now().minusSeconds(9999999);
        var timestampSupplier = new TimestampSupplier(reference, 0);
        InstancioApi<Order> orderInstancioApi = MockDataUtil.getGenerate(timestampSupplier, 1);
        TimestampSupplier paymentTimestampSupplier = new TimestampSupplier(reference, 5);
        InstancioApi<Payment> paymentInstancioApi = MockDataUtil.getPaymentSupplier(paymentTimestampSupplier, 1);
        Topology topology = OrderPaymentStreamJoin.createStream();
        Properties config = StreamUtils.getProperties();
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, config)) {
            var failedOrders = testDriver.createOutputTopic("failed-order", Serdes.Integer().deserializer(), CustomSerdes.Order().deserializer());
            var successOrders = testDriver.createOutputTopic("success-order", Serdes.Integer().deserializer(), CustomSerdes.Order().deserializer());
            TestInputTopic<Integer, Order> orderTopic = testDriver.createInputTopic("orders", Serdes.Integer().serializer(), CustomSerdes.Order().serializer());
            Stream<Order> order = orderInstancioApi.stream().limit(MockDataUtil.ORDER_COUNT);
            order.toList().forEach(o -> orderTopic.pipeInput(o.getId(), o));
            //payments
            TestInputTopic<Integer, Payment> paymentTopic = testDriver.createInputTopic("payments", Serdes.Integer().serializer(), CustomSerdes.Payment().serializer());
            paymentInstancioApi.stream().limit(MockDataUtil.ORDER_COUNT).toList().forEach(p -> paymentTopic.pipeInput(p.getOrderId(), p));
            while (!successOrders.isEmpty()) {
                log.debug("{}", successOrders.readValue());
            }
        }

    }

}