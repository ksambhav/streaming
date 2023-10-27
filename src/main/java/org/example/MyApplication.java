package org.example;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.example.mock.MockDataUtil;
import org.example.model.Order;
import org.example.model.Payment;
import org.example.stream.OrderPaymentStreamJoin;
import org.example.stream.StreamUtils;
import org.instancio.InstancioApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.example.mock.MockDataUtil.getPaymentSupplier;

@Slf4j
@EnableKafka
@EnableScheduling
@EnableKafkaStreams
@SpringBootApplication
public class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    @Bean
    public KStream<Integer, Payment> paymentStream(StreamsBuilder streamsBuilder) {
        log.info("creating payment kafka stream");
        return StreamUtils.getPaymentKStream(streamsBuilder);
    }

    @Bean
    public KStream<Integer, Order> orderStream(StreamsBuilder streamsBuilder) {
        log.debug("creating order kafka stream");
        return StreamUtils.getOrderStream(streamsBuilder);
    }

    @Bean
    public Topology orderPaymentTopology(KStream<Integer, Payment> paymentStream,
                                         KStream<Integer, Order> orderStream,
                                         StreamsBuilder streamsBuilder) {
        log.info("Creating topology");
        return OrderPaymentStreamJoin.getOrderPaymentTopology(orderStream, paymentStream, streamsBuilder);
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig(KafkaProperties kafkaProperties) {
        log.info("Creating defaultKafkaStreamsConfig using {}", kafkaProperties.getBootstrapServers().get(0));
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "streams-app-v6");
        props.put(NUM_STREAM_THREADS_CONFIG, 2);
        props.put(PROCESSING_GUARANTEE_CONFIG, EXACTLY_ONCE_V2);
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers().get(0));
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer streamMonitoringCustomizer(MeterRegistry meterRegistry) {
        return factoryBean -> factoryBean.addListener(new KafkaStreamsMicrometerListener(meterRegistry));
    }

    @Component
    @Slf4j
    @RequiredArgsConstructor
    public static class MockDataFeeder {

        private int orderId = 1;

        public static final int BATCH_SIZE = 200_000;

        private final Supplier<Instant> currenTimestampSupplier = Instant::now;


        private final KafkaTemplate<Integer, Object> kafkaTemplate;


        @SneakyThrows
        @Scheduled(fixedDelay = 10, timeUnit = SECONDS, initialDelay = 30)
        void sendMockData() {
            InstancioApi<Order> orderInstancioApi = MockDataUtil.getGenerate(currenTimestampSupplier, orderId);
            log.info("Sending mock order data");
            orderInstancioApi.stream().limit(BATCH_SIZE).forEach(order -> kafkaTemplate.send("orders", order.getId(), order));
            Thread.sleep(5000);
            log.info("Sending mock payment data");
            InstancioApi<Payment> paymentInstancioApi = getPaymentSupplier(currenTimestampSupplier, orderId);
            paymentInstancioApi.stream().limit(BATCH_SIZE).forEach(payment -> kafkaTemplate.send("payments", payment.getOrderId(), payment));
            orderId += BATCH_SIZE;
        }


    }
}
