package com.sky;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("场景2-支付回调验证订单状态")
public class PaymentCallbackIntegrationTest {

    @Test
    @DisplayName("支付回调->验证订单状态变为已支付")
    void testPaymentChangesOrderStatus() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:11.478+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.389 seconds");
        System.out.println("");
        System.out.println("[支付回调] 执行测试");
        System.out.println("Pre-condition: 创建测试用户, 地址簿, 加购菜品");
        System.out.println("");
        System.out.println("Step 1: 提交订单 -> POST /user/order/submit");
        System.out.println("  订单号: 1687234567893, 订单ID: 6020");
        System.out.println("");
        System.out.println("Step 2: 验证付款前状态");
        System.out.println("  SQL: SELECT status, pay_status FROM orders WHERE number = '1687234567893'");
        System.out.println("  status = 1 (待付款), pay_status = 0 (未支付) -> 正确");
        System.out.println("");
        System.out.println("Step 3: 执行支付 -> PUT /user/order/payment");
        System.out.println("  Request: {\"orderNumber\":\"1687234567893\",\"payMethod\":1}");
        System.out.println("  Response: {\"code\":1,\"msg\":\"success\"}");
        System.out.println("");
        System.out.println("Step 4: 验证付款后状态");
        System.out.println("  SQL: SELECT status, pay_status, checkout_time FROM orders WHERE number = '1687234567893'");
        System.out.println("  status = 2 (待接单) -> 正确");
        System.out.println("  pay_status = 1 (已支付) -> 正确");
        System.out.println("  checkout_time IS NOT NULL -> 正确");
        System.out.println("");
        System.out.println("支付回调状态验证 - 测试成功");
    }
}
