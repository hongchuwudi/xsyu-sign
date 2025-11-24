package com.hongchu.qqrobotsign.service;

import com.hongchu.qqrobotsign.pojo.DTO.UserDTO;
import com.hongchu.qqrobotsign.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author hongchu
 * @since 2025-11-17
 */
public interface IUserService extends IService<User> {

    // 登录(准确来说应该叫注册)
    String register(String username, String password);

    // 移除登录信息
    String removeLoginInfo(String username);

    // 续签JWS
    void refreshJws(String username);

    // 修改信息
    void setInfo(String username, UserDTO userDTO);

    // 设置自动登录
    void setAutoSign(String username, Boolean isAuto);
}
