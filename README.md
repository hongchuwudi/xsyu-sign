# 🚀 QQ Robot Sign - 自动化签到机器人

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)

## 📖 项目简介

**QQ Robot Sign** 是一个基于 Spring Boot 3.5.7 和 Java 17 开发的自动化签到系统，专门为西安石油大学的学生设计。系统通过模拟移动端请求，实现自动化的位置签到功能，并支持定时任务执行,本项目还在持续更新中,计划发展为一款QQ机器人,加入班级群和AI元素丰富聊天化。

### ✨ 核心特性

- 🕒 **智能定时签到** - 每天18:30-20:30随机时间自动执行
- 📍 **位置模拟签到** - 支持校区位置自动识别
- 🔐 **安全加密存储** - AES-256-GCM强加密用户凭证
- 🔄 **多用户支持** - 支持多个用户同时管理
- 📊 **签到状态监控** - 实时查看签到结果和历史记录

好的！我来加上技术栈的图标，让这部分更美观：

## 🛠 技术栈

**后端框架**
- <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white" height="20"> Java 17
- <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=springboot&logoColor=white" height="20"> Spring Boot 3.5.7

**数据存储**
- <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white" height="20"> MySQL 8.0+
- <img src="https://img.shields.io/badge/MyBatis%20Plus-3.5.10.1-000000?style=flat-square&logo=apache&logoColor=white" height="20"> MyBatis Plus 3.5.10.1

**网络通信**
- <img src="https://img.shields.io/badge/WebClient-Reactive-4A154B?style=flat-square&logo=spring&logoColor=white" height="20"> WebClient
- <img src="https://img.shields.io/badge/Spring%20Task-Scheduling-6DB33F?style=flat-square&logo=spring&logoColor=white" height="20"> Spring Task

**构建工具**
- <img src="https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat-square&logo=apache-maven&logoColor=white" height="20"> Maven 3.6+

## ⚙️ 配置文件说明

由于安全考虑，配置文件已添加到 `.gitignore`，请按以下说明创建配置文件：

### 2. 开发环境配置 `application-dev.yml`

```yaml
hc:
  datasource:
    host: your-mysql-host
    port: your-mysql-port
    username: your-username
    password: your-password
    db: your-database-name
```

### 3. 签到信息配置 `application-signInfo.yml`

```yaml
hc:
  sign-infos:
    in-area: 1
    area-json: '{"id":"170002","name":"鄠邑校区"}'
    latitude: 34.098273   # 纬度
    longitude: 108.656693 # 经度
  urls:
    base-url: "https://gwxg.xsyu.edu.cn"
    uri-pri: "/sign/mobile/receive"
    uri-get-all-sign: "/getMySignLogs"
    uri-get-one-sign: "/getSignLog"
    uri-sign: "/doSignByArea"
  test-user:
    username: "your-student-id"
    password: "your-password"
```

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化

```sql
CREATE DATABASE xsyu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create table user
(
    id                  bigint unsigned auto_increment comment '用户ID'
        primary key,
    name                varchar(255)                        null comment '姓名',
    email               varchar(255)                        null comment '邮箱(用于发送通知)',
    username            varchar(50)                         not null comment '用户名',
    password            varbinary(255)                      not null comment '加密密码',
    jws                 text                                null comment 'JWS',
    sign_time_begin     time      default '18:30:00'        null comment '每天签到起始时间',
    sign_time_end       time      default '20:00:00'        null comment '每天签到结束时间',
    sign_random_delay   int                                 null comment '随机延迟时间(秒)',
    sign_scheduled_time datetime                            null comment '计划签到时间',
    created_at          timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at          timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint username
        unique (username)
)
    comment '用户表';

create index idx_username
    on user (username);
```

### 3. 配置步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/qq-robot-sign.git
   cd qq-robot-sign
   ```

2. **创建配置文件**
   ```bash
   # 在 src/main/resources/ 目录下创建：
   # application-dev.yml
   # application-signInfo.yml
   ```

3. **修改配置参数**
    - 在 `application-dev.yml` 中配置你的数据库连接
    - 在 `application-signInfo.yml` 中配置你的学号和密码

4. **运行项目**
   ```bash
   mvn spring-boot:run
   ```

## 📋 API 接口

### 用户管理
- `POST /user/login` - 用户登录并获取JWSESSION
- `DELETE /user/{username}` - 删除用户登录信息

### 签到功能
- `GET /sign/list` - 获取签到列表
- `GET /sign/detail` - 获取签到详情
- `POST /sign/do` - 执行签到
- `POST /sign/auto` - 一键签到所有未完成项

## ⏰ 定时任务

系统内置智能定时任务，在指定时间范围内随机执行签到：

```java
@Scheduled(cron = "0 */1 18-20 * * ?")
public void autoSignTask() {
    // 每天18:00-20:59每分钟检查一次
    // 为每个用户设置1-3分钟随机延迟
    // 避免所有用户同时请求
}
```

## 🔒 安全特性

### 加密方案
- **AES-256-GCM** 加密用户密码
- **PBKDF2** 密钥派生算法
- **随机盐值** 每次加密不同
- **GCM认证** 防篡改保护

```java
// 加密示例
String encrypted = StrongCryptoUtils.encrypt("userPassword");
// 结果格式: 盐值:IV:密文 (自包含，无需额外存储)
```

## 🗂 项目结构

```
src/
├── main/
│   ├── java/com/hongchu/qqrobotsign/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── service/         # 业务逻辑
│   │   ├── webClient/       # HTTP客户端
│   │   ├── utils/           # 工具类
│   │   └── entity/          # 实体类
│   └── resources/
│       ├── application.yml           # 主配置
│       ├── application-dev.yml       # 开发配置 (需手动创建)
│       └── application-signInfo.yml  # 签到配置 (需手动创建)
└── test/                    # 测试代码
```

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request


## ⚠️ 免责声明

本项目仅用于学习和研究目的，请遵守学校相关规定，合理使用自动化工具。开发者不对因使用本项目而产生的任何问题负责。

---

**⭐ 如果这个项目对你有帮助，请给个 Star！**
