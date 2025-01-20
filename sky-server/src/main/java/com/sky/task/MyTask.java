package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyTask {
    //1.启动类 加 @EnableScheduling //开启定时任务调度
    //2.对应方法上加 @Scheduled + cron表达式（秒 分 时 日 月 周 年（可选））



//    @Scheduled(cron = "*/5 * * * * ?")
//    public void task(){
//        log.info("触发一次");
//    }
}
