package org.example;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.Comparator;
import java.util.List;

@Slf4j
@SpringBootTest
@EmbeddedKafka(topics = {"orders", "payments"})
class MyApplicationTest {

    @Test
    void test(@Autowired MeterRegistry meterRegistry) {
        List<Meter> meterList = meterRegistry.getMeters().stream()
                .sorted(Comparator.comparing(meter -> meter.getId().getName())).toList();
        meterList.forEach(meter -> {
            log.info("{},{}", meter.getId().getName(), meter.getId().getDescription());
        });
    }

}