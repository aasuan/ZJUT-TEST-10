package com.sky.mapper;

import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.github.pagehelper.PageHelper;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ImportAutoConfiguration(PageHelperAutoConfiguration.class)
@Sql(scripts = {"/schema-h2.sql", "/data-category.sql"})
@DisplayName("CategoryMapper 测试")
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    // ============================================================
    // insert — 新增分类
    // ============================================================

    @Nested
    @DisplayName("新增分类 insert")
    class InsertTest {

        @Test
        @DisplayName("TC-CAT-001: 应成功插入 type=1 的分类")
        void shouldInsertCategoryType1() {
            Category category = Category.builder()
                    .type(1)
                    .name("测试分类")
                    .sort(1)
                    .status(0)
                    .build();

            categoryMapper.insert(category);

            // 验证插入后可通过分页查到
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(20);
            var page = categoryMapper.pageQuery(query);
            assertThat(page).anyMatch(c -> "测试分类".equals(c.getName()));
        }

        @Test
        @DisplayName("TC-CAT-002: 应成功插入 type=2 的分类")
        void shouldInsertCategoryType2() {
            Category category = Category.builder()
                    .type(2)
                    .name("套餐分类")
                    .sort(2)
                    .status(1)
                    .build();

            categoryMapper.insert(category);

            // list() 只查 status=1, 故直接查 pageQuery
            var query = new com.sky.dto.CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(20);
            var page = categoryMapper.pageQuery(query);
            assertThat(page.getResult())
                    .anyMatch(c -> c.getType() == 2 && "套餐分类".equals(c.getName()));
        }
    }

    // ============================================================
    // pageQuery — 分页查询（动态 <where> + <if>）
    // ============================================================

    @Nested
    @DisplayName("分页查询 pageQuery — 测试动态 SQL 拼接")
    class PageQueryTest {

        /**
         * TC-CAT-101: 无条件分页, total=15, 第一页 10 条
         */
        @Test
        @DisplayName("TC-CAT-101: 无条件分页应返回第一页 10 条且 total=15")
        void shouldReturnFirstPage10Of15() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);

            PageHelper.startPage(query.getPage(), query.getPageSize());
            var page = categoryMapper.pageQuery(query);

            assertThat(page.getTotal()).isEqualTo(15L);
            assertThat(page.getResult()).hasSize(10);
        }

        /**
         * TC-CAT-102: 按 name 筛选, 验证动态 <if> 生效
         */
        @Test
        @DisplayName("TC-CAT-102: 按 name='套餐' 筛选, 所有结果 name 应包含'套餐'")
        void shouldFilterByName() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setName("套餐");

            PageHelper.startPage(query.getPage(), query.getPageSize());
            var page = categoryMapper.pageQuery(query);

            assertThat(page.getResult()).isNotEmpty();
            assertThat(page.getResult()).allMatch(c -> c.getName().contains("套餐"));
        }

        /**
         * TC-CAT-103: pageSize=1 边界值
         */
        @Test
        @DisplayName("TC-CAT-103: pageSize=1 应只返回 1 条")
        void shouldReturnOneRecordWhenMinPageSize() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(1);

            PageHelper.startPage(query.getPage(), query.getPageSize());
            var page = categoryMapper.pageQuery(query);

            assertThat(page.getResult()).hasSize(1);
        }

        /**
         * 验证动态 SQL: type 筛选不生效时仅 name 筛选生效
         */
        @Test
        @DisplayName("仅传 name 时不会错误拼接 type 条件")
        void shouldNotAppendTypeConditionWhenNull() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(10);
            query.setName("套餐");
            // type 为 null, 不应拼接 AND type = null

            PageHelper.startPage(1, 10);
            var page = categoryMapper.pageQuery(query);

            // 应返回 name 含"套餐"的结果, 而非 0 条
            assertThat(page.getTotal()).isPositive();
        }

        /**
         * 验证按 sort asc 和 create_time desc 排序
         */
        @Test
        @DisplayName("结果应按 sort ASC 排序")
        void shouldOrderBySortAsc() {
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(15);

            PageHelper.startPage(1, 15);
            var page = categoryMapper.pageQuery(query);
            var records = page.getResult();

            for (int i = 1; i < records.size(); i++) {
                assertThat(records.get(i).getSort())
                        .isGreaterThanOrEqualTo(records.get(i - 1).getSort());
            }
        }
    }

    // ============================================================
    // deleteById — 删除分类
    // ============================================================

    @Nested
    @DisplayName("删除分类 deleteById")
    class DeleteByIdTest {

        @Test
        @DisplayName("TC-CAT-201: 应成功删除存在的分类")
        void shouldDeleteExistingCategory() {
            categoryMapper.deleteById(1L);

            // 分页总数应减少
            CategoryPageQueryDTO query = new CategoryPageQueryDTO();
            query.setPage(1);
            query.setPageSize(20);
            PageHelper.startPage(1, 20);
            var page = categoryMapper.pageQuery(query);
            assertThat(page.getTotal()).isEqualTo(14L);
        }

        @Test
        @DisplayName("删除不存在的 id 不应报错")
        void shouldNotThrowWhenIdNotFound() {
            categoryMapper.deleteById(99999L);
            // 无异常即为通过
        }
    }

    // ============================================================
    // update — 修改分类（动态 <set>）
    // ============================================================

    @Nested
    @DisplayName("修改分类 update — 测试动态 <set> SQL")
    class UpdateTest {

        /**
         * TC-CAT-301: 正常修改, 验证动态 <set> 正确拼接
         */
        @Test
        @DisplayName("TC-CAT-301: 更新 name 后应能查到新名称")
        void shouldUpdateCategoryName() {
            Category category = Category.builder()
                    .id(1L)
                    .name("新名称")
                    .type(1)
                    .sort(1)
                    .status(1)
                    .build();

            categoryMapper.update(category);

            // 通过 list 查询验证
            var list = categoryMapper.list(1);
            assertThat(list).anyMatch(c -> c.getId() == 1L && "新名称".equals(c.getName()));
        }

        /**
         * 验证动态 set: 仅更新 status, name 不变
         */
        @Test
        @DisplayName("动态 set: 仅传 status 时不应覆盖其他字段")
        void shouldOnlyUpdateNonNullFields() {
            Category category = Category.builder()
                    .id(1L)
                    .status(0)
                    .build();

            categoryMapper.update(category);

            // list() 只查 status=1, 已禁用的查不到 → 用 pageQuery 验证
            var pageQuery = new com.sky.dto.CategoryPageQueryDTO();
            pageQuery.setPage(1);
            pageQuery.setPageSize(20);
            var page = categoryMapper.pageQuery(pageQuery);
            var updated = page.getResult().stream()
                    .filter(c -> c.getId() == 1L).findFirst().orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(0);
            assertThat(updated.getName()).isEqualTo("热销套餐"); // name 未被置空
        }

        @Test
        @DisplayName("TC-CAT-401 & TC-CAT-402: 分别验证启用和禁用")
        void shouldToggleStatus() {
            // 禁用 (status=0) — list() 仅查 status=1, 故用 pageQuery
            categoryMapper.update(Category.builder().id(1L).status(0).build());
            var pq = new com.sky.dto.CategoryPageQueryDTO();
            pq.setPage(1);
            pq.setPageSize(20);
            var c = categoryMapper.pageQuery(pq).getResult().stream()
                    .filter(x -> x.getId() == 1L).findFirst().orElseThrow();
            assertThat(c.getStatus()).isEqualTo(0);

            // 启用 (status=1) — list() 可查到
            categoryMapper.update(Category.builder().id(1L).status(1).build());
            var list = categoryMapper.list(null);
            c = list.stream().filter(x -> x.getId() == 1L).findFirst().orElseThrow();
            assertThat(c.getStatus()).isEqualTo(1);
        }
    }

    // ============================================================
    // list — 根据类型查询
    // ============================================================

    @Nested
    @DisplayName("根据类型查询 list")
    class ListTest {

        /**
         * TC-CAT-501: 按 type=1 查询, 只返回启用(status=1)的分类
         */
        @Test
        @DisplayName("TC-CAT-501: 按 type=1 应只返回启用状态的对应类型")
        void shouldReturnEnabledCategoriesByType() {
            var result = categoryMapper.list(1);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(c -> c.getType() == 1 && c.getStatus() == 1);
        }

        /**
         * TC-CAT-502: 不传 type, 返回所有启用分类
         */
        @Test
        @DisplayName("TC-CAT-502: type 为 null 应返回所有启用分类")
        void shouldReturnAllEnabledCategoriesWhenTypeIsNull() {
            var result = categoryMapper.list(null);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(c -> c.getStatus() == 1);
            // 应包含 type=1 和 type=2 的启用分类
            assertThat(result).anyMatch(c -> c.getType() == 1);
            assertThat(result).anyMatch(c -> c.getType() == 2);
        }
    }
}
