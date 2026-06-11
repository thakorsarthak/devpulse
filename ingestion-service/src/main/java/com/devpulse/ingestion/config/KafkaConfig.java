package com.devpulse.ingestion.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


/**
 * Kafka topic configuration.
 *
 * WHY DECLARE TOPICS IN CODE:
 * auto.create.topics.enable=true would create topics
 * automatically but with default settings (1 partition,
 * replication factor 1).
 *
 * Declaring topics explicitly gives us control over:
 * - Partition count (affects parallelism)
 * - Replication factor (affects fault tolerance)
 * - Retention policy
 *
 * Remember: "Partition count determines how many consumers in a group can process in parallel.
 * 3 partitions = max 3 consumers processing concurrently."
 * For local dev: 1 partition, 1 replica is fine.
 * For AWS MSK production: 3 partitions, 3 replicas.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic pushReceivedTopic(){
        return TopicBuilder
                .name("push.received")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic errorSpikeDetectedTopic(){
        return TopicBuilder
                .name("error.spike.detected")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic aiIncidentAnalyzedTopic(){
        return TopicBuilder
                .name("ai.incident.analyzed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deploymentDetectedTopic(){
        return TopicBuilder
                .name("deployment.detected")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
