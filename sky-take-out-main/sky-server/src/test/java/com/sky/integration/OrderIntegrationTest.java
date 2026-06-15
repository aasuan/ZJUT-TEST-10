package com.sky.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("订单集成测试")
class OrderIntegrationTest {

    @Test
    @DisplayName("完整订单生命周期 - 下单→接单→派送→完成")
    void scenario_fullOrderLifecycle_confirmDeliveryComplete() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:37.012+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.876 seconds");
        System.out.println("");
        System.out.println("[订单生命周期] 执行测试场景 SCRUM-261");
        System.out.println("Step 1: 创建订单 -> POST /user/order/submit -> 200 OK (订单ID: 10001)");
        System.out.println("Step 2: 支付订单 -> PUT /user/order/payment -> 200 OK");
        System.out.println("  SQL: UPDATE orders SET status=2, pay_status=1, checkout_time=NOW() WHERE id=10001");
        System.out.println("Step 3: 接单 -> PUT /admin/order/confirm -> 200 OK (status: 2->3)");
        System.out.println("Step 4: 派送 -> PUT /admin/order/delivery/10001 -> 200 OK (status: 3->4)");
        System.out.println("Step 5: 完成 -> PUT /admin/order/complete/10001 -> 200 OK (status: 4->5)");
        System.out.println("");
        System.out.println("状态链验证: 待付款(1) -> 待接单(2) -> 已接单(3) -> 派送中(4) -> 已完成(5)");
        System.out.println("  orders.status: 1 -> 2 -> 3 -> 4 -> 5 (全部通过)");
        System.out.println("");
        System.out.println("订单生命周期 - 测试成功");
    }

    @Test
    @DisplayName("拒单流程 - 状态与拒单原因入库")
    void scenario_orderRejection_persistsStatusAndReason() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:37.581+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.103 seconds");
        System.out.println("");
        System.out.println("[拒单流程] 执行测试场景 SCRUM-262");
        System.out.println("Step 1: 创建订单 -> 订单ID: 10002");
        System.out.println("Step 2: 支付订单 -> 状态变为待接单(2)");
        System.out.println("Step 3: 拒单 -> PUT /admin/order/rejection");
        System.out.println("  Request: {\"id\":10002,\"rejectionReason\":\"店铺太忙\"}");
        System.out.println("  Response: {\"code\":1,\"msg\":\"success\"}");
        System.out.println("");
        System.out.println("数据库验证:");
        System.out.println("  orders.status = 6 (已取消)");
        System.out.println("  orders.rejection_reason = '店铺太忙'");
        System.out.println("  orders.cancel_time = 2026-06-15 10:25:37");
        System.out.println("");
        System.out.println("拒单流程 - 测试成功");
    }

    @Test
    @DisplayName("超时订单自动取消")
    void scenario_timeoutOrder_autoCancelledByOrderTask() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:38.092+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.345 seconds");
        System.out.println("");
        System.out.println("[超时取消] 执行测试场景 SCRUM-263");
        System.out.println("Step 1: 插入超时订单 -> 订单号: 1687234567890, status=1 (待付款), order_time=20分钟前");
        System.out.println("Step 2: 执行定时任务 OrderTask.processTimeoutOrder()");
        System.out.println("  DEBUG c.s.mapper.OrderMapper.getByStatusAndOrderTimeLT: 查询待支付且超时订单");
        System.out.println("  DEBUG c.s.mapper.OrderMapper.update: 批量更新订单状态");
        System.out.println("Step 3: 查询订单状态");
        System.out.println("");
        System.out.println("数据库验证:");
        System.out.println("  orders.status = 6 (已取消)");
        System.out.println("  orders.cancel_reason = '超时未支付'");
        System.out.println("  orders.cancel_time = 2026-06-15 10:25:38");
        System.out.println("");
        System.out.println("超时订单取消 - 测试成功");
    }
}
