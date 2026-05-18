package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜品管理 Service 层测试")
class DishServiceImplTest {

    @Mock
    private DishMapper dishMapper;

    @Mock
    private DishFlavorMapper dishFlavorMapper;

    @Mock
    private SetmealDishMapper setmealDishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private Page<DishVO> page;

    @InjectMocks
    private DishServiceImpl dishService;

    // ============================================================
    // saveWithFlavor — 新增菜品及口味
    // ============================================================

    @Nested
    @DisplayName("新增菜品 saveWithFlavor")
    class SaveWithFlavorTest {

        /**
         * TC-DISH-001: 完整 DishDTO 含 2 条口味 → 菜品+口味同时入库
         */
        @Test
        @DisplayName("TC-DISH-001 含口味时应新增菜品和口味")
        void shouldSaveDishAndFlavors() {
            DishDTO dto = new DishDTO();
            dto.setName("宫保鸡丁");
            dto.setCategoryId(1L);
            dto.setPrice(new BigDecimal("28.00"));
            dto.setFlavors(List.of(
                    DishFlavor.builder().name("辣度").value("微辣").build(),
                    DishFlavor.builder().name("口味").value("咸鲜").build()
            ));

            dishService.saveWithFlavor(dto);

            verify(dishMapper).insert(any(Dish.class));
            verify(dishFlavorMapper).insertBatch(argThat(flavors ->
                    flavors.size() == 2));
        }

        /**
         * TC-DISH-002: 无口味 flavors=[]
         */
        @Test
        @DisplayName("TC-DISH-002 无口味时应只新增菜品不新增口味")
        void shouldSaveDishOnlyWhenNoFlavors() {
            DishDTO dto = new DishDTO();
            dto.setName("白米饭");
            dto.setCategoryId(2L);
            dto.setPrice(new BigDecimal("2.00"));
            dto.setFlavors(List.of());

            dishService.saveWithFlavor(dto);

            verify(dishMapper).insert(any(Dish.class));
            verify(dishFlavorMapper, never()).insertBatch(any());
        }

        /**
         * TC-DISH-003: price=0.00 边界值
         */
        @Test
        @DisplayName("TC-DISH-003 价格为 0 应成功保存")
        void shouldSaveDishWithZeroPrice() {
            DishDTO dto = new DishDTO();
            dto.setName("免费小菜");
            dto.setCategoryId(1L);
            dto.setPrice(BigDecimal.ZERO);

            dishService.saveWithFlavor(dto);

            verify(dishMapper).insert(argThat(d ->
                    d.getPrice().compareTo(BigDecimal.ZERO) == 0));
        }
    }

    // ============================================================
    // queryPage — 分页查询
    // ============================================================

    @Nested
    @DisplayName("分页查询 queryPage")
    class QueryPageTest {

        @Test
        @DisplayName("TC-DISH-101 无条件分页应返回 total 和列表")
        void shouldReturnPageResult() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);

            List<DishVO> records = List.of(
                    DishVO.builder().id(1L).name("菜品1").build(),
                    DishVO.builder().id(2L).name("菜品2").build()
            );
            when(page.getTotal()).thenReturn(20L);
            when(page.getResult()).thenReturn(records);
            when(dishMapper.pageQuery(query)).thenReturn(page);

            PageResult<DishVO> result = dishService.queryPage(query);

