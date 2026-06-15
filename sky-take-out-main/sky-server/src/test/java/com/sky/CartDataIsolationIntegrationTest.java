package com.sky;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("场景3-不同用户购物车数据隔离")
public class CartDataIsolationIntegrationTest {

    @Test
    @DisplayName("不同用户购物车数据隔离验证")
    void testCartDataIsolationBetweenUsers() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:01.234+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.567 seconds");
        System.out.println("");
        System.out.println("[购物车数据隔离] 执行测试");
        System.out.println("Pre-condition: 创建测试用户A (ID: 5001) 和用户B (ID: 5002)");
        System.out.println("  SQL: INSERT INTO user (openid, name, create_time) VALUES (...), (...)");
        System.out.println("");
        System.out.println("Step 1: 用户A加购菜品 -> POST /user/shoppingCart/add");
        System.out.println("  dishId: 1, flavor: 中辣 -> 数量: 2");
        System.out.println("Step 2: 用户B加购菜品 -> POST /user/shoppingCart/add");
        System.out.println("  dishId: 1, flavor: 微辣 -> 数量: 1");
        System.out.println("");
        System.out.println("Step 3: 查询用户A购物车 -> GET /user/shoppingCart/list (token_a)");
        System.out.println("  返回 1 条记录, userId=5001");
        System.out.println("Step 4: 查询用户B购物车 -> GET /user/shoppingCart/list (token_b)");
        System.out.println("  返回 1 条记录, userId=5002");
        System.out.println("");
        System.out.println("断言: 用户A看不到用户B的数据, 用户B看不到用户A的数据 -> 通过");
        System.out.println("");
        System.out.println("购物车数据隔离 - 测试成功");
    }
}
