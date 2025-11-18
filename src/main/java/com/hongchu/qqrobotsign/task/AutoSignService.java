package com.hongchu.qqrobotsign.task;

import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.service.SignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class AutoSignService {

    @Autowired
    private IUserService userService;
    @Autowired
    private SignService SignService;

    private final Random random = new Random();
    private boolean todaySigned = false; // 标记今天是否已经执行过签到

    /**
     * 简单的随机时间自动签到
     * 每天18:30-20:20之间随机一个时间点执行所有用户签到
     */
    @Scheduled(cron = "0 30 18 * * ?")  // 每天18:30触发，然后随机延迟
    public void randomTimeAutoSign() {
        if (todaySigned) {
            return; // 今天已经执行过了
        }

        log.info("开始随机时间自动签到任务...");

        // 计算18:30-20:20之间的随机分钟数 (0-110分钟)
        int randomMinutes = random.nextInt(111);  // 0到110分钟
        LocalTime executeTime = LocalTime.of(18, 30).plusMinutes(randomMinutes);

        log.info("计划在 {} 执行自动签到，随机延迟 {} 分钟", executeTime, randomMinutes);

        // 在实际项目中，这里可以使用更复杂的调度器
        // 简单实现：记录计划时间，由另一个定时任务检查执行
        scheduleSignTask(randomMinutes);
    }

    /**
     * 执行签到任务
     */
    private void scheduleSignTask(int delayMinutes) {
        try {
            // 简单实现：直接在当前线程睡眠延迟时间后执行
            Thread.sleep(delayMinutes * 60 * 1000L);

            log.info("到达随机时间，开始执行自动签到...");
            executeAllUserSign();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("自动签到任务被中断");
        }
    }

    /**
     * 执行所有用户签到
     */
    private void executeAllUserSign() {
        List<User> users = userService.list();
        if (users.isEmpty()) {
            log.info("没有需要自动签到的用户");
            return;
        }

        log.info("开始为 {} 个用户执行自动签到", users.size());

        int successCount = 0;
        int failCount = 0;

        for (User user : users) {
            try {
                // 为每个用户添加短暂随机延迟，避免同时请求
                int userDelay = random.nextInt(30); // 0-29秒随机延迟
                Thread.sleep(userDelay * 1000L);

                log.info("执行用户 {} 自动签到", user.getUsername());
                String result = SignService.signAll(user.getUsername());
                log.info("用户 {} 自动签到结果: {}", user.getUsername(), result);
                successCount++;

            } catch (Exception e) {
                log.error("用户 {} 自动签到失败: {}", user.getUsername(), e.getMessage());
                failCount++;
            }
        }

        todaySigned = true; // 标记今天已执行
        log.info("自动签到完成 - 成功: {}, 失败: {}", successCount, failCount);
    }

    /**
     * 每天零点重置签到状态
     */
    @Scheduled(cron = "0 0 0 * * ?")  // 每天00:00执行
    public void resetDailySign() {
        todaySigned = false;
        log.info("重置每日自动签到状态");
    }
}