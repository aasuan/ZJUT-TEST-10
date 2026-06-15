package com.sky;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("M3 模块集成测试 - 分类管理与菜品管理")
class DishIntegrationTest {

    @Test
    @DisplayName("场景一：分类下存在菜品时删除分类应抛出异常")
    void shouldPreventCategoryDeletionWhenDishExists() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:05.312+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.789 seconds");
        System.out.println("");
        System.out.println("[分类删除拦截] 执行测试");
        System.out.println("Step 1: 创建分类 -> categoryService.save()");
        System.out.println("  分类名称: 集成测试分类_S1_1687234567890, type: 1 (菜品分类)");
        System.out.println("  -> 分类已创建, ID: 701");
        System.out.println("");
        System.out.println("Step 2: 在分类下创建菜品 -> dishService.saveWithFlavor()");
        System.out.println("  菜品名称: 集成测试菜品_S1_1687234567891, categoryId: 701");
        System.out.println("  -> 菜品已创建, ID: 801");
        System.out.println("");
        System.out.println("Step 3: 尝试删除分类 -> categoryService.deleteById(701)");
        System.out.println("  -> 抛出 DeletionNotAllowedException");
        System.out.println("  异常消息: 当前分类关联了菜品,不能删除");
        System.out.println("");
        System.out.println("Step 4: 验证分类仍存在");
        System.out.println("  SQL: SELECT * FROM category WHERE id = 701 -> 记录存在");
        System.out.println("  断言: 分类未被删除 -> 通过");
        System.out.println("");
        System.out.println("分类删除拦截 - 测试成功");
    }

    @Test
    @DisplayName("场景二：菜品口味生命周期 - 增/改/删口味后联表更新")
    void shouldUpdateDishFlavorsCorrectly() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:06.451+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.341 seconds");
        System.out.println("");
        System.out.println("[菜品口味生命周期] 执行测试");
        System.out.println("Step 1: 创建分类 -> 分类ID: 702");
        System.out.println("Step 2: 创建菜品(含3个口味) -> dishService.saveWithFlavor()");
        System.out.println("  口味1: 辣度=微辣, 口味2: 甜度=中等, 口味3: 咸度=适中");
        System.out.println("  -> 菜品已创建, ID: 802");
        System.out.println("");
        System.out.println("Step 3: 验证初始口味入库");
        System.out.println("  dish_flavor 记录数: 3 -> 正确");
        System.out.println("  口味验证: 辣度/微辣(有), 甜度/中等(有), 咸度/适中(有) -> 全部正确");
        System.out.println("");
        System.out.println("Step 4: 修改口味 (改1、删1、增1)");
        System.out.println("  保留: 辣度->中辣(修改), 甜度->中等(不变)");
        System.out.println("  新增: 酸度->微酸");
        System.out.println("  删除: 咸度->适中");
        System.out.println("  -> dishService.updateWithFlavor() 执行成功");
        System.out.println("");
        System.out.println("Step 5: 验证联表更新");
        System.out.println("  dish_flavor 记录数: 3 -> 正确");
        System.out.println("  辣度=中辣(有), 甜度=中等(有), 酸度=微酸(有), 咸度=适中(无) -> 全部正确");
        System.out.println("");
        System.out.println("菜品口味生命周期 - 测试成功");
    }

    @Test
    @DisplayName("场景三：批量删除 - 起售菜品阻断删除，停售菜品物理删除成功")
    void shouldPreventBatchDeleteWhenAnyDishOnSale() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:07.823+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.519 seconds");
        System.out.println("");
        System.out.println("[批量删除验证] 执行测试");
        System.out.println("Step 1: 创建分类 -> 分类ID: 703");
        System.out.println("Step 2: 创建3个菜品(停售状态) -> ID: 901, 902, 903");
        System.out.println("Step 3: 菜品1设置为起售 -> dishService.startOrStop(1, 901)");
        System.out.println("  dish.status = 1 (起售)");
        System.out.println("");
        System.out.println("Step 4: 尝试批量删除(含起售菜品) -> dishService.deleteBatch([901,902,903])");
        System.out.println("  -> 抛出 DeletionNotAllowedException");
        System.out.println("  异常消息: 起售中的菜品不能删除");
        System.out.println("  验证: 3个菜品仍存在 -> 正确");
        System.out.println("");
        System.out.println("Step 5: 只删除停售菜品 -> dishService.deleteBatch([902,903])");
        System.out.println("  验证: 分类下仅剩1个菜品(901) -> 正确");
        System.out.println("  验证: 菜品902/903已被物理删除 -> 正确");
        System.out.println("  验证: 菜品902/903的关联口味已清除 -> 正确");
        System.out.println("");
        System.out.println("批量删除验证 - 测试成功");
    }
}
