package com.github.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfiguration {
    public static final String PROJECT_ID_CACHE_KEY = "project-id";

    private final String redisHost;
    private final Integer redisPort;

    public CacheConfiguration(
            @Value("${spring.data.redis.host}") String redisHost,
            @Value("${spring.data.redis.port}") Integer redisPort) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .shutdownTimeout(Duration.ZERO)
                .build();
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort), clientConfig);
    }

    @Bean
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        RedisCacheConfiguration commonCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> initialCacheConfigs =
                Collections.singletonMap(PROJECT_ID_CACHE_KEY, commonCacheConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(initialCacheConfigs)
                .transactionAware()
                .build();
    }
}