package com.hongchu.qqrobotsign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hongchu.qqrobotsign.utils.XSYULoginUtil;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * 西安石油大学一键签到工具（独立运行，零Spring依赖）
 *
 * 使用方法：直接运行 main 方法，输入学号密码，自动完成登录和签到。
 */
public class XSYUOneKeySign {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ---------- 常量配置 ----------
    // User-agent
    private static final String MOBILE_UA = "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36";
    // HTTPClient
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    // 签到接口基础地址
    private static final String BASE_SIGN_URL = "https://gwxg.xsyu.edu.cn/sign/mobile/receive";
    // 签到列表接口
    private static final String GET_SIGN_LIST_URL = BASE_SIGN_URL + "/getMySignLogs?page=1&size=20";
    // 执行签到接口模板
    private static final String DO_SIGN_URL_TEMPLATE = BASE_SIGN_URL + "/doSignByArea?id=%s&signId=%s&schoolId=%s";

    // 签到位置信息（鄠邑校区）
    private static final String IN_AREA = "1";
    private static final String AREA_JSON = "{\\\"id\\\":\\\"170002\\\",\\\"name\\\":\\\"鄠邑校区\\\"}";
    private static final String LATITUDE = "34.098273";
    private static final String LONGITUDE = "108.656693";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("--> 请输入学号：");
        String username = scanner.nextLine().trim();
        System.out.print("--> 请输入密码：");
        String password = scanner.nextLine().trim();

        try {
            boolean success = oneKeySign(username, password);
            System.out.println(success ? "\n--> 一键签到全部完成！" : "\n--> 签到过程中出现问题，请查看上方日志。");
        } catch (Exception e) {
            System.err.println("--> 发生异常：");
            e.printStackTrace();
        }
    }

    /**
     * 一键签到主流程
     * @return true 表示所有可签到项均成功，false 表示有失败或异常
     */
    public static boolean oneKeySign(String username, String password) throws Exception {
        // 1. 登录获取 JWSESSION
        System.out.println("\n--> 正在登录统一认证系统...");
        String jwsession = XSYULoginUtil.login(username, password);
        if (jwsession == null || jwsession.isEmpty()) {
            System.err.println("--> 登录失败，无法获取 JWSESSION");
            return false;
        }
        System.out.println("--> 登录成功，JWSESSION: " + jwsession);

        // 2. 获取签到列表
        System.out.println("\n--> 正在获取签到列表...");
        List<SignItemCore> signItems = fetchSignList(jwsession);
        if (signItems.isEmpty()) {
            System.out.println("--> 没有需要签到的项目（可能均已签到或不在签到时间内）");
            return true;
        }
        System.out.println("--> 共获取到 " + signItems.size() + " 个待签到项：");
        for (SignItemCore item : signItems) {
            System.out.printf("    - %s (id=%s, signId=%s, schoolId=%s)\n",
                    item.signName, item.id, item.signId, item.schoolId);
        }

        // 3. 逐个执行签到
        System.out.println("\n--> 开始执行签到...");
        int successCount = 0;
        int total = signItems.size();
        for (SignItemCore item : signItems) {
            System.out.printf("\n--> 正在签到：%s\n", item.signName);
            boolean ok = executeSign(jwsession, item);
            if (ok) {
                successCount++;
                System.out.println("    [成功]");
            } else {
                System.out.println("    [失败]");
            }
        }

        System.out.printf("\n--> 签到结束：成功 %d / %d\n", successCount, total);
        return successCount == total;
    }

    /**
     * 调用签到列表接口，过滤出待签到且有效的项
     */
    private static List<SignItemCore> fetchSignList(String jwsession) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GET_SIGN_LIST_URL))
                .header("User-Agent", MOBILE_UA)
                .header("Accept", "application/json, text/plain, */*")
                .header("JWSESSION", jwsession)
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("--> 获取签到列表失败，HTTP状态码: " + response.statusCode());
            return Collections.emptyList();
        }

        JsonNode root = MAPPER.readTree(response.body());
        int code = root.path("code").asInt();
        String message = root.path("message").asText();
        if (code != 0 && code != 200) {
            System.err.println("--> 接口返回错误：code=" + code + ", message=" + message);
            return Collections.emptyList();
        }

        JsonNode dataNode = root.path("data");
        if (!dataNode.isArray()) {
            System.err.println("--> data 字段不是数组或不存在");
            return Collections.emptyList();
        }

        List<SignItemCore> validItems = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (JsonNode itemNode : dataNode) {
            String id = itemNode.path("id").asText();
            String signId = itemNode.path("signId").asText();
            String schoolId = itemNode.path("schoolId").asText();
            String signName = itemNode.path("signTitle").asText();
            int signStatus = itemNode.path("signStatus").asInt();
            long start = itemNode.path("start").asLong();
            long end = itemNode.path("end").asLong();

            boolean isUnsigned = (signStatus == 1);
            boolean inTime = (now >= start && now <= end);

            if (isUnsigned && inTime) {
                SignItemCore core = new SignItemCore();
                core.id = id;
                core.signId = signId;
                core.schoolId = schoolId;
                core.signName = signName;
                validItems.add(core);
            } else {
                System.out.printf("    [跳过] %s（状态=%d, 有效期内=%b）\n", signName, signStatus, inTime);
            }
        }
        return validItems;
    }

    /**
     * 执行单个签到（POST 请求）
     */
    private static boolean executeSign(String jwsession, SignItemCore item) throws IOException, InterruptedException {
        String url = String.format(DO_SIGN_URL_TEMPLATE, item.id, item.signId, item.schoolId);

        String requestBody = String.format(
                "{\"inArea\":%s,\"areaJSON\":\"%s\",\"latitude\":%s,\"longitude\":%s}",
                IN_AREA, AREA_JSON, LATITUDE, LONGITUDE
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", MOBILE_UA)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("JWSESSION", jwsession)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("--> 签到请求失败，HTTP状态码: " + response.statusCode());
            return false;
        }

        JsonNode root = MAPPER.readTree(response.body());
        int code = root.path("code").asInt();
        String message = root.path("message").asText();

        boolean success = (code == 0 || code == 200);
        if (!success) {
            System.err.println("--> 签到接口返回错误：code=" + code + ", message=" + message);
        }
        return success;
    }

    // ---------- 内部数据类 ----------
    static class SignItemCore {
        String id;
        String signId;
        String schoolId;
        String signName;
    }
}
