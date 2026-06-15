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
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

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
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG ,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG ,
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
