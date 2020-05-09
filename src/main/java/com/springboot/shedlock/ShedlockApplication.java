package com.springboot.shedlock;

import com.springboot.shedlock.config.lock.RedisLockProviderEnhance;
import net.javacrumbs.shedlock.core.LockProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShedlockApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ShedlockApplication.class, args);
        RedisLockProviderEnhance lockProvider = context.getBean(RedisLockProviderEnhance.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            lockProvider.unlockByPrefix("job-lock:default:");
        }));
    }

}
