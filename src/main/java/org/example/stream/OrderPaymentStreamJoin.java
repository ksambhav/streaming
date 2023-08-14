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

@Slf4j
public class OrderPaymentStreamJoin {

    public Topology createStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, Order> orderStream = StreamUtils.getOrderStream(streamsBuilder);
//        orderStream.print(Printed.toSysOut());
        KStream<Integer, Payment> paymentKStream = StreamUtils.getPaymentKStream(streamsBuilder);
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
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)),
                joinedWith
        );

        join.print(Printed.toSysOut());

        Topology topology = streamsBuilder.build();
        log.info("{}", topology.describe());
        return topology;
    }

}
