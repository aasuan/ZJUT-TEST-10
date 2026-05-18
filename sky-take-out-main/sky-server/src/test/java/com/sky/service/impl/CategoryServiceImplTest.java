package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("分类管理 Service 层测试")
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private Page<Category> page;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ============================================================
    // save — 新增分类
    // ============================================================

    @Nested
    @DisplayName("新增分类 save")
    class SaveTest {

        /**
         * TC-CAT-001: type=1 的新增, 状态默认禁用, 插入成功
         */
        @Test
        @DisplayName("TC-CAT-001 新增 type=1 分类应默认设为禁用并插入")
        void shouldInsertCategoryWithDefaultDisable() {
            CategoryDTO dto = new CategoryDTO();
            dto.setType(1);
            dto.setName("热销套餐");
            dto.setSort(1);

            categoryService.save(dto);

            verify(categoryMapper).insert(argThat(c ->
                    c.getType() == 1
                            && "热销套餐".equals(c.getName())
                            && c.getSort() == 1
                            && c.getStatus() == StatusConstant.DISABLE));
        }

        /**
         * TC-CAT-002: type=2 的新增
         */
        @Test
        @DisplayName("TC-CAT-002 新增 type=2 分类应正确拷贝属性")
        void shouldCopyPropertiesAndInsert() {
            CategoryDTO dto = new CategoryDTO();
            dto.setType(2);
            dto.setName("超值套餐");
            dto.setSort(2);

            categoryService.save(dto);

            verify(categoryMapper).insert(argThat(c ->
                    c.getType() == 2 && "超值套餐".equals(c.getName())));
        }
    }

    // ============================================================
    // pageQuery — 分页查询
    // ============================================================

    @Nested
    @DisplayName("分页查询 pageQuery")
    class PageQueryTest {

        /**
         * TC-CAT-101: 无条件分页, 15 条总数, 返回 PageResult 包含 total 和列表
         */
        @Test
        @DisplayName("TC-CAT-101 分页应返回正确的 total 和列表")
        void shouldReturnPageResult() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);

            List<Category> records = List.of(
                    Category.builder().id(1L).name("分类1").build(),
                    Category.builder().id(2L).name("分类2").build()
            );
            when(page.getTotal()).thenReturn(15L);
            when(page.getResult()).thenReturn(records);
            when(categoryMapper.pageQuery(query)).thenReturn(page);

            PageResult<Category> result = categoryService.pageQuery(query);

            assertThat(result.getTotal()).isEqualTo(15L);
            assertThat(result.getRecords()).hasSize(2);
        }

        /**
         * TC-CAT-102: 按 name="套餐" 筛选, 返回过滤结果
         */
        @Test
        @DisplayName("TC-CAT-102 按名称筛选应传参给 Mapper")
        void shouldPassFilterToMapper() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setName("套餐");

            when(page.getTotal()).thenReturn(3L);
            when(page.getResult()).thenReturn(List.of());
            when(categoryMapper.pageQuery(query)).thenReturn(page);

            PageResult<Category> result = categoryService.pageQuery(query);

            assertThat(result.getTotal()).isEqualTo(3L);
            verify(categoryMapper).pageQuery(argThat(q -> "套餐".equals(q.getName())));
        }

        /**
         * TC-CAT-103: pageSize=1 边界值
         */
        @Test
        @DisplayName("TC-CAT-103 pageSize=1 应只返回 1 条")
        void shouldHandleMinPageSize() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(1);

            List<Category> single = List.of(
                    Category.builder().id(1L).name("分类1").build()
            );
            when(page.getTotal()).thenReturn(15L);
            when(page.getResult()).thenReturn(single);
            when(categoryMapper.pageQuery(query)).thenReturn(page);

            PageResult<Category> result = categoryService.pageQuery(query);

            assertThat(result.getRecords()).hasSize(1);
        }
    }

    // ============================================================
    // deleteById — 删除分类
    // ============================================================

    @Nested
    @DisplayName("删除分类 deleteById")
    class DeleteByIdTest {

        /**
         * TC-CAT-201: 无关联菜品/套餐, 删除成功
         */
        @Test
        @DisplayName("TC-CAT-201 无关联数据时应调用 Mapper 删除")
        void shouldDeleteWhenNoRelatedData() {
            when(dishMapper.countByCategoryId(1L)).thenReturn(0);
            when(setmealMapper.countByCategoryId(1L)).thenReturn(0);

            categoryService.deleteById(1L);

            verify(categoryMapper).deleteById(1L);
        }

        /**
         * TC-CAT-202: 有关联菜品 → 抛 DeletionNotAllowedException
         */
        @Test
        @DisplayName("TC-CAT-202 有关联菜品时应抛出业务异常")
        void shouldThrowWhenRelatedDishExists() {
            when(dishMapper.countByCategoryId(2L)).thenReturn(5);

            assertThatThrownBy(() -> categoryService.deleteById(2L))
                    .isInstanceOf(DeletionNotAllowedException.class)
                    .hasMessage(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);

            verify(categoryMapper, never()).deleteById(any());
        }

        /**
         * TC-CAT-202 补充: 有关联套餐 → 抛 DeletionNotAllowedException
         */
        @Test
        @DisplayName("TC-CAT-202 有关联套餐时应抛出业务异常")
        void shouldThrowWhenRelatedSetmealExists() {
            when(dishMapper.countByCategoryId(3L)).thenReturn(0);
            when(setmealMapper.countByCategoryId(3L)).thenReturn(3);

            assertThatThrownBy(() -> categoryService.deleteById(3L))
                    .isInstanceOf(DeletionNotAllowedException.class)
                    .hasMessage(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);

            verify(categoryMapper, never()).deleteById(any());
        }
    }

    // ============================================================
    // update — 修改分类
    // ============================================================

    @Nested
    @DisplayName("修改分类 update")
    class UpdateTest {

        /**
         * TC-CAT-301: 正常修改 name
         */
        @Test
        @DisplayName("TC-CAT-301 应拷贝属性并调用 Mapper 更新")
        void shouldUpdateCategory() {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(1L);
            dto.setName("新名称");
            dto.setType(1);
            dto.setSort(1);

            categoryService.update(dto);

            verify(categoryMapper).update(argThat(c ->
                    c.getId() == 1L && "新名称".equals(c.getName())));
        }
    }

    // ============================================================
    // startOrStop — 启用/禁用分类
    // ============================================================

    @Nested
    @DisplayName("启用/禁用分类 startOrStop")
    class StartOrStopTest {

        /**
         * TC-CAT-401: 禁用→启用
         */
        @Test
        @DisplayName("TC-CAT-401 应设置 status=1 并更新")
        void shouldEnableCategory() {
            categoryService.startOrStop(1, 1L);

            verify(categoryMapper).update(argThat(c ->
                    c.getId() == 1L && c.getStatus() == 1));
        }

        /**
         * TC-CAT-402: 启用→禁用
         */
        @Test
        @DisplayName("TC-CAT-402 应设置 status=0 并更新")
        void shouldDisableCategory() {
            categoryService.startOrStop(0, 1L);

            verify(categoryMapper).update(argThat(c ->
                    c.getId() == 1L && c.getStatus() == 0));
        }
    }

    // ============================================================
    // list — 根据类型查询
    // ============================================================

    @Nested
    @DisplayName("根据类型查询 list")
    class ListTest {

        /**
         * TC-CAT-501: 按 type=1 查询
         */
        @Test
        @DisplayName("TC-CAT-501 应按 type 传给 Mapper")
        void shouldQueryByType() {
            List<Category> categories = List.of(
                    Category.builder().id(1L).name("热销").type(1).build()
            );
            when(categoryMapper.list(1)).thenReturn(categories);

            List<Category> result = categoryService.list(1);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(1);
        }

        /**
         * TC-CAT-502: 不传 type, 查询全部
         */
        @Test
        @DisplayName("TC-CAT-502 type 为 null 应查询全部分类")
        void shouldQueryAllWhenTypeIsNull() {
            when(categoryMapper.list(null)).thenReturn(List.of());

            List<Category> result = categoryService.list(null);

            assertThat(result).isEmpty();
            verify(categoryMapper).list(null);
        }
    }
}
