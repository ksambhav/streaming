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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apache.kafka.common.serialization.Serdes.String;
import static org.instancio.Select.field;

class OrderPaymentStreamJoinTest {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final int ORDER_COUNT = 10;


    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    public static class TimestampSupplier implements Supplier<Instant> {

        private final Instant referenceTimeStamp;
        private int offset = 0;


        public TimestampSupplier(Instant referenceTimeStamp, int initialOffsetSecs) {
            this.referenceTimeStamp = referenceTimeStamp;
            this.offset = initialOffsetSecs;
        }

        @Override
        public Instant get() {
            return referenceTimeStamp.plusSeconds(offset++);
        }
    }


    @Test
    void createStream() throws JsonProcessingException {
        Instant reference = Instant.now().minusSeconds(9999999);
        var timestampSupplier = new TimestampSupplier(reference, 0);
        InstancioApi<Order> orderInstancioApi = Instancio.of(Order.class)
                .generate(field(Order::getId), gen -> gen.intSeq().start(1))
                .supply(field(Order::getCreatedOn), timestampSupplier)
                .generate(field(Order::getDescription), gen -> gen.text().loremIpsum().words(10));

        TimestampSupplier paymentTimestampSupplier = new TimestampSupplier(reference, 5);
        InstancioApi<Payment> paymentInstancioApi = Instancio.of(Payment.class)
                .generate(field(Payment::getId), gen -> gen.intSeq().start(10))
                .generate(field(Payment::getOrderId), gen -> gen.intSeq().start(1))
                .generate(field(Payment::getAmount), gen -> gen.doubles().range((double) 0, 1000.0).as(BigDecimal::valueOf))
                .supply(field(Payment::getCreatedOn), paymentTimestampSupplier);


        OrderPaymentStreamJoin stream = new OrderPaymentStreamJoin();
        Topology topology = stream.createStream();
        Properties config = getProperties();
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, config)) {
            TestInputTopic<Integer, Order> orderTopic = testDriver.createInputTopic("orders", Serdes.Integer().serializer(), CustomSerdes.Order().serializer());
            Stream<Order> order = orderInstancioApi.stream().limit(ORDER_COUNT);
            order.toList().forEach(o -> orderTopic.pipeInput(o.getId(), o));
            //payments
            TestInputTopic<Integer, Payment> paymentTopic = testDriver.createInputTopic("payments", Serdes.Integer().serializer(), CustomSerdes.Payment().serializer());
            paymentInstancioApi.stream().limit(ORDER_COUNT).toList().forEach(p -> paymentTopic.pipeInput(p.getOrderId(), p));
        }

    }

    private static Properties getProperties() {
        Properties config = new Properties();
        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "samsoft");
        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, String().getClass().getName());
        config.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/Users/sambhav.jain/work/poc/stream-joins/stream-joins/state-store");
        return config;
    }
}