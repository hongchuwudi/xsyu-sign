package com.hongchu.qqrobotsign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongchu.qqrobotsign.exception.BusinessException;
import com.hongchu.qqrobotsign.pojo.DTO.SignDTO;
import com.hongchu.qqrobotsign.pojo.entity.SignItem;
import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.mapper.UserMapper;
import com.hongchu.qqrobotsign.result.Result;
import com.hongchu.qqrobotsign.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongchu.qqrobotsign.utils.CryptoUtils;
import com.hongchu.qqrobotsign.utils.XSYULoginUtil;
import com.hongchu.qqrobotsign.webClient.BaseSignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author hongchu
 * @since 2025-11-17
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 登录
    @Override
    public String register(String username, String password) {
        // 1.检查是否有该用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));

        // 2.如果有就全局返回错误
        if (user != null) throw new BusinessException("用户已存在,请先删除重新登录");

        // 3. 检查jws
        String jws = XSYULoginUtil.login(username, password);
        if (jws == null) throw new BusinessException("用户名或密码错误");

        // 4. 创建用户
        user = new User();
        // 4.1 设置用户名
        user.setUsername(username);
        // 4.2 设置密码
        byte[] passwordBytes = CryptoUtils.encrypt(password).getBytes();
        user.setPassword(passwordBytes);
        // 4.3 调用jws获取函数
        user.setJws(jws);
        log.info("用户: {} 登录成功---JWS:{}", username, jws);

        // 5. 数据入库-保存用户
        this.save(user);
        return "成功记录登录并获取JWS";
    }

    // 删除登录信息
    @Override
    public String removeLoginInfo(String username) {
        log.info("删除用户登录信息-username: {}", username);

        // 1. 检查用户是否存在
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) throw new BusinessException("用户不存在");

        // 2. 删除用户记录
        boolean removed = this.remove(new QueryWrapper<User>().eq("username", username));

        if (removed) {
            log.info("用户 {} 信息删除成功", username);
            return "用户信息删除成功";
        } else {
            log.error("用户 {} 信息删除失败", username);
            throw new BusinessException("用户信息删除失败");
        }
    }

    @Override
    public String refreshJws(String username) {
        // 1.检查是否有该用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));

        // 2.如果有就全局返回错误
        if (user == null) throw new BusinessException("用户不存在,无法续签JWS");

        // 3. 获取JWS
        String jws = XSYULoginUtil.login(username, CryptoUtils.decrypt(new String(user.getPassword())));

        // 4. 更新JWS
        update().set("jws", jws).eq("username", username);

        log.info("用户: {} 续签成功---JWS:{}", username, jws);
        return "成功续签JWS";
    }
}
