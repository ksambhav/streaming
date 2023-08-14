package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apache.kafka.common.serialization.Serdes.String;
import static org.instancio.Select.field;

class OrderPaymentStreamTest {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final int ORDER_COUNT = 1000;


    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    public static class TimestampSupplier implements Supplier<Instant> {

        public static final Instant referenceTimeStamp;
        private int offset = 0;

        static {
            referenceTimeStamp = Instant.now().minusSeconds(99999999L);
        }

        @Override
        public Instant get() {
            return referenceTimeStamp.plusSeconds(offset++);
        }
    }


    @Test
    void createStream() throws JsonProcessingException {

        final Instant[] reference = {Instant.now().minusSeconds(9999999L)};
        InstancioApi<Order> orderInstancioApi = Instancio.of(Order.class)
                .generate(field(Order::getDescription), gen -> gen.text().loremIpsum().words(10))
                .supply(field(Order::getCreatedOn), TimestampSupplier::new);

        OrderPaymentStream stream = new OrderPaymentStream();
        Topology topology = stream.createStream();
        Properties config = new Properties();
        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "samsoft");
        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, String().getClass().getName());
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, config)) {
            TestInputTopic<Integer, Order> orderTopic = testDriver.createInputTopic("orders", Serdes.Integer().serializer(), CustomSerdes.Order().serializer());
            Stream<Order> order = orderInstancioApi.stream().limit(ORDER_COUNT);
            order.toList().forEach(o -> orderTopic.pipeInput(o.getId(), o));
        }

    }
}