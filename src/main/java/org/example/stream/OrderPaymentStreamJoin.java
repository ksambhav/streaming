package org.example.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.example.model.Order;
import org.example.model.Payment;

import java.time.Duration;

import static org.apache.kafka.streams.kstream.Branched.withConsumer;
import static org.apache.kafka.streams.kstream.JoinWindows.ofTimeDifferenceWithNoGrace;
import static org.apache.kafka.streams.kstream.Produced.with;
import static org.example.config.CustomSerdes.Order;
import static org.example.config.CustomSerdes.Payment;

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
            log.debug("Joined {} with {} with success={}", order.getId(), payment.getId(), payment.isSuccess());
            return order;
        };
        StreamJoined<Integer, Order, Payment> joinedWith = StreamJoined
                .with(Serdes.Integer(), Order(), Payment())
                .withName("JOIN_ORDER_PAYMENT")
                .withStoreName("JOIN_ORDER_PAYMENT_STORE");
        //orderStream is the primary operator of this join
        KStream<Integer, Order> joined = orderStream.join(paymentKStream,
                valueJoiner,
                ofTimeDifferenceWithNoGrace(Duration.ofSeconds(100)),
                joinedWith
        );
        joined.split(Named.as("order_status"))
                .branch((orderId, joinedorder) -> joinedorder.isPaymentSuccess(),
                        withConsumer(ks -> ks.to("success-order", with(Serdes.Integer(), Order()))))
                .defaultBranch(withConsumer(ks -> ks.to("failed-order", with(Serdes.Integer(), Order()))));
        Topology topology = streamsBuilder.build();
        log.info("{}", topology.describe());
        return topology;
    }

}
