package com.holland.spring.cache.exam;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;

@EnableCaching
@Configuration
public class CacheConfig {
    @Resource
    private ApplicationContext ctx;

    @ConditionalOnMissingBean(CacheManager.class)
    @Bean
    public CacheManager cacheManager() {
        final CompositeCacheManager compositeCacheManager = new CompositeCacheManager();

        compositeCacheManager.setCacheManagers(Arrays.asList(
                caffeineCacheManager()
                , redisCacheManager()
        ));

        return compositeCacheManager;
    }

    public CaffeineCacheManager caffeineCacheManager() {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(Duration.ofSeconds(3))
                .weakKeys()
                .recordStats());
        return cacheManager;
    }

    public RedisCacheManager redisCacheManager() {
        final RedisConnectionFactory redisConnectionFactory = ctx.getBean(RedisConnectionFactory.class);

        final RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);

        final RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(6));

        return RedisCacheManager
                .builder(redisCacheWriter)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
