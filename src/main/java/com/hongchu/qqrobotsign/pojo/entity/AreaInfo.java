package com.hongchu.qqrobotsign.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区域信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AreaInfo {
    private String id;           // 区域ID
    private String name;         // 区域名称
    private String latitude;     // 纬度
    private String longitude;    // 经度
    private Integer radius;      // 半径 (米)
    private Integer shape;       // 形状类型
    private String dataStr;      // 数据字符串
}