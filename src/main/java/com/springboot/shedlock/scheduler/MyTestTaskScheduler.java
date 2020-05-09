package com.springboot.shedlock.scheduler;

import lombok.SneakyThrows;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MyTestTaskScheduler implements Job {

    @SneakyThrows
    @Scheduled(cron = "0/5 * * * * ?")
    @SchedulerLock(name = "myTask", lockAtMostFor="200000", lockAtLeastFor="200000")
    @Override
    public void exec() {
        System.out.println("执行任务...");
        TimeUnit.SECONDS.sleep(10000);
    }
}
