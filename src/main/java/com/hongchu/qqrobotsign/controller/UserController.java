package com.hongchu.qqrobotsign.controller;


import com.hongchu.qqrobotsign.pojo.DTO.UserDTO;
import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.result.Result;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/login")
    public String register(@RequestParam("username") String username,
                        @RequestParam("psd") String psd) {
        log.info("controller层-登录用户名：{}，密码：{}", username, psd);
        // 插入用户信息
        return userService.register(username, psd);
    }

    /**
     * 是否自动签到
     * @param username 用户名
     * @param isAuto true/false
     * @return 成功
     */
    @PutMapping("/auto-sign/{username}/{isAuto}")
    public Result<Void> setAutoSign(@PathVariable("username") String username,
                                    @PathVariable("isAuto") Boolean isAuto) {
        log.info("接收参数 - username: {}, isAuto: {}, 类型: {}", username, isAuto, isAuto.getClass());
        userService.setAutoSign(username, isAuto);
        return Result.success();
    }

    /**
     * 登出
     * @param username 用户名
     * @return 登出结果
     */
    @PostMapping("/logout/{username}")
    public String logout(@PathVariable("username") String username) {
        log.info("controller层-删除信息-username: {}", username);
        return userService.removeLoginInfo(username);
    }

    /**
     * 修改用户信息
     * @param username 用户
     * @param userDTO 修改信息
     * @return 操作成功
     */
    @PutMapping("/info/{username}")
    public Result<Void> setInfo(@PathVariable("username") String username, UserDTO userDTO){
        log.info("controller层-修改信息-username: {},UserDTO: {}", username,userDTO);
        userService.setInfo(username,userDTO);
        return Result.success();
    }

}
