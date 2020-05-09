package com.springboot.shedlock.config.lock;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.support.LockException;
import net.javacrumbs.shedlock.support.annotation.NonNull;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

@Slf4j
public class RedisLockProviderEnhance extends RedisLockProvider {

    private final StringRedisTemplate redisTemplate;

    public RedisLockProviderEnhance(@NonNull RedisConnectionFactory redisConn) {
        super(redisConn);
        redisTemplate = new StringRedisTemplate(redisConn);
    }

    public void unlockByKey(String key) {
        try {

            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new LockException("Can not remove node", e);
        }
    }

    public void unlockByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            log.error("删除的分布式锁包括：" + keys);
            redisTemplate.delete(keys);
        } catch (Exception e) {
            throw new LockException("Can not remove node", e);
        }
    }

}
