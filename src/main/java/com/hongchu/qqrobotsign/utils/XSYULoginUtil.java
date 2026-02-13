package com.hongchu.qqrobotsign.utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XSYULoginUtil {

    private static final String SERVICE_CAS_LOGIN = "https://gwxg.xsyu.edu.cn/basicinfo/mobile/login/casLogin";
    private static final String CAS_LOGIN_URL = "https://ids.xsyu.edu.cn/authserver/login";
    private static final String CAS_HOST = "ids.xsyu.edu.cn";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";

    /** Apply a full set of browser-like headers to minimize server-side captcha triggers. */
    private static void applyBrowserHeaders(HttpURLConnection conn, String referer) {
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        conn.setRequestProperty("Accept-Encoding", "identity"); // let us handle it manually
        conn.setRequestProperty("Connection", "keep-alive");
        if (referer != null && !referer.isEmpty()) {
            conn.setRequestProperty("Referer", referer);
        }
    }

    /**
     * 执行西安石油大学统一认证登录，获取JWSESSION。
     * CookieManager 管理 domain/path 匹配的 cookie，手动跟随重定向以检测循环。
     * 失败时重新走完整 CAS 流程（全新 CookieManager），最多3次。
     */
    public static String login(String username, String password) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            CookieManager cm = new CookieManager();
            CookieHandler old = CookieHandler.getDefault();
            CookieHandler.setDefault(cm);

            try {
                if (attempt > 1) {
                    long delay = 3000 + (long)(Math.random() * 2000);
                    System.out.println("=== 第 " + attempt + " 次重新尝试登录 (等待 " + delay + "ms) ===");
                    Thread.sleep(delay);
                }

                String result = doLogin(username, password);
                if (result != null) {
                    System.out.println("登录成功! 尝试次数: " + attempt);
                    return result;
                }
                System.out.println("第 " + attempt + " 次尝试失败");

            } catch (Exception e) {
                System.out.println("第 " + attempt + " 次尝试异常: " + e.getMessage());
            } finally {
                CookieHandler.setDefault(old);
            }
        }
        return null;
    }

    private static String doLogin(String username, String password) throws IOException, InterruptedException {
        // 第一步：访问服务端，获取CAS重定向URL
        String casLoginUrl = getCasLoginUrl();
        if (casLoginUrl == null) {
            System.out.println("无法获取CAS登录URL");
            return null;
        }
        System.out.println("CAS登录URL: " + casLoginUrl);

        // 第二步：从CAS登录页提取execution
        String execution = getExecutionFromCas(casLoginUrl);
        if (execution == null) {
            System.out.println("无法从CAS获取execution参数");
            return null;
        }
        System.out.println("获取到execution: " + execution);

        // 短暂延迟，模拟浏览器行为，降低被风控识别概率
        Thread.sleep(800 + (long)(Math.random() * 400));

        // 第三步：提交CAS登录表单，获取ticket + CASTGC
        String ticket = submitCasLogin(username, password, execution);
        if (ticket == null) {
            System.out.println("CAS登录失败");
            return null;
        }
        System.out.println("获取到ticket: " + ticket);

        // 第四步：用ticket获取JWSESSION（手动跟随重定向链）
        return getJWSessionWithTicket(ticket);
    }

    // ==================== 第一步 ====================
    private static String getCasLoginUrl() throws IOException {
        HttpURLConnection conn = open(SERVICE_CAS_LOGIN, false);
        int code = conn.getResponseCode();
        System.out.println("初始访问响应码: " + code);
        if (code == 302) {
            String location = conn.getHeaderField("Location");
            System.out.println("重定向到: " + location);
            return location;
        }
        return null;
    }

    // ==================== 第二步 ====================
    private static String getExecutionFromCas(String casUrl) throws IOException {
        HttpURLConnection conn = open(casUrl, false);
        String html = readBody(conn);
        return extractExecution(html);
    }

    // ==================== 第三步 ====================
    private static String submitCasLogin(String username, String password, String execution) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(CAS_LOGIN_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        // Set browser-like headers to avoid server-side captcha trigger
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Origin", "https://" + CAS_HOST);
        conn.setRequestProperty("Referer", "https://" + CAS_HOST + "/authserver/login?service=" + URLEncoder.encode(SERVICE_CAS_LOGIN, StandardCharsets.UTF_8));
        conn.setRequestProperty("Sec-Ch-Ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"");
        conn.setRequestProperty("Sec-Ch-Ua-Mobile", "?0");
        conn.setRequestProperty("Sec-Ch-Ua-Platform", "\"Windows\"");
        conn.setRequestProperty("Sec-Fetch-Dest", "document");
        conn.setRequestProperty("Sec-Fetch-Mode", "navigate");
        conn.setRequestProperty("Sec-Fetch-Site", "same-origin");
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");

        String body = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
                "&execution=" + URLEncoder.encode(execution, StandardCharsets.UTF_8) +
                "&_eventId=submit" +
                "&loginType=1" +
                "&rememberMe=true" +
                "&service=" + URLEncoder.encode(SERVICE_CAS_LOGIN, StandardCharsets.UTF_8);

        System.out.println("提交CAS登录请求体");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        System.out.println("CAS登录响应码: " + code);

        if (code == 302) {
            String location = conn.getHeaderField("Location");
            System.out.println("CAS登录后重定向到: " + location);
            return extractTicket(location);
        }

        if (code == 200) {
            String html = readBody(conn);
            System.out.println("CAS返回200，响应前500字符: " +
                    html.substring(0, Math.min(500, html.length())));

            if (html.contains("验证码") || html.contains("captcha")) {
                throw new RuntimeException("登录需要验证码，请手动登录");
            }

            String ticket = extractTicket(html);
            if (ticket != null) {
                System.out.println("从响应页面提取到ticket: " + ticket);
                return ticket;
            }
        }

        return null;
    }

    // ==================== 第四步：手动跟随重定向链 ====================
    // 不使用 setInstanceFollowRedirects(true)，因为CAS↔service可能形成死循环
    // 手动跟随可精确检测循环（depth>5 即停止），CookieManager 自动管理cookie
    private static String getJWSessionWithTicket(String ticket) throws IOException {
        String url = SERVICE_CAS_LOGIN + "?ticket=" +
                URLEncoder.encode(ticket, StandardCharsets.UTF_8);
        System.out.println("使用ticket访问URL: " + url);
        return followRedirect(url, 0);
    }

    private static String followRedirect(String url, int depth) throws IOException {
        if (depth > 5) {
            System.out.println("重定向链过长(" + depth + "次)，停止跟随");
            return null;
        }

        System.out.println((depth == 0 ? "访问" : "跟随重定向[" + depth + "]") + ": " + url);

        HttpURLConnection conn = open(url, false);
        int code = conn.getResponseCode();

        // CookieManager 已自动解析 Set-Cookie 并存入 cookie store
        // 检查是否已拿到 JWSESSION
        String jws = findJWSession();
        if (jws != null) {
            System.out.println("成功获取JWSESSION!");
            return jws;
        }

        System.out.println("响应码[" + depth + "]: " + code);

        if (code == 302) {
            String location = conn.getHeaderField("Location");
            if (location != null) return followRedirect(location, depth + 1);
        }

        return null;
    }

    // ==================== 工具方法 ====================

    private static HttpURLConnection open(String url, boolean followRedirects) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(followRedirects);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        applyBrowserHeaders(conn, url.contains("authserver") ? null : "https://" + CAS_HOST + "/");
        return conn;
    }

    private static String readBody(HttpURLConnection conn) throws IOException {
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static String findJWSession() {
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        if (cm == null) return null;
        for (HttpCookie c : cm.getCookieStore().getCookies()) {
            if ("JWSESSION".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty())
                return c.getValue();
        }
        return null;
    }

    private static String extractTicket(String text) {
        if (text == null) return null;
        Matcher m = Pattern.compile("ticket=([^\"&\\s]+)").matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    private static String extractExecution(String html) {
        Matcher m = Pattern.compile("name=\"execution\" value=\"([^\"]+)\"").matcher(html);
        if (m.find()) return m.group(1);
        m = Pattern.compile("execution\" value=\"([^\"]+)\"").matcher(html);
        if (m.find()) return m.group(1);
        return null;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入学号：");
        String username = scanner.nextLine();
        System.out.print("请输入密码：");
        String password = scanner.nextLine();
        String jwsession = login(username, password);
        if (jwsession != null) {
            System.out.println("登录成功！");
            System.out.println("JWSESSION: " + jwsession);
        } else {
            System.out.println("登录失败！");
        }
    }
}