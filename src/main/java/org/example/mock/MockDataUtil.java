package org.example.mock;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.example.model.Payment;
import org.instancio.Instancio;
import org.instancio.InstancioApi;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static org.instancio.Select.field;

@Slf4j
@UtilityClass
public class MockDataUtil {
    public static final int ORDER_COUNT = 100;
    private static final List<String> ITEMS = List.of("Laptop",
            "Mobile",
            "PC",
            "Router",
            "Keyboard",
            "Graphics Card",
            "Mice",
            "Tablet",
            "Speakers");

    public static InstancioApi<Payment> getPaymentSupplier(Supplier<Instant> paymentTimestampSupplier, int orderIdSeedValue) {
        return Instancio.of(Payment.class)
                .generate(field(Payment::getId), gen -> gen.intSeq().start(10))
                .generate(field(Payment::getOrderId), gen -> gen.intSeq().start(orderIdSeedValue))
                .generate(field(Payment::getAmount), gen -> gen.doubles().range((double) 0, 1000.0).as(BigDecimal::valueOf))
                .supply(field(Payment::getCreatedOn), paymentTimestampSupplier);
    }

    public static InstancioApi<Order> getGenerate(Supplier<Instant> timestampSupplier, int orderIdSeedValue) {
        return Instancio.of(Order.class)
                .generate(field(Order::getId), gen -> gen.intSeq().start(orderIdSeedValue))
                .generate(field(Order::getItemDescription), gen -> gen.oneOf(ITEMS))
                .supply(field(Order::getCreatedOn), timestampSupplier);
    }
}
