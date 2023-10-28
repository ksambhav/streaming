package org.example.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.example.config.CustomSerdes;
import org.example.model.Order;
import org.example.model.Payment;

import java.time.Duration;

import static org.apache.kafka.streams.kstream.Branched.withConsumer;

@Slf4j
public class OrderPaymentStreamJoin {

    public static Topology createStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = StreamUtils.getOrderStream(streamsBuilder);
        KStream<Integer, Payment> paymentKStream = StreamUtils.getPaymentKStream(streamsBuilder);
        return getOrderPaymentTopology(orderStream, paymentKStream, streamsBuilder);
    }

    public static Topology getOrderPaymentTopology(KStream<Integer, Order> orderStream,
                                                   KStream<Integer, Payment> paymentKStream,
                                                   StreamsBuilder streamsBuilder) {
        ValueJoiner<Order, Payment, Order> valueJoiner = (order, payment) -> {
            order.setPaymentId(payment.getId());
            order.setPaymentSuccess(payment.isSuccess());
            return order;
        };
        StreamJoined<Integer, Order, Payment> joinedWith = StreamJoined
                .with(Serdes.Integer(), CustomSerdes.Order(), CustomSerdes.Payment())
                .withName("JOIN_ORDER_PAYMENT")
                .withStoreName("JOIN_ORDER_PAYMENT_STORE");
        KStream<Integer, Order> joined = orderStream.join(paymentKStream,
                valueJoiner,
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)),
                joinedWith
        );
        joined.split(Named.as("order_status"))
                .branch((orderId, joinedorder) -> joinedorder.isPaymentSuccess(), withConsumer(ks -> ks.to("success-order", Produced.with(Serdes.Integer(), CustomSerdes.Order()))))
                .defaultBranch(withConsumer(ks -> ks.to("failed-order", Produced.with(Serdes.Integer(), CustomSerdes.Order()))));
        Topology topology = streamsBuilder.build();
        log.info("{}", topology.describe());
        return topology;
    }

}
