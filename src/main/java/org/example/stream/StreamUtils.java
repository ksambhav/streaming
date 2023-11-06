package org.example.stream;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.example.model.Order;
import org.example.model.Payment;

import java.util.Properties;

import static org.apache.kafka.common.serialization.Serdes.Integer;
import static org.apache.kafka.common.serialization.Serdes.String;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.apache.kafka.streams.kstream.Consumed.with;
import static org.example.config.CustomSerdes.Order;
import static org.example.config.CustomSerdes.Payment;

@UtilityClass
@Slf4j
public class StreamUtils {
    public static KStream<Integer, Payment> getPaymentKStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream("payments",
                with(Integer(), Payment()).withName("PROCESSOR_PAYMENT"));
    }

    public static KStream<Integer, Order> getOrderStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream("orders",
                with(Integer(), Order()).withName("PROCESSOR_ORDER"));
    }

    public static Properties getProperties() {
        Properties config = new Properties();
        config.setProperty(APPLICATION_ID_CONFIG, "samsoft");
        config.setProperty(DEFAULT_KEY_SERDE_CLASS_CONFIG, Integer().getClass().getName());
        config.setProperty(DEFAULT_VALUE_SERDE_CLASS_CONFIG, String().getClass().getName());
        config.setProperty(STATE_DIR_CONFIG, "state-store");
        return config;
    }
}
