package com.springboot.shedlock.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "shedlock.redis")
public class RedisProperties {
    public enum RedisMode {
        SENTINEL, STANDALONE, CLUSTER
    }

    private String host;
    private String port = "6379";
    private String password;
    private RedisMode mode = RedisMode.STANDALONE;
    private Duration timeout;
    private String masterName;
}
