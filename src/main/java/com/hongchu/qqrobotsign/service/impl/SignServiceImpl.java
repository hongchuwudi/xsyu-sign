package com.hongchu.qqrobotsign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongchu.qqrobotsign.config.SignInfoConfig;
import com.hongchu.qqrobotsign.exception.BusinessException;
import com.hongchu.qqrobotsign.pojo.DTO.SignDTO;
import com.hongchu.qqrobotsign.pojo.entity.SignItem;
import com.hongchu.qqrobotsign.pojo.entity.User;
import com.hongchu.qqrobotsign.result.Result;
import com.hongchu.qqrobotsign.service.SignService;
import com.hongchu.qqrobotsign.webClient.BaseSignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SignServiceImpl implements SignService {
    @Autowired private BaseSignService baseSignService;
    @Autowired private UserServiceImpl userService;
    @Autowired private SignInfoConfig signInfoConfig;
    /**
     * 一键签到所有未签到的签到
     * @param username 用户名
     * @return 签到结果
     */
    public String signAll(String username) {
        log.info("一键签到-username: {}", username);

        // 1. 检查用户是否存在
        User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) throw new BusinessException("用户不存在,请先登录");


        // 2. 获取最近10条签到
        Result<List<SignItem>> signResult = baseSignService.getAllSign(username, 1, 10);
        if (signResult.getCode() != 200 || signResult.getData() == null) {
            return "获取签到列表失败";
        }

        List<SignItem> signList = signResult.getData();

        // 3. 检查是否有未签到的
        List<SignItem> unsignedItems = signList.stream()
                .filter(item -> item.getSignStatus() != null && item.getSignStatus() == 1)
                .toList();

        if (unsignedItems.isEmpty()) return "最近10条签到已全部完成，无需签到";

        // 4. 执行签到
        List<String> signResults = new ArrayList<>();
        for (SignItem item : unsignedItems) {
            SignDTO signBuild = SignDTO.builder().inArea(1)
                    .latitude(item.getLatitude())
                    .longitude(item.getLongitude())
                    .areaJSON(signInfoConfig.getAreaJson())
                    .build();
            // 执行签到-默认鄠邑
            String result = processSingleSign(username, item, signBuild);
            signResults.add(result);

            // 短暂延迟
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 5. 返回结果
        return String.format("一键签到完成！共处理%d个签到：\n%s",
                unsignedItems.size(), String.join("\n", signResults));
    }

    /**
     * 处理单个签到
     * @param username 用户名
     * @param signItem 签到项
     * @param signDTO 签到数据(不传采用默认配置)
     * @return 处理结果
     */
    @Override
    public String processSingleSign(String username, SignItem signItem, SignDTO signDTO) {
        try {
            // 执行签到
            Result<String> signResult = baseSignService.sign(
                    username,
                    signItem.getId(),
                    signItem.getSignId(),
                    signItem.getSchoolId(),
                    signDTO
            );

            // 检查签到结果
            // 未登录-刷新JWS-二次签到
            if (Objects.equals(signResult.getMessage(), "未登录,请重新登录")) {
                userService.refreshJws(username);
                signResult = baseSignService.sign(
                        username,
                        signItem.getId(),
                        signItem.getSignId(),
                        signItem.getSchoolId(),
                        signDTO
                );
            }

            // 返回结果
            if (signResult.getCode() == 200)
                return String.format("✅ %s - %s", signItem.getSignTitle(), signResult.getData());
            else
                return String.format("❌ %s - %s", signItem.getSignTitle(), signResult.getMessage());
        } catch (Exception e) {
            log.error("处理签到 {} 失败: {}", signItem.getSignTitle(), e.getMessage());
            return String.format("⚠️ %s - 异常: %s", signItem.getSignTitle(), e.getMessage());
        }
    }
}
