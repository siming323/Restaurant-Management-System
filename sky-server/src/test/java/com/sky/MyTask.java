package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

/**
 * 自定义定时任务类
 */
//@Component
@Slf4j
//@SpringBootTest(classes = SkyApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MyTask {

    /**
     * 定时任务 每隔5秒触发一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    @Test
    @Repeat(value = 10)
    public void executeTask(){
        log.info("定时任务测试代码开始执行：{}",new Date());
    }
}