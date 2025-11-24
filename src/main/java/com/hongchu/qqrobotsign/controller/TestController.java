package com.hongchu.qqrobotsign.controller;

import com.hongchu.qqrobotsign.exception.BusinessException;
import com.hongchu.qqrobotsign.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 测试控制器
 * </p>
 *
 * @author hongchu
 * @since 2025-11-24
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    /**
     * pressTest 压力测试
     * @return 成功
     */
    @GetMapping("/pressTest")
    public Result<String> pressTest() {
        log.info("controller层-pressTest............");
        // 插入用户信息
        return Result.success("测试成功! ");
    }
}
