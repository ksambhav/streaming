package org.example;

import org.apache.kafka.common.serialization.Serde;

import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

public class CustomSerdes {
    private CustomSerdes() {
    }

    public static Serde<Order> Order() {
        return serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Order.class));
    }

    public static Serde<Payment> Payment() {
        return serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Payment.class));
    }

    public static Serde<Confirmation> Confirmation() {
        return serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Confirmation.class));
    }
}
