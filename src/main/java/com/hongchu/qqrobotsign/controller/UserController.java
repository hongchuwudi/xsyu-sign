package com.hongchu.qqrobotsign.controller;


import com.hongchu.qqrobotsign.result.Result;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author hongchu
 * @since 2025-11-17
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    @Autowired private IUserService userService;
    @Autowired private SignService SignService;
    /**
     * 注册自签用户信息
     * @param username 用户名
     * @param psd 加密密码
     * @return 登录结果
     */
    @RequestMapping("/login")
    public String register(@RequestParam("username") String username,
                        @RequestParam("cryptPsd") String psd) {
        log.info("controller层-登录用户名：{}，密码：{}", username, psd);
        // 插入用户信息
        return userService.register(username, psd);
    }

    // 设置自动签到
    @RequestMapping("/AutoSign/{username}/{isAuto}")
    public Result<Void> setAutoSign(@RequestParam("username") String username,
                              @RequestParam("isAuto") Boolean isAuto) {
        log.info("controller层-设置自动签到-username: {}, isAuto: {}", username, isAuto);
        userService.update().eq("username", username).set("auto_sign", isAuto);
        return Result.success();
    }



    /**
     * 登出
     * @param username 用户名
     * @return 登出结果
     */
    @RequestMapping("/logout")
    public String logout(@RequestParam("username") String username) {
        log.info("controller层-删除信息-username: {}", username);
        return userService.removeLoginInfo(username);
    }

}
