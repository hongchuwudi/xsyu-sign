package com.hongchu.qqrobotsign;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hongchu.qqrobotsign.**.mapper")
@EnableScheduling
@Slf4j
public class QqRobotSignApplication {
    public static void main(String[] args) {
        SpringApplication.run(QqRobotSignApplication.class, args);
        log.info("==============春天的鞋启动成功==============");
    }
}
