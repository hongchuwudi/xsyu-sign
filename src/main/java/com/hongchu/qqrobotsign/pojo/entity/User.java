package com.hongchu.qqrobotsign.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.time.LocalTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author hongchu
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密密码
     */
    private byte[] password;

    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * JWT令牌
     */
    private String jws;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 自动签到时间配置
    private Integer signRandomDelay;      // 随机延迟时间(秒)
    private LocalDateTime signScheduledTime; // 计划签到时间

    /**
     * 自动签到
     */
    private Boolean autoSign;

}
