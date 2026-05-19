package com.sky;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M3 模块全链路集成测试 —— 分类管理与菜品管理
 * <p>
 * 继承 BaseIntegrationTest，使用真实 Spring 容器 + 真实 Docker 数据库，
 * 通过 @Transactional 确保测试数据自动回滚，不污染公共库。
 */
@Transactional
@DisplayName("M3 模块集成测试 - 分类管理与菜品管理")
class DishIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    // ============================================================
    // 场景一：分类与菜品的强关联删除拦截
    // ============================================================

    /**
     * 业务路径：
     * 1. 调用真实服务创建一个全新的分类
     * 2. 调用真实服务创建一个新菜品，categoryId 指向该分类
     * 3. 调用删除分类接口尝试删除该分类
     * <p>
     * 断言期望：抛出 DeletionNotAllowedException（含"当前分类关联了菜品,不能删除"），
     * 且数据库中该分类依然存在。
     */
    @Test
    @DisplayName("场景一：分类下存在菜品时删除分类应抛出 DeletionNotAllowedException 且分类仍存在")
    void shouldPreventCategoryDeletionWhenDishExists() {
        // ---- 1. 创建一个全新的分类 ----
        String categoryName = "集成测试分类_S1_" + System.currentTimeMillis();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setType(1);  // 1 = 菜品分类
        categoryDTO.setName(categoryName);
        categoryDTO.setSort(1);
        categoryService.save(categoryDTO);

        // 通过分页查询找回刚创建的分类 ID（save 接口无返回值）
        CategoryPageQueryDTO pageQuery = new CategoryPageQueryDTO();
        pageQuery.setPage(1);
        pageQuery.setPageSize(10);
        pageQuery.setName(categoryName);
        PageResult<Category> pageResult = categoryService.pageQuery(pageQuery);
        assertFalse(pageResult.getRecords().isEmpty(), "刚创建的分类应能被分页查询到");
        Long categoryId = pageResult.getRecords().get(0).getId();
        assertNotNull(categoryId);

        // ---- 2. 创建一个新菜品，categoryId 指向该分类 ----
        DishDTO dishDTO = new DishDTO();
        dishDTO.setName("集成测试菜品_S1_" + System.currentTimeMillis());
        dishDTO.setCategoryId(categoryId);
        dishDTO.setPrice(new BigDecimal("28.00"));
        dishDTO.setDescription("用于验证分类删除拦截");
        dishDTO.setStatus(StatusConstant.DISABLE);
        dishService.saveWithFlavor(dishDTO);

        // 验证菜品已入库（通过分类 ID 分页查询找回菜品 ID）
        DishPageQueryDTO dishPageQuery = new DishPageQueryDTO();
        dishPageQuery.setPage(1);
        dishPageQuery.setPageSize(10);
        dishPageQuery.setCategoryId(categoryId.intValue());
        PageResult<DishVO> dishPageResult = dishService.queryPage(dishPageQuery);
        assertFalse(dishPageResult.getRecords().isEmpty(), "刚创建的菜品应能被分页查询到");
        Long dishId = dishPageResult.getRecords().get(0).getId();
        DishVO createdDish = dishService.getByIdWithFlavor(dishId);
        assertNotNull(createdDish);
        assertEquals(categoryId, createdDish.getCategoryId());

        // ---- 3. 尝试删除该分类，预期抛出业务异常 ----
        DeletionNotAllowedException exception = assertThrows(
                DeletionNotAllowedException.class,
                () -> categoryService.deleteById(categoryId),
                "删除有关联菜品的分类应抛出 DeletionNotAllowedException"
        );
        assertEquals(MessageConstant.CATEGORY_BE_RELATED_BY_DISH, exception.getMessage());

        // ---- 4. 验证数据库中该分类依然存在 ----
        PageResult<Category> afterDeleteQuery = categoryService.pageQuery(pageQuery);
        assertFalse(afterDeleteQuery.getRecords().isEmpty(),
                "删除失败后，分类应依然存在于数据库中");
        assertEquals(categoryId, afterDeleteQuery.getRecords().get(0).getId());
    }

    // ============================================================
    // 场景二：菜品及其口味的复杂生命周期流转
    // ============================================================

    /**
     * 业务路径：
     * 1. 调用真实服务创建一个包含完整口味列表（DishFlavor）的新菜品
     * 2. 调用分页查询接口查出该菜品
     * 3. 修改该菜品的口味数据（新增一个口味、修改一个口味、删除一个口味），
     *    调用修改菜品接口
     * 4. 再次调用查询接口获取该菜品详情
     * <p>
     * 断言期望：严格比对修改前后数据库中真实保存的口味数据，
     * 验证联表更新逻辑（删除旧口味、插入新口味）是否准确生效。
     */
    @Test
    @DisplayName("场景二：菜品口味生命周期——增/改/删口味后联表更新应准确生效")
    void shouldUpdateDishFlavorsCorrectly() {
        // ---- 1. 准备工作：创建分类 ----
        String categoryName = "集成测试分类_S2_" + System.currentTimeMillis();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setType(1);
        categoryDTO.setName(categoryName);
        categoryDTO.setSort(1);
        categoryService.save(categoryDTO);
        Long categoryId = getCategoryIdByName(categoryName);

        // ---- 2. 创建包含完整口味列表的新菜品 ----
        DishDTO dishDTO = new DishDTO();
        dishDTO.setName("测试菜品_口味生命周期_" + System.currentTimeMillis());
        dishDTO.setCategoryId(categoryId);
        dishDTO.setPrice(new BigDecimal("38.00"));
        dishDTO.setDescription("用于验证口味联表更新逻辑");
        dishDTO.setStatus(StatusConstant.DISABLE);

        List<DishFlavor> initialFlavors = new ArrayList<>();
        initialFlavors.add(DishFlavor.builder().name("辣度").value("微辣").build());
        initialFlavors.add(DishFlavor.builder().name("甜度").value("中等").build());
        initialFlavors.add(DishFlavor.builder().name("咸度").value("适中").build());
        dishDTO.setFlavors(initialFlavors);

        dishService.saveWithFlavor(dishDTO);

        // 查回菜品 ID
        Long dishId = getDishIdByCategory(categoryId);

        // ---- 3. 验证初始口味已正确入库 ----
        DishVO dishAfterCreate = dishService.getByIdWithFlavor(dishId);
        assertNotNull(dishAfterCreate);
        List<DishFlavor> flavorsAfterCreate = dishAfterCreate.getFlavors();
        assertEquals(3, flavorsAfterCreate.size(), "初始应保存 3 条口味");

        // 验证每条口味的 name 和 value
        assertFlavorExists(flavorsAfterCreate, "辣度", "微辣");
        assertFlavorExists(flavorsAfterCreate, "甜度", "中等");
        assertFlavorExists(flavorsAfterCreate, "咸度", "适中");

        // ---- 4. 修改菜品口味：新增一个、修改一个、删除一个 ----
        // 修改方案：
        //   - 保留"辣度"但 value 改为"中辣"（修改）
        //   - 保留"甜度"不变
        //   - 新增"酸度"→"微酸"（新增）
        //   - 删除"咸度"（删除）
        List<DishFlavor> updatedFlavors = new ArrayList<>();
        updatedFlavors.add(DishFlavor.builder().name("辣度").value("中辣").build());   // 修改
        updatedFlavors.add(DishFlavor.builder().name("甜度").value("中等").build());   // 不变
        updatedFlavors.add(DishFlavor.builder().name("酸度").value("微酸").build());   // 新增

        DishDTO updateDTO = new DishDTO();
        updateDTO.setId(dishId);
        updateDTO.setName(dishAfterCreate.getName());
        updateDTO.setCategoryId(categoryId);
        updateDTO.setPrice(new BigDecimal("38.00"));
        updateDTO.setDescription(dishAfterCreate.getDescription());
        updateDTO.setStatus(StatusConstant.DISABLE);
        updateDTO.setFlavors(updatedFlavors);

        dishService.updateWithFlavor(updateDTO);

        // ---- 5. 再次查询，验证联表更新逻辑 ----
        DishVO dishAfterUpdate = dishService.getByIdWithFlavor(dishId);
        assertNotNull(dishAfterUpdate);
        List<DishFlavor> flavorsAfterUpdate = dishAfterUpdate.getFlavors();
        assertEquals(3, flavorsAfterUpdate.size(),
                "更新后应仍有 3 条口味（删1、增1、改1，总数不变）");

        // 验证：修改生效
        assertFlavorExists(flavorsAfterUpdate, "辣度", "中辣");
        // 验证：保持不变
        assertFlavorExists(flavorsAfterUpdate, "甜度", "中等");
        // 验证：新增生效
        assertFlavorExists(flavorsAfterUpdate, "酸度", "微酸");
        // 验证：旧口味已删除
        assertFlavorNotExists(flavorsAfterUpdate, "咸度", "适中");
    }

    // ============================================================
    // 场景三：菜品的批量物理/逻辑删除验证
    // ============================================================

    /**
     * 业务路径：
     * 1. 调用服务创建 3 个独立的菜品
     * 2. 将其中 1 个菜品的状态设置为起售（status=1），另外 2 个为停售（status=0）
     * 3. 调用批量删除接口，传入这 3 个菜品的 ID → 预期抛出异常
     * 4. 只传入 2 个停售菜品 ID → 验证数据库中实际减少的记录条数等于 2
     * <p>
     * 断言期望：起售菜品阻断批量删除流程抛出 DeletionNotAllowedException；
     * 仅删除停售菜品时，数据库记录正确减少 2 条。
     */
    @Test
    @DisplayName("场景三：批量删除——起售菜品阻断删除，停售菜品物理删除成功")
    void shouldPreventBatchDeleteWhenAnyDishOnSale() {
        // ---- 1. 准备工作：创建分类 ----
        String categoryName = "集成测试分类_S3_" + System.currentTimeMillis();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setType(1);
        categoryDTO.setName(categoryName);
        categoryDTO.setSort(1);
        categoryService.save(categoryDTO);
        Long categoryId = getCategoryIdByName(categoryName);

        // ---- 2. 创建 3 个独立菜品（默认停售状态 status=0），使用唯一名称 ----
        long ts = System.currentTimeMillis();
        String name1 = "S3_菜品1_" + ts;
        String name2 = "S3_菜品2_" + ts;
        String name3 = "S3_菜品3_" + ts;

        Long dishId1 = createDishAndGetId(name1, categoryId);
        Long dishId2 = createDishAndGetId(name2, categoryId);
        Long dishId3 = createDishAndGetId(name3, categoryId);

        assertNotNull(dishId1);
        assertNotNull(dishId2);
        assertNotNull(dishId3);
        // 确保 3 个菜品 ID 不重复
        assertNotEquals(dishId1, dishId2);
        assertNotEquals(dishId1, dishId3);
        assertNotEquals(dishId2, dishId3);

        // 验证 3 个菜品均已入库
        assertEquals(3, countDishesInCategory(categoryId),
                "测试前应有 3 个菜品");

        // ---- 3. 将菜品1 设置为起售（status=1） ----
        dishService.startOrStop(StatusConstant.ENABLE, dishId1);
        Dish enabledDish = dishMapper.getById(dishId1);
        assertEquals(StatusConstant.ENABLE, enabledDish.getStatus(),
                "菜品1 应已变为起售状态");

        // ---- 4. 尝试批量删除 3 个菜品（含 1 个起售），预期抛出异常 ----
        List<Long> allThreeIds = List.of(dishId1, dishId2, dishId3);
        DeletionNotAllowedException exception = assertThrows(
                DeletionNotAllowedException.class,
                () -> dishService.deleteBatch(allThreeIds),
                "包含起售菜品的批量删除应抛出 DeletionNotAllowedException"
        );
        assertEquals(MessageConstant.DISH_ON_SALE, exception.getMessage());

        // 验证：删除失败后，3 个菜品都还在（事务已回滚该 deleteBatch 的数据库操作）
        assertNotNull(dishMapper.getById(dishId1), "删除失败后菜品1 应仍存在");
        assertNotNull(dishMapper.getById(dishId2), "删除失败后菜品2 应仍存在");
        assertNotNull(dishMapper.getById(dishId3), "删除失败后菜品3 应仍存在");

        // ---- 5. 只删除 2 个停售菜品，验证物理删除成功 ----
        List<Long> disabledIds = List.of(dishId2, dishId3);
        dishService.deleteBatch(disabledIds);

        // 断言期望：数据库实际减少 2 条记录
        assertEquals(1, countDishesInCategory(categoryId),
                "删除 2 个停售菜品后，应仅剩 1 个菜品（起售的那一个）");

        // 验证：起售菜品依然存在
        Dish remainingDish = dishMapper.getById(dishId1);
        assertNotNull(remainingDish, "起售菜品应仍存在于数据库中");
        assertEquals(StatusConstant.ENABLE, remainingDish.getStatus());

        // 验证：停售菜品已被物理删除（getById 返回 null）
        assertNull(dishMapper.getById(dishId2), "停售菜品2 应已被物理删除");
        assertNull(dishMapper.getById(dishId3), "停售菜品3 应已被物理删除");

        // 验证：停售菜品的口味数据也一并被删除
        assertEquals(0, dishFlavorMapper.getByDishId(dishId2).size(),
                "删除菜品后，关联口味也应被清除");
        assertEquals(0, dishFlavorMapper.getByDishId(dishId3).size(),
                "删除菜品后，关联口味也应被清除");
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /** 通过唯一名称分页查找分类 ID */
    private Long getCategoryIdByName(String name) {
        CategoryPageQueryDTO query = new CategoryPageQueryDTO();
        query.setPage(1);
        query.setPageSize(10);
        query.setName(name);
        PageResult<Category> result = categoryService.pageQuery(query);
        return result.getRecords().isEmpty() ? null : result.getRecords().get(0).getId();
    }

    /** 通过分类 ID 查找第一个菜品 ID */
    private Long getDishIdByCategory(Long categoryId) {
        DishPageQueryDTO query = new DishPageQueryDTO();
        query.setPage(1);
        query.setPageSize(10);
        query.setCategoryId(categoryId.intValue());
        PageResult<DishVO> result = dishService.queryPage(query);
        return result.getRecords().isEmpty() ? null : result.getRecords().get(0).getId();
    }

    /** 验证口味列表中存在指定 name+value 的组合 */
    private void assertFlavorExists(List<DishFlavor> flavors, String name, String value) {
        boolean found = flavors.stream()
                .anyMatch(f -> name.equals(f.getName()) && value.equals(f.getValue()));
        assertTrue(found, "口味列表中应包含 " + name + "=" + value);
    }

    /** 验证口味列表中不存在指定 name+value 的组合 */
    private void assertFlavorNotExists(List<DishFlavor> flavors, String name, String value) {
        boolean found = flavors.stream()
                .anyMatch(f -> name.equals(f.getName()) && value.equals(f.getValue()));
        assertFalse(found, "口味列表中不应包含已删除的 " + name + "=" + value);
    }

    /** 创建一个停售状态的菜品（使用明确名称）并返回其 ID */
    private Long createDishAndGetId(String name, Long categoryId) {
        DishDTO dto = new DishDTO();
        dto.setName(name);
        dto.setCategoryId(categoryId);
        dto.setPrice(new BigDecimal("25.00"));
        dto.setStatus(StatusConstant.DISABLE);
        dishService.saveWithFlavor(dto);
        // 通过唯一名称分页查找刚创建的菜品 ID
        DishPageQueryDTO query = new DishPageQueryDTO();
        query.setPage(1);
        query.setPageSize(10);
        query.setName(name);
        PageResult<DishVO> result = dishService.queryPage(query);
        return result.getRecords().stream()
                .filter(d -> name.equals(d.getName()))
                .findFirst()
                .map(DishVO::getId)
                .orElse(null);
    }

    /** 统计某个分类下的菜品数量 */
    private int countDishesInCategory(Long categoryId) {
        DishPageQueryDTO query = new DishPageQueryDTO();
        query.setPage(1);
        query.setPageSize(100);
        query.setCategoryId(categoryId.intValue());
        PageResult<DishVO> result = dishService.queryPage(query);
        return (int) result.getTotal();
    }
}
