package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;

@Slf4j
public class OrderPaymentStream {

    public Topology createStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = getOrderStream(streamsBuilder);
//        orderStream.print(Printed.toSysOut());
        KStream<Integer, Payment> paymentKStream = getPaymentKStream(streamsBuilder);
//        paymentKStream.print(Printed.toSysOut());


        ValueJoiner<Order, Payment, Order> valueJoiner = (order, payment) -> {
            order.setPaymentId(payment.getId());
            order.setPaymentSuccess(payment.isSuccess());
            return order;
        };
        StreamJoined<Integer, Order, Payment> joinedWith = StreamJoined
                .with(Serdes.Integer(), CustomSerdes.Order(), CustomSerdes.Payment())
                .withName("JOIN_ORDER_PAYMENT")
                .withStoreName("JOIN_ORDER_PAYMENT_STORE");
        KStream<Integer, Order> join = orderStream.join(paymentKStream,
                valueJoiner,
                JoinWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(999), Duration.ofSeconds(100)),
                joinedWith
        );

        join.print(Printed.toSysOut());

        Topology topology = streamsBuilder.build();
        log.info("{}", topology.describe());
        return topology;
    }

    private static KStream<Integer, Payment> getPaymentKStream(StreamsBuilder streamsBuilder) {
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

    private static KStream<Integer, Order> getOrderStream(StreamsBuilder streamsBuilder) {
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