            assertThat(result.getTotal()).isEqualTo(20L);
            assertThat(result.getRecords()).hasSize(2);
        }

        @Test
        @DisplayName("TC-DISH-102 复合条件筛选应传参给 Mapper")
        void shouldPassCompositeFilter() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setName("鸡");
            query.setCategoryId(1);
            query.setStatus(1);

            when(page.getTotal()).thenReturn(1L);
            when(page.getResult()).thenReturn(List.of());
            when(dishMapper.pageQuery(query)).thenReturn(page);

            PageResult<DishVO> result = dishService.queryPage(query);

            assertThat(result.getTotal()).isEqualTo(1L);
            verify(dishMapper).pageQuery(argThat(q ->
                    "鸡".equals(q.getName()) && q.getCategoryId() == 1 && q.getStatus() == 1));
        }

        @Test
        @DisplayName("TC-DISH-103 pageSize=1 应只返回 1 条")
        void shouldHandleMinPageSize() {
            DishPageQueryDTO query = new DishPageQueryDTO();
            query.setPage(1);
            query.setPageSize(1);

            when(page.getTotal()).thenReturn(20L);
            when(page.getResult()).thenReturn(List.of(DishVO.builder().build()));
            when(dishMapper.pageQuery(query)).thenReturn(page);

            PageResult<DishVO> result = dishService.queryPage(query);

            assertThat(result.getRecords()).hasSize(1);
        }
    }

    // ============================================================
    // deleteBatch — 批量删除
    // ============================================================

    @Nested
    @DisplayName("批量删除 deleteBatch")
    class DeleteBatchTest {

        /**
         * TC-DISH-201: 单个菜品未起售且未关联套餐 → 删除成功
         */
        @Test
        @DisplayName("TC-DISH-201 未起售未关联套餐时应成功删除")
        void shouldDeleteSingleDish() {
            Dish dish = Dish.builder().id(1L).status(StatusConstant.DISABLE).build();
            when(dishMapper.getById(1L)).thenReturn(dish);
            when(setmealDishMapper.getSetmealIdByDishIds(List.of(1L))).thenReturn(List.of());

            dishService.deleteBatch(List.of(1L));

            verify(dishMapper).deleteByIds(List.of(1L));
            verify(dishFlavorMapper).deleteByDishIds(List.of(1L));
        }

        /**
         * TC-DISH-202: 批量删除 3 个菜品
         */
        @Test
        @DisplayName("TC-DISH-202 批量删除应全部成功")
        void shouldDeleteMultipleDishes() {
            for (long id : List.of(1L, 2L, 3L)) {
                when(dishMapper.getById(id))
                        .thenReturn(Dish.builder().id(id).status(StatusConstant.DISABLE).build());
            }
            when(setmealDishMapper.getSetmealIdByDishIds(List.of(1L, 2L, 3L)))
                    .thenReturn(List.of());

            dishService.deleteBatch(List.of(1L, 2L, 3L));

            verify(dishMapper).deleteByIds(List.of(1L, 2L, 3L));
        }

        /**
         * TC-DISH-203: 菜品已关联起售套餐 → 抛异常
         */
        @Test
        @DisplayName("TC-DISH-203 关联套餐时应抛出业务异常")
        void shouldThrowWhenRelatedToSetmeal() {
            for (long id : List.of(5L)) {
                when(dishMapper.getById(id))
                        .thenReturn(Dish.builder().id(id).status(StatusConstant.DISABLE).build());
            }
            when(setmealDishMapper.getSetmealIdByDishIds(List.of(5L)))
                    .thenReturn(List.of(10L));

            assertThatThrownBy(() -> dishService.deleteBatch(List.of(5L)))
                    .isInstanceOf(DeletionNotAllowedException.class)
                    .hasMessage(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

            verify(dishMapper, never()).deleteByIds(any());
        }

        /**
         * 补充: 起售中的菜品不可删除
         */
        @Test
        @DisplayName("起售中的菜品删除应抛异常")
        void shouldThrowWhenDishIsOnSale() {
            when(dishMapper.getById(1L))
                    .thenReturn(Dish.builder().id(1L).status(StatusConstant.ENABLE).build());

            assertThatThrownBy(() -> dishService.deleteBatch(List.of(1L)))
                    .isInstanceOf(DeletionNotAllowedException.class)
                    .hasMessage(MessageConstant.DISH_ON_SALE);

            verify(dishMapper, never()).deleteByIds(any());
        }
    }

    // ============================================================
    // getByIdWithFlavor — 根据 ID 查询
    // ============================================================

    @Nested
    @DisplayName("根据 ID 查询 getByIdWithFlavor")
    class GetByIdWithFlavorTest {

        /**
         * TC-DISH-301: 有 2 条口味
         */
        @Test
        @DisplayName("TC-DISH-301 有口味时应返回含口味的 DishVO")
        void shouldReturnDishWithFlavors() {
            Dish dish = Dish.builder().id(1L).name("宫保鸡丁").categoryId(1L)
                    .price(new BigDecimal("28.00")).status(1).build();
            List<DishFlavor> flavors = List.of(
                    DishFlavor.builder().name("辣度").value("微辣").build(),
                    DishFlavor.builder().name("口味").value("咸鲜").build()
            );

            when(dishMapper.getById(1L)).thenReturn(dish);
            when(dishFlavorMapper.getByDishId(1L)).thenReturn(flavors);

            DishVO result = dishService.getByIdWithFlavor(1L);

            assertThat(result.getName()).isEqualTo("宫保鸡丁");
            assertThat(result.getFlavors()).hasSize(2);
        }

        /**
         * TC-DISH-302: 无口味
         */
        @Test
        @DisplayName("TC-DISH-302 无口味时应返回空 flavor 列表")
        void shouldReturnDishWithoutFlavors() {
            Dish dish = Dish.builder().id(2L).name("白米饭").build();
            when(dishMapper.getById(2L)).thenReturn(dish);
            when(dishFlavorMapper.getByDishId(2L)).thenReturn(List.of());

            DishVO result = dishService.getByIdWithFlavor(2L);

            assertThat(result.getFlavors()).isEmpty();
        }

        /**
         * TC-DISH-303: 菜品不存在 → dishMapper 返回 null, NPE or should throw
         */
        @Test
        @DisplayName("TC-DISH-303 菜品不存在时 getById 返回 null")
        void shouldReturnNullWhenDishNotFound() {
            when(dishMapper.getById(99999L)).thenReturn(null);

            // BeanUtils.copyProperties(null, ...) 抛出 IllegalArgumentException
            assertThatThrownBy(() -> dishService.getByIdWithFlavor(99999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Source must not be null");
        }
    }

    // ============================================================
    // updateWithFlavor — 更新菜品及口味
    // ============================================================

    @Nested
    @DisplayName("更新菜品 updateWithFlavor")
    class UpdateWithFlavorTest {

        /**
         * TC-DISH-401: 替换口味 → 旧口味删除, 新口味保存
         */
        @Test
        @DisplayName("TC-DISH-401 替换口味应先删旧再存新")
        void shouldReplaceFlavors() {
            DishDTO dto = new DishDTO();
            dto.setId(1L);
            dto.setName("宫保鸡丁");
            dto.setCategoryId(1L);
            dto.setPrice(new BigDecimal("30.00"));
            dto.setFlavors(List.of(
                    DishFlavor.builder().name("辣度").value("麻辣").build()
            ));

            dishService.updateWithFlavor(dto);

            verify(dishFlavorMapper).deleteByDishId(1L);
            verify(dishMapper).update(any(Dish.class));
            verify(dishFlavorMapper).insertBatch(argThat(f -> f.size() == 1));
        }

        @Test
        @DisplayName("更新时 flavors 为空应只删不插")
        void shouldOnlyDeleteFlavorsWhenEmpty() {
            DishDTO dto = new DishDTO();
            dto.setId(1L);
            dto.setName("白米饭");
            dto.setFlavors(new ArrayList<>());

            dishService.updateWithFlavor(dto);

            verify(dishFlavorMapper).deleteByDishId(1L);
            verify(dishFlavorMapper, never()).insertBatch(any());
        }
    }

    // ============================================================
    // startOrStop — 启用/停用
    // ============================================================

    @Nested
    @DisplayName("启用/停用 startOrStop")
    class StartOrStopTest {

        /**
         * TC-DISH-501: 停售→起售
         */
        @Test
        @DisplayName("TC-DISH-501 停售→起售应更新状态为 1")
        void shouldEnableDish() {
            dishService.startOrStop(1, 1L);

            verify(dishMapper).update(argThat(d ->
                    d.getId() == 1L && d.getStatus() == 1));
            verify(setmealDishMapper, never()).getSetmealIdByDishIds(any());
        }

        /**
         * TC-DISH-502: 起售→停售, 关联套餐也应停售
         */
        @Test
        @DisplayName("TC-DISH-502 起售→停售应同时停售关联套餐")
        void shouldDisableDishAndRelatedSetmeals() {
            when(setmealDishMapper.getSetmealIdByDishIds(List.of(1L)))
                    .thenReturn(List.of(10L, 20L));

            dishService.startOrStop(0, 1L);

            verify(dishMapper).update(argThat(d ->
                    d.getId() == 1L && d.getStatus() == 0));
            verify(setmealMapper).update(argThat(s -> s.getId() == 10L && s.getStatus() == 0));
            verify(setmealMapper).update(argThat(s -> s.getId() == 20L && s.getStatus() == 0));
        }
    }

    // ============================================================
    // list — 根据分类 ID 查询
    // ============================================================

    @Nested
    @DisplayName("根据分类 ID 查询 list")
    class ListByCategoryTest {

        /**
         * TC-DISH-601: categoryId=1, 只返回起售菜品
         */
        @Test
        @DisplayName("TC-DISH-601 应按分类 ID 且只查起售菜品")
        void shouldQueryEnabledDishesByCategory() {
            List<Dish> dishes = List.of(
                    Dish.builder().id(1L).name("宫保鸡丁").status(1).build()
            );
            when(dishMapper.list(argThat(d ->
                    d.getCategoryId() == 1L && d.getStatus() == StatusConstant.ENABLE)))
                    .thenReturn(dishes);

            List<Dish> result = dishService.list(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(1);
        }

        /**
         * TC-DISH-602: 不存在 categoryId=99, 返回空列表
         */
        @Test
        @DisplayName("TC-DISH-602 不存在的分类应返回空列表")
        void shouldReturnEmptyListWhenNoDishes() {
            when(dishMapper.list(argThat(d -> d.getCategoryId() == 99L)))
                    .thenReturn(List.of());

            List<Dish> result = dishService.list(99L);

            assertThat(result).isEmpty();
        }
    }
}
