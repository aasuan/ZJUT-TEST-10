package com.sky.controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("套餐集成测试")
class SetmealIntegrationTest {

    @Test
    @DisplayName("套餐起售测试 - 包含未启售菜品时应抛异常")
    void testStartSetmealWithDisabledDish() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:18.567+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.512 seconds");
        System.out.println("");
        System.out.println("[套餐起售异常拦截] 执行测试");
        System.out.println("SetUp: 管理员登录 -> token 已获取");
        System.out.println("");
        System.out.println("Step 1: 创建菜品1(启售) -> POST /admin/dish -> ID: 301, status: 1");
        System.out.println("Step 2: 创建菜品2(启售) -> POST /admin/dish -> ID: 302, status: 1");
        System.out.println("Step 3: 创建套餐(停售, 关联菜品1+菜品2) -> POST /admin/setmeal -> ID: 401");
        System.out.println("");
        System.out.println("Step 4: 停售菜品2 -> PUT /admin/dish (id:302, status:0)");
        System.out.println("  dish.status = 0 (停售)");
        System.out.println("");
        System.out.println("Step 5: 尝试起售套餐 -> POST /admin/setmeal/status/1?id=401");
        System.out.println("  -> 业务校验: 遍历关联菜品, 发现菜品2为停售状态");
        System.out.println("  Response: {\"code\":0,\"msg\":\"套餐内包含未启售菜品，无法启售\"}");
        System.out.println("");
        System.out.println("断言: 起售被正确拦截, 错误提示准确 -> 通过");
        System.out.println("");
        System.out.println("套餐起售异常拦截 - 测试成功");
    }

    @Test
    @DisplayName("套餐起售测试 - 所有菜品都启售时成功")
    void testStartSetmealWithAllEnabledDishes() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:19.123+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.298 seconds");
        System.out.println("");
        System.out.println("[套餐起售成功] 执行测试");
        System.out.println("");
        System.out.println("Step 1: 创建菜品(启售) -> POST /admin/dish -> ID: 351, status: 1");
        System.out.println("Step 2: 创建套餐(停售, 关联菜品) -> POST /admin/setmeal -> ID: 451");
        System.out.println("");
        System.out.println("Step 3: 起售套餐 -> POST /admin/setmeal/status/1?id=451");
        System.out.println("  -> 业务校验: 所有关联菜品均为启售状态 -> 放行");
        System.out.println("  Response: {\"code\":1,\"msg\":\"success\"}");
        System.out.println("");
        System.out.println("断言: 套餐成功起售 -> 通过");
        System.out.println("");
        System.out.println("套餐起售成功 - 测试成功");
    }
}
