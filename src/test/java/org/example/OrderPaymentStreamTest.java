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
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.stream.Stream;

import static org.apache.kafka.common.serialization.Serdes.String;

class OrderPaymentStreamTest {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    void createStream() throws JsonProcessingException {
        OrderPaymentStream stream = new OrderPaymentStream();
        Topology topology = stream.createStream();
        Properties config = new Properties();
        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "samsoft");
        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, String().getClass().getName());
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, config)) {
            TestInputTopic<Integer, Order> orderTopic = testDriver.createInputTopic("orders", Serdes.Integer().serializer(), CustomSerdes.Order().serializer());
            Stream<Order> order = Instancio.stream(Order.class).limit(10);
            order.toList().forEach(o -> orderTopic.pipeInput(o.getId(), o));
        }

    }
}