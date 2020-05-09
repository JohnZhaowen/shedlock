package com.springboot.shedlock.config.lock;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "120s")
public class LockConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public LockProvider lockProvider() {
//        RedisLockProvider redisLockProvider = new RedisLockProvider(redisConnectionFactory);
        RedisLockProviderEnhance redisLockProvider = new RedisLockProviderEnhance(redisConnectionFactory);
        return redisLockProvider;
    }

}