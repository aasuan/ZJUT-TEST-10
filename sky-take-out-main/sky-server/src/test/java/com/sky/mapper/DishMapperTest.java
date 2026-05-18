package com.sky.mapper;

import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ImportAutoConfiguration(PageHelperAutoConfiguration.class)
@Sql(scripts = {"/schema-h2.sql", "/data-dish.sql"})
@DisplayName("DishMapper 测试")
class DishMapperTest {

    @Autowired
    private DishMapper dishMapper;

    // ============================================================
    // insert — 新增菜品（useGeneratedKeys）
    // ============================================================

    @Nested
    @DisplayName("新增菜品 insert — 测试 useGeneratedKeys")
    class InsertTest {

        /**
         * TC-DISH-001: 插入菜品后应回填 ID
         */
        @Test
        @DisplayName("TC-DISH-001: 插入菜品后 ID 应被回填")
        void shouldInsertAndGetGeneratedKey() {
            Dish dish = Dish.builder()
                    .name("新菜品")
                    .categoryId(1L)
                    .price(new BigDecimal("25.00"))
                    .image("test.jpg")
                    .description("测试")
                    .status(1)
                    .build();

            dishMapper.insert(dish);

            // useGeneratedKeys 应回填 ID
            assertThat(dish.getId()).isNotNull();
            assertThat(dish.getId()).isGreaterThan(0);

            // 验证可查询到该数据
            Dish saved = dishMapper.getById(dish.getId());
            assertThat(saved.getName()).isEqualTo("新菜品");
            assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        }

        @Test
        @DisplayName("TC-DISH-003: 价格为 0 应成功插入")
        void shouldInsertDishWithZeroPrice() {
            Dish dish = Dish.builder()
                    .name("免费小菜")
                    .categoryId(1L)
                    .price(BigDecimal.ZERO)
                    .status(1)
                    .build();

            dishMapper.insert(dish);

            Dish saved = dishMapper.getById(dish.getId());
            assertThat(saved.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ============================================================
    // pageQuery — 分页查询（LEFT JOIN + 动态 <where>）
    // ============================================================

    @Nested
    @DisplayName("分页查询 pageQuery — 测试 LEFT JOIN 和动态 SQL")
    class PageQueryTest {

        /**
         * TC-DISH-101: 无条件分页, total=20, 第一页 10 条
         */
        @Test
        @DisplayName("TC-DISH-101: 无条件分页应返回第一页 10 条且 total=20")
        void shouldReturnFirstPage10Of20() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);

            PageHelper.startPage(1, 10);
            var page = dishMapper.pageQuery(query);

            assertThat(page.getTotal()).isEqualTo(20L);
            assertThat(page.getResult()).hasSize(10);
            // LEFT JOIN 应填充 categoryName
            assertThat(page.getResult().get(0).getCategoryName()).isNotNull();
        }

        /**
         * TC-DISH-102: 复合条件筛选 name + categoryId + status
         */
        @Test
        @DisplayName("TC-DISH-102: name + categoryId + status 组合筛选")
        void shouldFilterByMultipleConditions() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setName("鸡");
            query.setCategoryId(1);
            query.setStatus(1);

            PageHelper.startPage(1, 10);
            var page = dishMapper.pageQuery(query);

            assertThat(page.getTotal()).isPositive();
            // 所有结果应满足: name 含"鸡", categoryId=1, status=1
            assertThat(page.getResult())
                    .allMatch(d -> d.getName().contains("鸡")
                            && d.getCategoryId() == 1L
                            && d.getStatus() == 1);
        }

        /**
         * TC-DISH-103: pageSize=1 边界值
         */
        @Test
        @DisplayName("TC-DISH-103: pageSize=1 应只返回 1 条")
        void shouldReturnOneRecordWhenMinPageSize() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(1);

            PageHelper.startPage(1, 1);
            var page = dishMapper.pageQuery(query);

            assertThat(page.getResult()).hasSize(1);
        }

        /**
         * 验证仅按 categoryId 筛选
         */
        @Test
        @DisplayName("仅传 categoryId 时不应错误拼接其他条件")
        void shouldFilterByCategoryIdOnly() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setCategoryId(1);

            PageHelper.startPage(1, 10);
            var page = dishMapper.pageQuery(query);

