package com.hongchu.qqrobotsign.task;

import com.hongchu.qqrobotsign.pojo.entity.TaskConfig;
import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.service.ITaskConfigService;
import com.hongchu.qqrobotsign.service.SignService;
import com.hongchu.qqrobotsign.service.impl.TaskConfigServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
@DependsOn("taskConfigServiceImpl")
public class SchedulingManager {

    @Autowired private TaskScheduler taskScheduler;
    @Autowired private ITaskConfigService taskConfigService;
    @Autowired private SignService signService;
    @Autowired private IUserService userService;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private static final String DELAY_QUEUE = "sign:queue";

    @PostConstruct
    public void init() {
        log.info("==================== SchedulingManager @PostConstruct 开始初始化 ====================");
        log.info("taskScheduler 注入状态: {}", taskScheduler != null ? "已注入" : "未注入!!!");
        log.info("taskConfigService 注入状态: {}", taskConfigService != null ? "已注入" : "未注入!!!");
        refreshAllTasks();
        log.info("==================== SchedulingManager 初始化完成 ====================");
    }

    /**
     * 刷新所有定时任务（配置变更后调用）
     */
    public void refreshAllTasks() {
        log.info("==================== SchedulingManager-开始刷新所有定时任务 ====================");

        cancelAllTasks();

        List<TaskConfig> configs = ((TaskConfigServiceImpl) taskConfigService).list();
        log.info("从数据库查询到 {} 条任务配置", configs.size());
        for (TaskConfig config : configs) {
            log.info("检查任务配置: taskKey={}, taskName={}, enabled={}, cron={}",
                    config.getTaskKey(), config.getTaskName(), config.getEnabled(), config.getCronExpression());
            if (Boolean.TRUE.equals(config.getEnabled())
                    && config.getCronExpression() != null
                    && !config.getCronExpression().isEmpty()) {
                registerTask(config);
            } else {
                log.warn("跳过任务 {}: enabled={}, cron={}", config.getTaskKey(), config.getEnabled(), config.getCronExpression());
            }
        }

        log.info("==================== 定时任务刷新完成，当前已注册 {} 个任务 ====================", scheduledTasks.size());
        scheduledTasks.forEach((key, future) -> {
            log.info("  已注册任务: {} -> isDone={}, isCancelled={}", key, future.isDone(), future.isCancelled());
        });
    }

    private void registerTask(TaskConfig config) {
        log.info(">>> 开始注册任务: taskKey={}, taskName={}, cron={} <<<",
                config.getTaskKey(), config.getTaskName(), config.getCronExpression());

        Runnable task = switch (config.getTaskKey()) {
            case "schedule_users" -> () -> executeScheduleUsers();
            case "interval_sign" -> () -> executeIntervalSign();
            case "refresh_jws" -> () -> executeRefreshJws(config);
            default -> null;
        };

        if (task == null) {
            log.warn("未知任务类型: {}", config.getTaskKey());
            return;
        }

        try {
            ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(config.getCronExpression()));
            scheduledTasks.put(config.getTaskKey(), future);
            log.info("✅✅✅ 任务注册成功: {} ({}), cron: {}, 下次执行时间不确定(需看cron解析)",
                    config.getTaskName(), config.getTaskKey(), config.getCronExpression());
        } catch (Exception e) {
            log.error("❌❌❌ 注册任务失败: {} - {}", config.getTaskKey(), e.getMessage(), e);
        }
    }

    // ==================== 任务执行逻辑 ====================

    /**
     * 调度用户：筛选需要签到的用户，加入Redis延迟队列
     */
    private void executeScheduleUsers() {
        log.info("📋📋📋 定时触发-调度用户签到!!! 线程: {}, 时间: {}", Thread.currentThread().getName(), new java.util.Date());
        try {
            ((TaskConfigServiceImpl) taskConfigService).triggerImmediateSchedule(true);
            log.info("📋📋📋 调度用户签到-执行完成");
        } catch (Exception e) {
            log.error("调度用户签到失败", e);
        }
    }

    /**
     * 轮询Redis延迟队列，执行到期的签到任务
     */
    private void executeIntervalSign() {
        log.info("⏰⏰⏰ 间隔签到-触发!!! 线程: {}, 时间: {}", Thread.currentThread().getName(), new java.util.Date());
        try {
            long now = System.currentTimeMillis();
            // 先查一下队列里有多少数据
            Long queueSize = redisTemplate.opsForZSet().size(DELAY_QUEUE);
            log.info("间隔签到-Redis队列总大小: {}, 当前时间戳: {}", queueSize, now);

            Set<Object> readyUsers = redisTemplate.opsForZSet().rangeByScore(DELAY_QUEUE, 0, now);
            if (readyUsers == null || readyUsers.isEmpty()) {
                log.info("间隔签到检查-队列中无到期用户 (队列总大小={})", queueSize);
                return;
            }

            log.info("⏰⏰⏰ 定时触发-从Redis队列取出 {} 个待签到用户", readyUsers.size());
            for (Object obj : readyUsers) {
                String username = obj.toString();
                try {
                    redisTemplate.opsForZSet().remove(DELAY_QUEUE, username);
                    log.info("开始执行用户 {} 签到", username);
                    String result = signService.signAll(username);
                    log.info("用户 {} 签到结果: {}", username, result);
                } catch (Exception e) {
                    log.error("用户 {} 签到执行失败", username, e);
                }
            }
        } catch (Exception e) {
            log.error("间隔签到执行失败", e);
        }
    }

    /**
     * JWS续签：按间隔周数刷新所有用户的JWSESSION
     */
    private void executeRefreshJws(TaskConfig config) {
        log.info("定时触发-JWS续签检查");
        try {
            if (!((TaskConfigServiceImpl) taskConfigService).shouldRefreshJwsToday(config)) {
                log.info("今天不是JWS续签日，跳过");
                return;
            }

            List<User> users = userService.list();
            log.info("开始续签 {} 个用户的JWS", users.size());
            for (User user : users) {
                try {
                    userService.refreshJws(user.getUsername());
                    log.info("用户 {} JWS续签成功", user.getUsername());
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("用户 {} JWS续签失败", user.getUsername(), e);
                }
            }
            log.info("JWS续签完成");
        } catch (Exception e) {
            log.error("JWS续签执行失败", e);
        }
    }

    // ==================== 任务管理 ====================

    private void cancelAllTasks() {
        scheduledTasks.forEach((name, future) -> {
            if (future != null && !future.isCancelled()) {
                future.cancel(false);
                log.info("已取消任务: {}", name);
            }
        });
        scheduledTasks.clear();
    }

    public Map<String, Boolean> getRunningTasks() {
        Map<String, Boolean> status = new LinkedHashMap<>();
        scheduledTasks.forEach((name, future) ->
                status.put(name, future != null && !future.isCancelled() && !future.isDone()));
        return status;
    }
}
