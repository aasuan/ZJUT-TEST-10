package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql(scripts = {"/schema-h2.sql", "/data-flavor.sql"})
@DisplayName("DishFlavorMapper 测试")
class DishFlavorMapperTest {

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    // ============================================================
    // insertBatch — 批量插入口味
    // ============================================================

    @Nested
    @DisplayName("批量插入口味 insertBatch")
    class InsertBatchTest {

        /**
         * TC-DISH-001: 批量插入 2 条口味
         */
        @Test
        @DisplayName("TC-DISH-001: 批量插入 2 条口味后应能查到")
        void shouldInsertMultipleFlavors() {
            List<DishFlavor> flavors = List.of(
                    DishFlavor.builder().dishId(2L).name("辣度").value("微辣").build(),
                    DishFlavor.builder().dishId(2L).name("口味").value("咸鲜").build()
            );

            dishFlavorMapper.insertBatch(flavors);

            var result = dishFlavorMapper.getByDishId(2L);
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name").containsExactlyInAnyOrder("辣度", "口味");
        }

        /**
         * TC-DISH-002: 空口味列表 — 不影响
         */
        @Test
        @DisplayName("TC-DISH-002: 空列表插入应无作用")
        void shouldHandleEmptyList() {
            // 不做任何插入, 直接查询应返回空
            var result = dishFlavorMapper.getByDishId(2L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("批量插入后 dishId 应被正确设置")
        void shouldSetCorrectDishId() {
            List<DishFlavor> flavors = List.of(
                    DishFlavor.builder().dishId(2L).name("分量").value("大份").build()
            );

            dishFlavorMapper.insertBatch(flavors);

            var result = dishFlavorMapper.getByDishId(2L);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDishId()).isEqualTo(2L);
        }
    }

    // ============================================================
    // getByDishId — 根据菜品 ID 查询口味
    // ============================================================

    @Nested
    @DisplayName("根据菜品 ID 查询口味 getByDishId")
    class GetByDishIdTest {

        /**
         * TC-DISH-301: 菜品 id=1 有 2 条口味
         */
        @Test
        @DisplayName("TC-DISH-301: 有口味的菜品应返回完整口味列表")
        void shouldReturnFlavorsForDish() {
            var flavors = dishFlavorMapper.getByDishId(1L);

            assertThat(flavors).hasSize(2);
            assertThat(flavors).extracting("name").contains("辣度", "口味");
        }

        /**
         * TC-DISH-302: 菜品 id=2 无口味
         */
        @Test
        @DisplayName("TC-DISH-302: 无口味的菜品应返回空列表")
        void shouldReturnEmptyListWhenNoFlavors() {
            var flavors = dishFlavorMapper.getByDishId(2L);

            assertThat(flavors).isEmpty();
        }

        @Test
        @DisplayName("不存在的菜品 ID 应返回空列表")
        void shouldReturnEmptyListForUnknownDish() {
            var flavors = dishFlavorMapper.getByDishId(99999L);

            assertThat(flavors).isEmpty();
        }
    }

    // ============================================================
    // deleteByDishId — 按菜品 ID 删除口味
    // ============================================================

    @Nested
    @DisplayName("删除口味")
    class DeleteTest {

        /**
         * TC-DISH-401: 删除旧口味后再插入新口味
         */
        @Test
        @DisplayName("TC-DISH-401: 删除 + 重新插入口味")
        void shouldDeleteAndReinsertFlavors() {
            // 删除旧口味
            dishFlavorMapper.deleteByDishId(1L);
            assertThat(dishFlavorMapper.getByDishId(1L)).isEmpty();

            // 插入新口味
            List<DishFlavor> newFlavors = List.of(
                    DishFlavor.builder().dishId(1L).name("辣度").value("麻辣").build()
            );
            dishFlavorMapper.insertBatch(newFlavors);

            var result = dishFlavorMapper.getByDishId(1L);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("辣度");
            assertThat(result.get(0).getValue()).isEqualTo("麻辣");
        }

        /**
         * TC-DISH-202: 批量删除口味 — 测试 <foreach> SQL
         */
        @Test
        @DisplayName("TC-DISH-202: 批量删除应删除所有指定菜品口味")
        void shouldDeleteFlavorsByDishIds() {
            dishFlavorMapper.deleteByDishIds(List.of(1L, 3L));

            assertThat(dishFlavorMapper.getByDishId(1L)).isEmpty();
            assertThat(dishFlavorMapper.getByDishId(3L)).isEmpty();
            // 菜品 2 本来就没有口味, 不受影响
            assertThat(dishFlavorMapper.getByDishId(2L)).isEmpty();
        }
    }
}
