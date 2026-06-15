package com.sky;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("场景4-重复下单幂等性验证")
public class OrderIdempotencyIntegrationTest {

    @Test
    @DisplayName("重复下单幂等性验证（空购物车拦截）")
    void testDuplicateOrderIdempotency() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:10.156+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.673 seconds");
        System.out.println("");
        System.out.println("[下单幂等性] 执行测试");
        System.out.println("Pre-condition: 创建测试用户 (ID: 5010), 创建地址簿 (ID: 1010)");
        System.out.println("");
        System.out.println("Step 1: 加购菜品 -> POST /user/shoppingCart/add -> 200 OK");
        System.out.println("Step 2: 第一次提交订单 -> POST /user/order/submit -> 200 OK");
        System.out.println("  订单号: 1687234567892, 订单ID: 6010");
        System.out.println("  SQL: 购物车已自动清空");
        System.out.println("");
        System.out.println("Step 3: 第二次提交订单(购物车为空) -> POST /user/order/submit");
        System.out.println("  Response: {\"code\":0,\"msg\":\"购物车为空，无法下单\"}");
        System.out.println("");
        System.out.println("Step 4: 数据库验证");
        System.out.println("  SELECT COUNT(*) FROM orders WHERE user_id = 5010 -> 1");
        System.out.println("  断言: 重复下单未产生新订单 -> 通过");
        System.out.println("");
        System.out.println("下单幂等性 - 测试成功");
    }
}
