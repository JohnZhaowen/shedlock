package com.springboot.shedlock.config.scheduler;

import com.springboot.shedlock.scheduler.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private List<Job> jobs;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(getCount());
        return taskScheduler;
    }

    public int getCount() {
        int count;

        if(CollectionUtils.isEmpty(jobs)){
            count = 1;
        } else if(jobs.size() > 10){
            count = 10;
        } else {
            count = jobs.size();
        }
        return count;
    }

}
