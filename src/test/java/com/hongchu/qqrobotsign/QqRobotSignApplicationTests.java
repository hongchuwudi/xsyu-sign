package com.hongchu.qqrobotsign;

import com.hongchu.qqrobotsign.config.SignInfoConfig;
import com.hongchu.qqrobotsign.config.TestUserConfig;
import com.hongchu.qqrobotsign.config.UrlConfig;
import com.hongchu.qqrobotsign.pojo.DTO.SignDTO;
import com.hongchu.qqrobotsign.service.IUserService;
import com.hongchu.qqrobotsign.webClient.BaseSignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QqRobotSignApplicationTests {
    @Autowired private IUserService userService;
    @Autowired private BaseSignService baseSignService;
    @Autowired private SignInfoConfig signInfoConfig;
    @Autowired private TestUserConfig testUserConfig;
    @Autowired private UrlConfig urlConfig;

    @Test
    void contextLoads() {
        // 使用配置的测试用户
        String username = testUserConfig.getUsername();

        // 获取所有签到
        String allSign = String.valueOf(baseSignService.getAllSign(username, 1, 2));
        System.out.println("所有签到: " + allSign);

        // 使用配置的签到参数
        SignDTO signDTO = SignDTO.builder()
                .inArea(signInfoConfig.getInArea())
                .areaJSON(signInfoConfig.getAreaJson())
                .latitude(signInfoConfig.getLatitude())
                .longitude(signInfoConfig.getLongitude())
                .build();

        // 执行签到
        String sign = String.valueOf(baseSignService.sign(username,
                "9105976684918908xx", "9105976679003586xx", "17", signDTO));
        System.out.println("签到结果: " + sign);
    }

    @Test
    void testConfigClasses() {
        // 单独测试配置类是否正确加载
        System.out.println("=== 配置类测试 ===");

        // 测试 URL 配置
        System.out.println("URL配置:");
        System.out.println("  基础URL: " + urlConfig.getBaseUrl());
        System.out.println("  接口前缀: " + urlConfig.getUriPri());
        System.out.println("  完整获取URL: " + urlConfig.getFullGetAllSignUrl());
        System.out.println("  完整详情URL: " + urlConfig.getFullGetOneSignUrl());
        System.out.println("  完整签到URL: " + urlConfig.getFullSignUrl());

        // 测试签到信息配置
        System.out.println("签到信息配置:");
        System.out.println("  是否在区域内: " + signInfoConfig.getInArea());
        System.out.println("  区域JSON: " + signInfoConfig.getAreaJson());
        System.out.println("  纬度: " + signInfoConfig.getLatitude());
        System.out.println("  经度: " + signInfoConfig.getLongitude());

        // 测试用户配置
        System.out.println("测试用户配置:");
        System.out.println("  用户名: " + testUserConfig.getUsername());
        System.out.println("  密码: " + testUserConfig.getPassword());

        System.out.println("=== 配置类测试完成 ===");
    }
}