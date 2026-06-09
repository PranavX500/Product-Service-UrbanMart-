package com.example.Product_Service.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            RedisCacheConfig.class
    );
    private static final String PRODUCTS_CACHE = "products";
    private static final long CACHE_TTL_MINUTES = 10L;

    @Value("${app.cache.redis.enabled:true}")
    private boolean redisEnabled;

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.example.Product_Service")
                        .allowIfSubType("java.lang")
                        .allowIfSubType("java.util")
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(
                objectMapper,
                null
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(CACHE_TTL_MINUTES))
                .prefixCacheNameWith("v2::")
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                serializer
                        )
                );
    }

    @Bean
    public CacheManager cacheManager(
            final RedisConnectionFactory redisConnectionFactory,
            final RedisCacheConfiguration redisCacheConfiguration
    ) {
        ConcurrentMapCacheManager fallbackCacheManager =
                new ConcurrentMapCacheManager(PRODUCTS_CACHE);

        if (!redisEnabled) {
            LOGGER.info(
                    "Redis cache is disabled by configuration. Using in-memory cache."
            );
            return fallbackCacheManager;
        }

        if (!isRedisAvailable(redisConnectionFactory)) {
            LOGGER.warn("Redis is unavailable. Falling back to in-memory cache.");
            return fallbackCacheManager;
        }

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(
                redisConnectionFactory
        )
                .cacheDefaults(redisCacheConfiguration)
                .withCacheConfiguration(PRODUCTS_CACHE, redisCacheConfiguration)
                .build();

        LOGGER.info("Redis is available. Using Redis-backed cache.");
        return redisCacheManager;
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(
                    final RuntimeException exception,
                    final Cache cache,
                    final Object key
            ) {
                LOGGER.warn(
                        "Cache GET failed for cache '{}' and key '{}'. Continuing without cache.",
                        cacheName(cache),
                        key,
                        exception
                );
            }

            @Override
            public void handleCachePutError(
                    final RuntimeException exception,
                    final Cache cache,
                    final Object key,
                    final Object value
            ) {
                LOGGER.warn(
                        "Cache PUT failed for cache '{}' and key '{}'. Continuing without cache.",
                        cacheName(cache),
                        key,
                        exception
                );
            }

            @Override
            public void handleCacheEvictError(
                    final RuntimeException exception,
                    final Cache cache,
                    final Object key
            ) {
                LOGGER.warn(
                        "Cache EVICT failed for cache '{}' and key '{}'. Continuing without cache.",
                        cacheName(cache),
                        key,
                        exception
                );
            }

            @Override
            public void handleCacheClearError(
                    final RuntimeException exception,
                    final Cache cache
            ) {
                LOGGER.warn(
                        "Cache CLEAR failed for cache '{}'. Continuing without cache.",
                        cacheName(cache),
                        exception
                );
            }
        };
    }

    private boolean isRedisAvailable(
            final RedisConnectionFactory redisConnectionFactory
    ) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            return true;
        } catch (Exception exception) {
            LOGGER.warn(
                    "Redis health check failed during cache manager initialization: {}",
                    exception.getMessage()
            );
            return false;
        }
    }

    private String cacheName(final Cache cache) {
        return cache != null ? cache.getName() : "unknown";
    }
}
