package com.devpulse.analytics.config;


import com.devpulse.analytics.dto.PushEventMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration.
 *
 * WHY EXPLICIT CONSUMER CONFIG OVER AUTO-CONFIGURATION:
 * Spring Boot auto-configures Kafka consumers from properties.
 * Explicit config gives us control over:
 * - Type mapping (which JSON maps to which class)
 * - Trusted packages (security - only deserialize our classes)
 * - Concurrency (how many threads process messages)
 *
 * WHY setConcurrency(3):
 * 3 threads = matches our 3 partitions on push.received topic.
 * One thread per partition = maximum parallelism.
 * More threads than partitions = idle threads (waste).
 * Fewer threads than partitions = under-utilization.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-server:localhost:9092}")
    private String bootstrapServer;


    @Bean
    public ConsumerFactory<String, PushEventMessage> consumerFactory(){

        Map<String,Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG , bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG , "analytics-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG ,"earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG , ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG , ErrorHandlingDeserializer.class);


        // Tell ErrorHandlingDeserializer what the actual deserializers are
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);


        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.devpulse.*");
        props.put(JsonDeserializer.KEY_DEFAULT_TYPE , PushEventMessage.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS , false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(PushEventMessage.class , false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String , PushEventMessage> kafkaListenerContainerFactory(){

        ConcurrentKafkaListenerContainerFactory<String, PushEventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        /*Telling spring to Use the consumer configuration we just defined*/
        factory.setConsumerFactory(consumerFactory());
        // 3 threads = matches 3 partitions on the topic
        factory.setConcurrency(3);

        return factory;
    }
}
