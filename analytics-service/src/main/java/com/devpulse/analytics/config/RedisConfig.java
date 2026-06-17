package com.devpulse.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration.
 *
 * WHY CUSTOM SERIALIZER:
 * Default Redis serializer uses Java serialization — binary format,
 * not human-readable, breaks if class structure changes.
 * JSON serialization means you can inspect cache values in
 * Redis CLI and they survive class refactoring.
 */
@Configuration
public class RedisConfig {

    /**  create the template and give it the connection*/
    @Bean
    public RedisTemplate<String, Object> redisTemplate (RedisConnectionFactory connectionFactory){

        RedisTemplate<String , Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer( new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer  jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper());

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }


    /**Meaning: "Use this Redis connection and apply these defaults to all caches"*/
    @Bean
    public RedisCacheManager  cacheManager(RedisConnectionFactory connectionFactory){

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config).build();
    }


    // Object mapper convert Java object to JSON
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Support for Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
