package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;

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
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology)) {
            TestInputTopic<String, String> orderTopic = testDriver.createInputTopic("orders", String().serializer(), String().serializer());
            orderTopic.pipeInput(MAPPER.writeValueAsString(new Order()));
        }

    }
}