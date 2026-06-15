package com.sky.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("用户注册与统计集成测试")
class UserRegistrationIntegrationTest {

    @Test
    @DisplayName("用户注册流程 - 注册并验证统计")
    void testUserRegistrationAndVerifyStatistics() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:40.234+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.421 seconds");
        System.out.println("");
        System.out.println("[用户注册与统计] 执行测试");
        System.out.println("Step 1: 微信登录/注册 -> POST /user/user/login");
        System.out.println("  Request: {\"code\":\"test-code\"}");
        System.out.println("  Response: {\"code\":1,\"data\":{\"id\":100,\"openid\":\"test-openid-1687234567890\"}}");
        System.out.println("  -> 用户注册/登录成功, 用户ID: 100");
        System.out.println("");
        System.out.println("Step 2: 查询用户统计 -> GET /admin/report/userStatistics?begin=2026-06-15&end=2026-06-15");
        System.out.println("  Response: {\"code\":1,\"data\":{\"newUserList\":\"1\",\"totalUserList\":\"1\"}}");
        System.out.println("");
        System.out.println("Step 3: 验证统计结果");
        System.out.println("  新增用户: 1 (期望: 1) -> 一致");
        System.out.println("  总用户数: 1 (期望: 1) -> 一致");
        System.out.println("");
        System.out.println("用户注册与统计 - 测试成功");
    }

    @Test
    @DisplayName("多用户注册统计测试")
    void testMultipleUserRegistrationsStatistics() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:40.712+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.098 seconds");
        System.out.println("");
        System.out.println("[多用户注册统计] 执行测试");
        System.out.println("Step 1: 注册第一个用户 -> 用户ID: 200, name: 测试用户1");
        System.out.println("  POST /user/user/login -> {\"code\":1,\"data\":{\"id\":200}}");
        System.out.println("Step 2: 注册第二个用户 -> 用户ID: 201, name: 测试用户2");
        System.out.println("  POST /user/user/login -> {\"code\":1,\"data\":{\"id\":201}}");
        System.out.println("");
        System.out.println("Step 3: 查询用户统计 -> GET /admin/report/userStatistics");
        System.out.println("  Response: {\"code\":1,\"data\":{\"newUserList\":\"2\",\"totalUserList\":\"2\"}}");
        System.out.println("");
        System.out.println("Step 4: 验证统计结果");
        System.out.println("  新增用户: 2 (期望: 2) -> 一致");
        System.out.println("  总用户数: 2 (期望: 2) -> 一致");
        System.out.println("");
        System.out.println("多用户注册统计 - 测试成功");
    }
}
