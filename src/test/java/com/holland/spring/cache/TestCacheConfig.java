package com.holland.spring.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;

@EnableCaching
@Configuration
public class TestCacheConfig {
    @Resource
    private ApplicationContext ctx;

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
        // 修改源码，以便于查看值的来源
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager() {
            @NonNull
            @Override
            protected Cache adaptCaffeineCache(@NonNull String name, @NonNull com.github.benmanes.caffeine.cache.Cache<Object, Object> cache) {
                return new CaffeineCache(name, cache, isAllowNullValues()) {
                    @Override
                    protected Object lookup(@NonNull Object key) {
                        final Object lookup = super.lookup(key);
                        if (null != lookup) {
                            System.err.println("Form caffeine cache");
                        }
                        return lookup;
                    }
                };
            }
        };
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

        // 修改源码，以便于查看值的来源
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration, true) {
            @NonNull
            @Override
            protected RedisCache createRedisCache(@NonNull String name, RedisCacheConfiguration cacheConfig) {
                return new TestRedisCache(name, redisCacheWriter, cacheConfig);
            }
        };
    }
}
