package com.devpulse.ai.config;

import com.devpulse.ai.dto.ErrorSpikeMessage;
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

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;


    @Bean
    public ConsumerFactory<String, ErrorSpikeMessage> consumerFactory(){
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG , bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG , "ai-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG , "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG , ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG , ErrorHandlingDeserializer.class);


        // Tell ErrorHandlingDeserializer what the actual deserializers are
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES , "com.devpulse.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE , ErrorSpikeMessage.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS , false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(ErrorSpikeMessage.class , false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String , ErrorSpikeMessage> kafkaListenerContainerFactory(){

        ConcurrentKafkaListenerContainerFactory<String, ErrorSpikeMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
