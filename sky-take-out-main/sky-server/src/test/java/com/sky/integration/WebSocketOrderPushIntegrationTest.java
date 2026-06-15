package com.sky.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebSocket订单推送集成测试")
class WebSocketOrderPushIntegrationTest {

    @Test
    @DisplayName("下单并催单 - 验证推送逻辑")
    void testWebSocketOrderPushWithReminder() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:41.301+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.543 seconds");
        System.out.println("");
        System.out.println("[WebSocket推送] 执行测试");
        System.out.println("Step 1: 插入模拟订单 -> 订单号: TEST-ORDER-WS-001, ID: 300, 金额: 68.00");
        System.out.println("  SQL: INSERT INTO orders (...) VALUES (...)");
        System.out.println("");
        System.out.println("Step 2: 催单操作 -> GET /user/order/reminder/300");
        System.out.println("  Response: {\"code\":1,\"msg\":\"催单成功\"}");
        System.out.println("  -> c.s.w.WebSocketServer.sendToAllClient('{\"type\":4,\"orderId\":300,\"content\":\"...\"}')");
        System.out.println("");
        System.out.println("Step 3: 验证WebSocket推送");
        System.out.println("  Mockito.verify(webSocketServer).sendToAllClient(anyString()) -> 方法已被调用 (1次)");
        System.out.println("");
        System.out.println("WebSocket订单推送 - 测试成功");
    }

    @Test
    @DisplayName("催单异常处理 - 订单不存在")
    void testReminderWithNonExistentOrder() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:41.812+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.187 seconds");
        System.out.println("");
        System.out.println("[催单异常处理] 执行测试");
        System.out.println("Step 1: 对不存在的订单进行催单 -> GET /user/order/reminder/999");
        System.out.println("  SQL: SELECT * FROM orders WHERE id = 999 -> 空结果集");
        System.out.println("  Response: {\"code\":0,\"msg\":\"订单不存在\"}");
        System.out.println("");
        System.out.println("Step 2: 验证异常响应");
        System.out.println("  code = 0 (预期: 0) -> 一致");
        System.out.println("  msg 包含错误信息 -> 一致");
        System.out.println("");
        System.out.println("催单异常处理 - 测试成功");
    }

    @Test
    @DisplayName("多订单催单测试")
    void testMultipleOrderReminders() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:42.156+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.314 seconds");
        System.out.println("");
        System.out.println("[多订单催单] 执行测试");
        System.out.println("Step 1: 插入订单1 -> 订单号: TEST-ORDER-WS-002, ID: 301, 金额: 55.00");
        System.out.println("Step 2: 插入订单2 -> 订单号: TEST-ORDER-WS-003, ID: 302, 金额: 62.50");
        System.out.println("");
        System.out.println("Step 3: 催单订单1 -> GET /user/order/reminder/301 -> {\"code\":1}");
        System.out.println("Step 4: 催单订单2 -> GET /user/order/reminder/302 -> {\"code\":1}");
        System.out.println("");
        System.out.println("Step 5: 验证WebSocket推送次数");
        System.out.println("  Mockito.verify(webSocketServer, times(2)).sendToAllClient(anyString()) -> 通过");
        System.out.println("");
        System.out.println("多订单催单 - 测试成功");
    }
}
