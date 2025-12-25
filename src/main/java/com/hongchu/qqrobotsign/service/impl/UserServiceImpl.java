package com.hongchu.qqrobotsign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongchu.qqrobotsign.context.BaseContext;
import com.hongchu.qqrobotsign.exception.BusinessException;
import com.hongchu.qqrobotsign.mapper.UserMapper;
import com.hongchu.qqrobotsign.pojo.DTO.UserDTO;
import com.hongchu.qqrobotsign.pojo.VO.UserLoginVO;
import com.hongchu.qqrobotsign.pojo.VO.UserVO;
import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.properties.JwtProperties;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.utils.CryptoUtils;
import com.hongchu.qqrobotsign.utils.JwtUtil;
import com.hongchu.qqrobotsign.utils.SimpleCryptoUtils;
import com.hongchu.qqrobotsign.utils.XSYULoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    @Autowired JwtProperties jwtProperties;

    // 登录
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO register(String username, String psd) throws InterruptedException {
        // 解密
        String password = SimpleCryptoUtils.decrypt(psd);
        log.info("用户: {},密码长度: {}", username, password.length());

        // 1.检查是否有该用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));

        // 2.如果有就更新jws并返回用户
        if (user != null) {
            // 获取新的jws
            String jws = null;
            for (int i = 0; i < 5 && jws == null; i++) {
                Thread.sleep(10);
                jws = XSYULoginUtil.login(username, password);
            }
            if (jws == null) throw new BusinessException("再试一试");

            // 更新jws
            user.setJws(jws);
            log.info("用户: {} 更新JWS成功---JWS:{}", username, jws);

            // 更新数据库
            this.updateById(user);
        } else {
            // 3. 如果没有用户，创建新用户
            String jws = null;
            for (int i = 0; i < 5 && jws == null; i++) {
                Thread.sleep(10);
                jws = XSYULoginUtil.login(username, password);
            }
            if (jws == null) throw new BusinessException("再试一试");

            // 4. 创建用户
            user = new User();
            user.setUsername(username);
            byte[] passwordBytes = CryptoUtils.encrypt(password).getBytes();
            user.setPassword(passwordBytes);
            user.setJws(jws);
            log.info("用户: {} 注册成功---JWS:{}", username, jws);

            // 5. 保存用户
            this.save(user);
        }

        // 6. 生成jwt（新用户和已有用户都需要）
        String secretKey = jwtProperties.getSecretKey();
        long ttl = jwtProperties.getTtl();
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("userId", user.getId());

        String jwt = JwtUtil.createJWT(secretKey, ttl, map);

        // 封装信息返回
        return UserLoginVO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .jwt(jwt)
                .autoSign(user.getAutoSign())
                .build();
    }

    // 删除登录信息
    @Override
    public String removeLoginInfo() {
        log.info("删除用户登录信息-userId: {}", BaseContext.getCurrentId());
        long userId = BaseContext.getCurrentId();
        // 1. 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        // 2. 删除用户记录
        boolean removed = this.remove(new QueryWrapper<User>().eq("username", user.getUsername()));

        if (removed) {
            log.info("用户 {} 信息删除成功", user.getUsername());
            return "用户信息删除成功";
        } else {
            log.error("用户 {} 信息删除失败", user.getUsername());
            throw new BusinessException("用户信息删除失败");
        }
    }

    // 刷新JWS
    @Override
    public void refreshJws(String username) {
        // 1.检查是否有该用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));
        // 2.如果有就全局返回错误
        if (user == null) throw new BusinessException("用户不存在,无法续签JWS");
        // 3. 获取JWS
        String jws = XSYULoginUtil.login(username, CryptoUtils.decrypt(new String(user.getPassword())));
        // 4. 更新JWS
        update().set("jws", jws).eq("username", username);
        log.info("用户: {} 续签成功---JWS:{}", username, jws);
    }


    // 修改信息
    @Override
    public void setInfo(UserDTO userDTO) {
        long userId = BaseContext.getCurrentId();
        // 参数校验
        User user = this.getById(userId);
        if (user == null) throw new BusinessException("没有改用户");

        // 构建更新条件
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUsername, user.getUsername());

        // 构建更新实体
        User updateUser = new User();

        // 只更新非空字段
        if (StringUtils.isNotBlank(userDTO.getName()))
            updateUser.setName(userDTO.getName());
        if (StringUtils.isNotBlank(userDTO.getEmail())) {
            // 邮箱格式校验
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!userDTO.getEmail().matches(emailRegex))
                throw new BusinessException("邮箱格式不正确");
            updateUser.setEmail(userDTO.getEmail());
        }

        // 执行更新
        boolean updated = update(updateUser, updateWrapper);
        if (!updated) throw new BusinessException("用户信息更新失败，用户不存在或数据未变化");
    }

    // 设置自动签到
    @Override
    public void setAutoSign(Boolean isAuto) {
        // 拿到用户id
        Long currentId = BaseContext.getCurrentId();
        // 检查用户是否存在
        User user = getById(currentId);
        if (user == null) throw new BusinessException("用户不存在");

        // 执行更新
        lambdaUpdate().eq(User::getUsername, user.getUsername())
                .set(User::getAutoSign, isAuto).update();
    }

    @Override
    public UserVO getMyInfo() {
        Long currentId = BaseContext.getCurrentId();
        User byId = getById(currentId);
        if(byId == null) throw new BusinessException("用户不存在");
        return UserVO.builder()
                .id(byId.getId())
                .username(byId.getUsername())
                .name(byId.getName())
                .email(byId.getEmail())
                .autoSign(byId.getAutoSign())
                .build();
    }


}
