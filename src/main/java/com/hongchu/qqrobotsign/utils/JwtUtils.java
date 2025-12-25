package com.hongchu.qqrobotsign.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT令牌工具类
 */
public class JwtUtils {

    // 签名密钥（和测试类保持一致）
    private static final String SECRET_KEY = "aG9uZ2NodQ==";
    
    // 令牌过期时间（480小时）
    private static final long EXPIRATION_TIME = 480 * 3600 * 1000;

    /**
     * 生成JWT令牌
     * @param claims 自定义声明信息
     * @return 生成的JWT令牌
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // 指定加密算法和秘钥
                .addClaims(claims)                               // 添加自定义声明
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .compact();                                      // 生成令牌
    }

    /**
     * 解析并验证JWT令牌
     * @param token JWT令牌
     * @return 包含声明信息的Claims对象
     * @throws Exception 如果令牌无效或过期会抛出异常
     */
    public static Claims parseToken(String token) throws Exception {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}