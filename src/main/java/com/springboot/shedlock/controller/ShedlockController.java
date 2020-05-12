package com.springboot.shedlock.controller;

import com.springboot.shedlock.config.lock.RedisLockProviderEnhance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shedlock")
public class ShedlockController {

    @Autowired
    private RedisLockProviderEnhance redisLockProviderEnhance;

    @GetMapping("/unlock")
    public String unlock(){
        redisLockProviderEnhance.unlockByPrefix("job-lock:default:");
        return "success";
    }
}
