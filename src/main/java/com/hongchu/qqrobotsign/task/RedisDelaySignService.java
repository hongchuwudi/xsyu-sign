package com.hongchu.qqrobotsign.task;

import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.service.EmailService;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.service.SignService;
import com.hongchu.qqrobotsign.utils.SimpleCryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisDelaySignService {

    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private IUserService userService;
    @Autowired private SignService signService;
    @Autowired private EmailService emailService;
    private final Random random = new Random();
    private static final String DELAY_QUEUE = "sign:queue";

    /**
     * 周日-周四 18:31开始调度所有用户（周五周六不执行）
     */
    @Scheduled(cron = "0 31 18 * * 0,1,2,3,4")
    public void startDailySign() {
        log.info("task层-开始每日签到调度");

        // 清空队列
        redisTemplate.delete(DELAY_QUEUE);

        // 筛选出来需要自动签到的用户
        List<User> users = userService.list().stream()
                .filter(user -> Boolean.TRUE.equals(user.getAutoSign()))
                .toList();

        // 添加到队列中
        for (User user : users) {
            // 每个用户随机1-30分钟延迟
            int delayMinutes = 1 + random.nextInt(30);
            long executeTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedTime = sdf.format(new Date(executeTime));

            redisTemplate.opsForZSet().add(DELAY_QUEUE, user.getUsername(), executeTime);
            log.info("task层-用户 {} - {}分钟后签到", user.getUsername(), delayMinutes);
            emailService.sendScheduleNotice(user.getEmail(),user.getUsername(),formattedTime);
        }
    }

    /**
     * 每分钟检查并执行签到
     */
    @Scheduled(cron = "0 */1 18-20 * * ?")
    public void checkAndSign() {
        log.info("task层-{}-开始检查签到", LocalTime.now());
        Set<Object> readyUsers = redisTemplate.opsForZSet().rangeByScore(
                DELAY_QUEUE, 0, System.currentTimeMillis()
        );

        if (readyUsers.isEmpty()) {
            log.info("task层-{}-当前时刻无用户需要签到", LocalTime.now());
            return;
        }

        for (Object username : readyUsers) {
            signService.signAll((String) username);
            redisTemplate.opsForZSet().remove(DELAY_QUEUE, username);
            log.info("task层-用户 {} - 签到完成", username);
        }
    }

    /**
     *  周五进行jws续签并存入数据库
     */
    @Scheduled(cron = "0 0 18 * * 5")
    public void AllRefresh(){
        log.info("task层-{}-开始续签jws", LocalTime.now());

        // 1. 查询所有用户信息
        List<User> users = userService.list();

        // 2. 对每个用户进行续签工作
        users.forEach(user ->{
            String username = user.getUsername();
            userService.refreshJws(username);
        });
    }
}