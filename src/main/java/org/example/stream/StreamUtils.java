package org.example.stream;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.example.config.CustomSerdes;
import org.example.model.Order;
import org.example.model.Payment;

@UtilityClass
@Slf4j
public class StreamUtils {
    public static KStream<Integer, Payment> getPaymentKStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream("payments",
                Consumed.with(Serdes.Integer(), CustomSerdes.Payment()).withTimestampExtractor((payment, l) -> {
                    Payment value = (Payment) payment.value();
                    long epochSecond = value.getCreatedOn().getEpochSecond();
                    if (epochSecond < l) {
                        throw new StreamsException("Not the right timing " + epochSecond);
                    }
                    return epochSecond;
                }).withName("PRO_PAYMENT"));
    }

    public static KStream<Integer, Order> getOrderStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream("orders",
                Consumed.with(Serdes.Integer(), CustomSerdes.Order()).withTimestampExtractor((consumerRecord, l) -> {
                    Order value = (Order) consumerRecord.value();
                    long epochSecond = value.getCreatedOn().getEpochSecond();
                    if (epochSecond < l) {
                        throw new StreamsException("Not the right timing " + epochSecond);
                    }
                    return epochSecond;
                }).withName("PRO_ORDER"));
    }
}
