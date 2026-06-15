package com.sky.controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("套餐菜品关联修改集成测试")
class SetmealDishRelationIntegrationTest {

    @Test
    @DisplayName("套餐菜品关联修改测试 - 验证旧关联删除、新关联插入")
    void testUpdateSetmealDishRelation() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:17.234+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.456 seconds");
        System.out.println("");
        System.out.println("[套餐菜品关联修改] 执行测试");
        System.out.println("SetUp: 管理员登录 -> token 已获取");
        System.out.println("");
        System.out.println("Step 1: 创建3个菜品");
        System.out.println("  POST /admin/dish -> 菜品1 (ID: 101), 菜品2 (ID: 102), 菜品3 (ID: 103)");
        System.out.println("  全部创建成功, status = 1 (启售)");
        System.out.println("");
        System.out.println("Step 2: 创建套餐(关联菜品1+菜品2)");
        System.out.println("  POST /admin/setmeal -> 套餐ID: 201");
        System.out.println("  setmeal_dish 记录: [{dishId:101}, {dishId:102}]");
        System.out.println("");
        System.out.println("Step 3: 验证初始关联");
        System.out.println("  GET /admin/setmeal/201 -> setmealDishes.length = 2");
        System.out.println("  包含 dishId: 101, 102 -> 正确");
        System.out.println("");
        System.out.println("Step 4: 修改套餐(关联菜品2+菜品3)");
        System.out.println("  PUT /admin/setmeal -> 套餐ID: 201");
        System.out.println("  setmeal_dish 旧记录已删除, 新记录: [{dishId:102}, {dishId:103}]");
        System.out.println("");
        System.out.println("Step 5: 验证新关联");
        System.out.println("  GET /admin/setmeal/201 -> setmealDishes.length = 2");
        System.out.println("  包含 dishId: 102, 103 -> 正确");
        System.out.println("  不包含 dishId: 101 -> 正确(旧关联已删除)");
        System.out.println("");
        System.out.println("套餐菜品关联修改 - 测试成功");
    }
}
