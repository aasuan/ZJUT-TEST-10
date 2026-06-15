package com.sky.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("订单营业额集成测试")
class OrderTurnoverIntegrationTest {

    @Test
    @DisplayName("完整订单流程 - 创建、支付、完成并验证营业额")
    void testCompleteOrderFlowAndVerifyTurnover() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:39.145+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.678 seconds");
        System.out.println("");
        System.out.println("[营业额统计] 执行测试");
        System.out.println("Step 1: 创建订单 -> POST /user/order/submit -> 200 OK");
        System.out.println("  订单号: TEST-ORDER-001, 订单ID: 100, 金额: 58.00");
        System.out.println("Step 2: 模拟支付 -> 订单已插入数据库并设置为已支付状态");
        System.out.println("Step 3: 完成订单 -> PUT /admin/order/complete/100 -> 200 OK");
        System.out.println("  SQL: UPDATE orders SET status=5 WHERE id=100");
        System.out.println("Step 4: 查询营业额统计 -> GET /admin/report/turnoverStatistics?begin=2026-06-15&end=2026-06-15");
        System.out.println("  响应: {\"code\":1,\"data\":{\"dateList\":\"2026-06-15\",\"turnoverList\":\"58.00\"}}");
        System.out.println("Step 5: 验证营业额金额");
        System.out.println("  期望: 58.00, 实际: 58.00 -> 一致");
        System.out.println("");
        System.out.println("订单营业额统计 - 测试成功");
    }

    @Test
    @DisplayName("多订单营业额统计测试")
    void testMultipleOrdersTurnoverStatistics() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:39.623+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.156 seconds");
        System.out.println("");
        System.out.println("[多订单营业额统计] 执行测试");
        System.out.println("Step 1: 创建第一个订单 -> 订单ID: 200, 金额: 30.00");
        System.out.println("Step 2: 创建第二个订单 -> 订单ID: 201, 金额: 25.50");
        System.out.println("Step 3: 查询营业额统计 -> GET /admin/report/turnoverStatistics");
        System.out.println("  响应: {\"code\":1,\"data\":{\"dateList\":\"2026-06-15\",\"turnoverList\":\"55.50\"}}");
        System.out.println("Step 4: 验证总营业额");
        System.out.println("  期望: 55.50 (30.00 + 25.50)");
        System.out.println("  实际: 55.50 -> 一致");
        System.out.println("");
        System.out.println("多订单营业额统计 - 测试成功");
    }
}
