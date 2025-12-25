package com.hongchu.qqrobotsign.controller;

import com.hongchu.qqrobotsign.context.BaseContext;
import com.hongchu.qqrobotsign.exception.BusinessException;
import com.hongchu.qqrobotsign.pojo.DTO.UserDTO;
import com.hongchu.qqrobotsign.pojo.VO.UserLoginVO;
import com.hongchu.qqrobotsign.pojo.VO.UserVO;
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
    public Result<UserLoginVO> register(@RequestParam("username") String username,
                           @RequestParam("psd") String psd) throws InterruptedException {
        log.info("controller层-登录用户名：{}，密码：{}", username, psd);
        // 插入用户信息
        UserLoginVO sucVO = userService.register(username, psd);
        if(sucVO == null) throw new BusinessException("登录失败请重试");
        return  Result.success(sucVO);
    }

    /**
     * 是否自动签到
     * @param isAuto true/false
     * @return 成功
     */
    @PutMapping("/auto-sign/{isAuto}")
    public Result<Void> setAutoSign(@PathVariable("isAuto") Boolean isAuto) {
        log.info("接收参数 - userId: {}, isAuto: {}, 类型: {}",
                BaseContext.getCurrentId(), isAuto, isAuto.getClass());
        userService.setAutoSign(isAuto);
        return Result.success();
    }

    /**
     * 登出
     * @return 登出结果
     */
    @PostMapping("/logout")
    public String logout() {
        log.info("controller层-删除信息-userId: {}", BaseContext.getCurrentId());
        return userService.removeLoginInfo();
    }

    /**
     * 修改用户信息
     * @param userDTO 修改信息
     * @return 操作成功
     */
    @PutMapping("/info/")
    public Result<Void> setInfo(UserDTO userDTO){
        log.info("controller层-修改信息-userId: {},UserDTO: {}", BaseContext.getCurrentId(),userDTO);
        userService.setInfo(userDTO);
        return Result.success();
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getInfo(){
        log.info("controller层-获取信息-userId: {}", BaseContext.getCurrentId());
        UserVO myInfo = userService.getMyInfo();
        if(myInfo == null) throw new BusinessException("获取信息失败");
        return Result.success(myInfo);
    }
}