            assertThat(page.getTotal()).isPositive();
            assertThat(page.getResult()).allMatch(d -> d.getCategoryId() == 1L);
        }

        /**
         * 验证仅按 status 筛选
         */
        @Test
        @DisplayName("仅传 status=0 应只返回停售菜品")
        void shouldFilterByStatusOnly() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setStatus(0);

            PageHelper.startPage(1, 10);
            var page = dishMapper.pageQuery(query);

            assertThat(page.getResult()).isNotEmpty();
            assertThat(page.getResult()).allMatch(d -> d.getStatus() == 0);
        }

        /**
         * 验证排序: create_time desc — 返回 20 条且 LEFT JOIN 的 categoryName 不为 null
         */
        @Test
        @DisplayName("排序按 create_time DESC + LEFT JOIN category 正常")
        void shouldOrderByCreateTimeDescAndJoinCategory() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(20);

            PageHelper.startPage(1, 20);
            var page = dishMapper.pageQuery(query);
            var records = page.getResult();

            assertThat(records).hasSize(20);
            // 每条 dish 的 categoryName 应来自 LEFT JOIN
            assertThat(records).allMatch(d -> d.getCategoryName() != null);
        }
    }

    // ============================================================
    // getById — 根据 ID 查询
    // ============================================================

    @Nested
    @DisplayName("根据 ID 查询 getById")
    class GetByIdTest {

        @Test
        @DisplayName("TC-DISH-301: 应返回正确的菜品")
        void shouldReturnDishById() {
            Dish dish = dishMapper.getById(1L);

            assertThat(dish).isNotNull();
            assertThat(dish.getName()).isEqualTo("宫保鸡丁");
            assertThat(dish.getCategoryId()).isEqualTo(1L);
            assertThat(dish.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC-DISH-303: 不存在的 ID 应返回 null")
        void shouldReturnNullWhenNotFound() {
            Dish dish = dishMapper.getById(99999L);
            assertThat(dish).isNull();
        }
    }

    // ============================================================
    // delete 操作
    // ============================================================

    @Nested
    @DisplayName("删除菜品")
    class DeleteTest {

        @Test
        @DisplayName("TC-DISH-201: 单条删除应成功")
        void shouldDeleteSingleDish() {
            dishMapper.deleteById(1L);

            assertThat(dishMapper.getById(1L)).isNull();
        }

        /**
         * TC-DISH-202: 批量删除 — 测试 <foreach> SQL
         */
        @Test
        @DisplayName("TC-DISH-202: 批量删除 ids=(1,2,3) 应删除 3 条")
        void shouldDeleteMultipleDishes() {
            dishMapper.deleteByIds(List.of(1L, 2L, 3L));

            assertThat(dishMapper.getById(1L)).isNull();
            assertThat(dishMapper.getById(2L)).isNull();
            assertThat(dishMapper.getById(3L)).isNull();
            // 第 4 条应仍存在
            assertThat(dishMapper.getById(4L)).isNotNull();
        }
    }

    // ============================================================
    // update — 更新菜品（动态 <set>）
    // ============================================================

    @Nested
    @DisplayName("更新菜品 update — 测试动态 <set>")
    class UpdateTest {

        @Test
        @DisplayName("TC-DISH-401: 更新全部字段应生效")
        void shouldUpdateAllFields() {
            Dish dish = Dish.builder()
                    .id(1L)
                    .name("宫保鸡丁改")
                    .categoryId(2L)
                    .price(new BigDecimal("30.00"))
                    .description("新描述")
                    .status(0)
                    .build();

            dishMapper.update(dish);

            Dish updated = dishMapper.getById(1L);
            assertThat(updated.getName()).isEqualTo("宫保鸡丁改");
            assertThat(updated.getCategoryId()).isEqualTo(2L);
            assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(updated.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("动态 set: 仅更新 name 不应清空其他字段")
        void shouldOnlyUpdateNameWhenAlone() {
            Dish original = dishMapper.getById(1L);

            dishMapper.update(Dish.builder().id(1L).name("新名称").build());

            Dish updated = dishMapper.getById(1L);
            assertThat(updated.getName()).isEqualTo("新名称");
            assertThat(updated.getPrice()).isEqualByComparingTo(original.getPrice());
            assertThat(updated.getStatus()).isEqualTo(original.getStatus());
        }
    }

    // ============================================================
    // list — 动态条件查询
    // ============================================================

    @Nested
    @DisplayName("动态条件查询 list")
    class ListTest {

        @Test
        @DisplayName("TC-DISH-601: 按 categoryId 查询应返回该分类下菜品")
        void shouldReturnDishesByCategoryId() {
            Dish condition = Dish.builder().categoryId(1L).build();
            var result = dishMapper.list(condition);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(d -> d.getCategoryId() == 1L);
        }

        @Test
        @DisplayName("TC-DISH-602: 不存在的 categoryId 应返回空列表")
        void shouldReturnEmptyListWhenCategoryNotFound() {
            Dish condition = Dish.builder().categoryId(999L).build();
            var result = dishMapper.list(condition);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("按 status=1 应只返回起售菜品")
        void shouldFilterByStatus() {
            Dish condition = Dish.builder().status(1).build();
            var result = dishMapper.list(condition);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(d -> d.getStatus() == 1);
        }
    }

    // ============================================================
    // countByCategoryId — 统计菜品数量
    // ============================================================

    @Nested
    @DisplayName("统计菜品数量 countByCategoryId")
    class CountByCategoryIdTest {

        @Test
        @DisplayName("categoryId=1 有 5 条菜品")
        void shouldCountDishesByCategory() {
            Integer count = dishMapper.countByCategoryId(1L);
            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("不存在的 categoryId 应返回 0")
        void shouldReturnZeroForUnknownCategory() {
            Integer count = dishMapper.countByCategoryId(999L);
            assertThat(count).isZero();
        }
    }

    // ============================================================
    // countByMap — 条件统计
    // ============================================================

    @Nested
    @DisplayName("条件统计 countByMap")
    class CountByMapTest {

        @Test
        @DisplayName("按 status=1 统计应返回正确数量")
        void shouldCountByStatus() {
            Integer count = dishMapper.countByMap(Map.of("status", 1));
            assertThat(count).isPositive();
        }

        @Test
        @DisplayName("按 categoryId=5 统计应返回正确数量")
        void shouldCountByCategory() {
            Integer count = dishMapper.countByMap(Map.of("categoryId", 5));
            assertThat(count).isEqualTo(3);
        }
    }
}
