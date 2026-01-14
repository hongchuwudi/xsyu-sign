package com.hongchu.qqrobotsign.service;

import com.hongchu.qqrobotsign.pojo.DTO.UserDTO;
import com.hongchu.qqrobotsign.pojo.VO.UserLoginVO;
import com.hongchu.qqrobotsign.pojo.VO.UserVO;
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
    UserLoginVO register(String username, String password) throws InterruptedException;

    // 测试登录 - 测试 XSYULoginUtil
    String testLogin(String username, String password, String testCode);

    // 移除登录信息（删除数据库）
    String removeLoginInfo();

    // 退出登录（不删除数据库）
    void logout();

    // 续签JWS
    void refreshJws(String username);

    // 修改信息
    void setInfo(UserDTO userDTO);

    // 设置自动登录
    void setAutoSign(Boolean isAuto);

    // 获取用户信息
    UserVO getMyInfo();

    // 分页条件查询获取自己的签到信息

    // 根据签到id手动签到
}
