package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.exception.BaseException;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("分类管理 Controller 层测试")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @MockBean
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 绕过 JWT 鉴权拦截器，让所有请求直接到达 Controller
        when(jwtTokenAdminInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    // ============================================================
    // Task 1.1: 新增分类 POST /admin/category
    // ============================================================

    @Nested
    @DisplayName("Task 1.1 — 新增分类 POST /admin/category")
    class SaveTest {

        /**
         * TC-CAT-001 (等价类): 有效请求体 type=1, 名称"热销套餐", sort=1
         * 预期: 返回 success (code=1), DB 新增一条 type=1 的分类
         */
        @Test
        @DisplayName("TC-CAT-001 新增 type=1 的分类应成功")
        void shouldSaveCategoryWithType1() throws Exception {
            String body = """
                    {"type":1,"name":"热销套餐","sort":1}""";

            mockMvc.perform(post("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.msg").doesNotExist());

            verify(categoryService).save(argThat(dto ->
                    dto.getType() == 1 && "热销套餐".equals(dto.getName()) && dto.getSort() == 1));
        }

        /**
         * TC-CAT-002 (等价类): 有效请求体 type=2, 名称"超值套餐", sort=2
         * 预期: 返回 success (code=1), DB 新增一条 type=2 的分类
         */
        @Test
        @DisplayName("TC-CAT-002 新增 type=2 的分类应成功")
        void shouldSaveCategoryWithType2() throws Exception {
            String body = """
                    {"type":2,"name":"超值套餐","sort":2}""";

            mockMvc.perform(post("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(categoryService).save(argThat(dto ->
                    dto.getType() == 2 && "超值套餐".equals(dto.getName()) && dto.getSort() == 2));
        }

        /**
         * TC-CAT-003 (异常): 请求体 name 为 null 或空字符串
         * 预期: 返回参数校验失败 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-003 name 为空时应返回校验失败")
        void shouldFailWhenNameIsNull() throws Exception {
            String body = """
                    {"type":1,"sort":1}""";

            doThrow(new BaseException("分类名称不能为空"))
                    .when(categoryService).save(argThat(dto ->
                            dto.getName() == null || dto.getName().isEmpty()));

            mockMvc.perform(post("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }

        /**
         * TC-CAT-003 补充: name 为空字符串 ""
         */
        @Test
        @DisplayName("TC-CAT-003 补充 name 为空字符串时应返回校验失败")
        void shouldFailWhenNameIsEmpty() throws Exception {
            String body = """
                    {"type":1,"name":"","sort":1}""";

            doThrow(new BaseException("分类名称不能为空"))
                    .when(categoryService).save(argThat(dto ->
                            dto.getName() != null && dto.getName().isEmpty()));

            mockMvc.perform(post("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }

        /**
         * TC-CAT-004 (异常): DB 已存在 name="热销套餐", 再次发送同名请求
         * 预期: 返回业务异常 — 分类名称重复 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-004 分类名称重复时应返回业务异常")
        void shouldFailWhenNameAlreadyExists() throws Exception {
            String body = """
                    {"type":1,"name":"热销套餐","sort":1}""";

            doThrow(new BaseException("分类名称已存在"))
                    .when(categoryService).save(any(CategoryDTO.class));

            mockMvc.perform(post("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("分类名称已存在"));
        }
    }

    // ============================================================
    // Task 1.2: 分类分页查询 GET /admin/category/page
    // ============================================================

    @Nested
    @DisplayName("Task 1.2 — 分类分页查询 GET /admin/category/page")
    class PageQueryTest {

        /**
         * TC-CAT-101 (等价类): DB 有 15 条分类, 无条件筛选 page=1&pageSize=10
         * 预期: 返回第 1 页 10 条, total=15
         */
        @Test
        @DisplayName("TC-CAT-101 无条件分页应返回第一页 10 条, total=15")
        void shouldReturnFirstPage10Of15() throws Exception {
            List<Category> categories = buildCategoryList(10);
            PageResult<Category> pageResult = new PageResult<>(15L, categories);

            when(categoryService.pageQuery(any())).thenReturn(pageResult);

            mockMvc.perform(get("/admin/category/page")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(15))
                    .andExpect(jsonPath("$.data.records.length()").value(10));
        }

        /**
         * TC-CAT-102 (等价类): 用 name="套餐" 筛选
         * 预期: 返回过滤后结果, 所有 name 含"套餐"
         */
        @Test
        @DisplayName("TC-CAT-102 按名称筛选分页应返回过滤结果")
        void shouldReturnFilteredResultsWhenNameProvided() throws Exception {
            List<Category> filtered = List.of(
                    Category.builder().id(1L).name("热销套餐").type(1).sort(1).status(1).build(),
                    Category.builder().id(2L).name("超值套餐").type(2).sort(2).status(1).build()
            );
            PageResult<Category> pageResult = new PageResult<>(2L, filtered);

            when(categoryService.pageQuery(any())).thenReturn(pageResult);

            mockMvc.perform(get("/admin/category/page")
                            .param("page", "1")
                            .param("pageSize", "10")
                            .param("name", "套餐"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.records[0].name").value("热销套餐"))
                    .andExpect(jsonPath("$.data.records[1].name").value("超值套餐"));
        }

        /**
         * TC-CAT-103 (边界值): pageSize=1（最小 pageSize）
         * 预期: 返回 1 条, 分页正常
         */
        @Test
        @DisplayName("TC-CAT-103 pageSize=1 (最小) 应返回 1 条记录")
        void shouldReturnOneRecordWhenMinPageSize() throws Exception {
            List<Category> singleItem = List.of(
                    Category.builder().id(1L).name("热销套餐").type(1).sort(1).status(1).build()
            );
            PageResult<Category> pageResult = new PageResult<>(15L, singleItem);

            when(categoryService.pageQuery(any())).thenReturn(pageResult);

            mockMvc.perform(get("/admin/category/page")
                            .param("page", "1")
                            .param("pageSize", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(15))
                    .andExpect(jsonPath("$.data.records.length()").value(1));
        }

        /**
         * TC-CAT-104 (异常): page=0 (非法页码)
         * 预期: 返回参数校验失败 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-104 page=0 时应返回参数校验失败")
        void shouldFailWhenPageIsZero() throws Exception {
            doThrow(new BaseException("页码不能为0"))
                    .when(categoryService).pageQuery(argThat(dto ->
                            dto.getPage() == 0));

            mockMvc.perform(get("/admin/category/page")
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 1.3: 删除分类 DELETE /admin/category
    // ============================================================

    @Nested
    @DisplayName("Task 1.3 — 删除分类 DELETE /admin/category")
    class DeleteTest {

        /**
         * TC-CAT-201 (等价类): DB 存在 id=1 的分类, 无关联菜品/套餐
         * 预期: 返回 success, 记录被删除
         */
        @Test
        @DisplayName("TC-CAT-201 无关联数据时应删除成功")
        void shouldDeleteWhenNoRelatedData() throws Exception {
            mockMvc.perform(delete("/admin/category")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(categoryService).deleteById(1L);
        }

        /**
         * TC-CAT-202 (异常): DB 存在 id=2 的分类, 且有关联菜品
         * 预期: 返回业务异常 — 有关联数据不可删除 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-202 有关联菜品时删除应返回业务异常")
        void shouldFailWhenRelatedDishExists() throws Exception {
            doThrow(new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH))
                    .when(categoryService).deleteById(2L);

            mockMvc.perform(delete("/admin/category")
                            .param("id", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value(MessageConstant.CATEGORY_BE_RELATED_BY_DISH));
        }

        /**
         * TC-CAT-203 (异常): DB 不存在 id=99999
         * 预期: 返回业务异常 — 分类不存在 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-203 分类不存在时应返回业务异常")
        void shouldFailWhenCategoryNotFound() throws Exception {
            doThrow(new BaseException("分类不存在"))
                    .when(categoryService).deleteById(99999L);

            mockMvc.perform(delete("/admin/category")
                            .param("id", "99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 1.4: 修改分类 PUT /admin/category
    // ============================================================

    @Nested
    @DisplayName("Task 1.4 — 修改分类 PUT /admin/category")
    class UpdateTest {

        /**
         * TC-CAT-301 (等价类): DB 存在 id=1, name="原名称", 修改为"新名称"
         * 预期: 返回 success, name 更新为"新名称"
         */
        @Test
        @DisplayName("TC-CAT-301 有效修改应成功")
        void shouldUpdateSuccessfully() throws Exception {
            String body = """
                    {"id":1,"name":"新名称","type":1,"sort":1}""";

            mockMvc.perform(put("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(categoryService).update(argThat(dto ->
                    dto.getId() == 1L && "新名称".equals(dto.getName())));
        }

        /**
         * TC-CAT-302 (异常): PUT body 中 id 为 null
         * 预期: 返回业务异常 — id 必填 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-302 id 为 null 时应返回业务异常")
        void shouldFailWhenIdIsNull() throws Exception {
            String body = """
                    {"name":"测试","type":1}""";

            doThrow(new BaseException("分类id不能为空"))
                    .when(categoryService).update(argThat(dto ->
                            dto.getId() == null));

            mockMvc.perform(put("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }

        /**
         * TC-CAT-303 (异常): DB 不存在 id=99999
         * 预期: 返回业务异常 — 分类不存在 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-303 分类不存在时应返回业务异常")
        void shouldFailWhenCategoryNotFound() throws Exception {
            String body = """
                    {"id":99999,"name":"测试"}""";

            doThrow(new BaseException("分类不存在"))
                    .when(categoryService).update(argThat(dto ->
                            dto.getId() == 99999L));

            mockMvc.perform(put("/admin/category")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 1.5: 启用/禁用分类 POST /admin/category/status/{status}
    // ============================================================

    @Nested
    @DisplayName("Task 1.5 — 启用/禁用分类 POST /admin/category/status/{status}")
    class StartOrStopTest {

        /**
         * TC-CAT-401 (等价类): DB 存在 id=1, status=0 (禁用), 启用
         * 预期: 返回 success, status 更新为 1
         */
        @Test
        @DisplayName("TC-CAT-401 禁用→启用应成功")
        void shouldEnableCategory() throws Exception {
            mockMvc.perform(post("/admin/category/status/1")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(categoryService).startOrStop(1, 1L);
        }

        /**
         * TC-CAT-402 (等价类): DB 存在 id=1, status=1 (启用), 禁用
         * 预期: 返回 success, status 更新为 0
         */
        @Test
        @DisplayName("TC-CAT-402 启用→禁用应成功")
        void shouldDisableCategory() throws Exception {
            mockMvc.perform(post("/admin/category/status/0")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(categoryService).startOrStop(0, 1L);
        }

        /**
         * TC-CAT-403 (异常): status=2 (非法值)
         * 预期: 返回参数校验失败 (code=0)
         */
        @Test
        @DisplayName("TC-CAT-403 status=2 非法值时应返回校验失败")
        void shouldFailWhenInvalidStatus() throws Exception {
            doThrow(new BaseException("状态值非法"))
                    .when(categoryService).startOrStop(2, 1L);

            mockMvc.perform(post("/admin/category/status/2")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 1.6: 根据类型查询分类 GET /admin/category/list
    // ============================================================

    @Nested
    @DisplayName("Task 1.6 — 根据类型查询分类 GET /admin/category/list")
    class ListByTypeTest {

        /**
         * TC-CAT-501 (等价类): DB 有 type=1 的分类
         * 预期: 返回 type=1 的分类列表
         */
        @Test
        @DisplayName("TC-CAT-501 按 type=1 应返回对应分类列表")
        void shouldReturnType1Categories() throws Exception {
            List<Category> categories = List.of(
                    Category.builder().id(1L).name("热销套餐").type(1).sort(1).status(1).build(),
                    Category.builder().id(2L).name("限时特惠").type(1).sort(2).status(1).build()
            );
            when(categoryService.list(1)).thenReturn(categories);

            mockMvc.perform(get("/admin/category/list")
                            .param("type", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].type").value(1))
                    .andExpect(jsonPath("$.data[1].type").value(1));
        }

        /**
         * TC-CAT-502 (等价类): 不传 type
         * 预期: 返回全部分类列表
         */
        @Test
        @DisplayName("TC-CAT-502 不传 type 应返回全部分类")
        void shouldReturnAllCategoriesWhenNoType() throws Exception {
            List<Category> categories = buildCategoryList(3);
            when(categoryService.list(null)).thenReturn(categories);

            mockMvc.perform(get("/admin/category/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 构造指定数量的 Category 列表
     */
    private List<Category> buildCategoryList(int count) {
        List<Category> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(Category.builder()
                    .id((long) (i + 1))
                    .name("分类" + (i + 1))
                    .type(i % 2 == 0 ? 1 : 2)
                    .sort(i + 1)
                    .status(1)
                    .build());
        }
        return list;
    }
}
