package com.hongchu.qqrobotsign.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * RSA加密后的密码
     */
    private String psd;
}
