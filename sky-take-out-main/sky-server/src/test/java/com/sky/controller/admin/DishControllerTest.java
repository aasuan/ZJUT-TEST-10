package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

@WebMvcTest(DishController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("菜品管理 Controller 层测试")
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DishService dishService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @MockBean
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(jwtTokenAdminInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());
    }

    // ============================================================
    // Task 3.1: 新增菜品 POST /admin/dish
    // ============================================================

    @Nested
    @DisplayName("Task 3.1 — 新增菜品 POST /admin/dish")
    class SaveTest {

        /**
         * TC-DISH-001 (等价类): 完整 DishDTO（含 name, categoryId, price, flavors）
         * 预期: success, 菜品+口味同时入库, Redis key dish_{categoryId} 清除
         */
        @Test
        @DisplayName("TC-DISH-001 完整菜品含口味应新增成功并清除 Redis 缓存")
        void shouldSaveDishWithFlavors() throws Exception {
            String body = """
                    {
                      "name": "宫保鸡丁",
                      "categoryId": 1,
                      "price": 28.00,
                      "image": "gongbao.jpg",
                      "description": "经典川菜",
                      "flavors": [
                        {"name": "辣度", "value": "微辣"},
                        {"name": "口味", "value": "咸鲜"}
                      ]
                    }""";

            mockMvc.perform(post("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).saveWithFlavor(argThat(dto ->
                    "宫保鸡丁".equals(dto.getName())
                            && dto.getCategoryId() == 1L
                            && dto.getPrice().compareTo(new BigDecimal("28.00")) == 0
                            && dto.getFlavors().size() == 2));
            verify(redisTemplate).keys("dish_1");
        }

        /**
         * TC-DISH-002 (等价类): DishDTO 不含 flavors (flavors=[])
         * 预期: success, 菜品创建, 口味表无关联记录
         */
        @Test
        @DisplayName("TC-DISH-002 无口味菜品应新增成功")
        void shouldSaveDishWithoutFlavors() throws Exception {
            String body = """
                    {
                      "name": "白米饭",
                      "categoryId": 2,
                      "price": 2.00,
                      "flavors": []
                    }""";

            mockMvc.perform(post("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).saveWithFlavor(argThat(dto ->
                    dto.getFlavors().isEmpty()));
        }

        /**
         * TC-DISH-003 (边界值): price=0.00
         * 预期: success（价格为 0 视为合法）
         */
        @Test
        @DisplayName("TC-DISH-003 价格为 0 应成功")
        void shouldSaveDishWithZeroPrice() throws Exception {
            String body = """
                    {
                      "name": "免费小菜",
                      "categoryId": 1,
                      "price": 0.00
                    }""";

            mockMvc.perform(post("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).saveWithFlavor(argThat(dto ->
                    dto.getPrice().compareTo(BigDecimal.ZERO) == 0));
        }

        /**
         * TC-DISH-004 (异常): name 为 null 或空
         * 预期: 返回参数校验失败 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-004 name 为空时应返回校验失败")
        void shouldFailWhenNameIsNull() throws Exception {
            String body = """
                    {"categoryId":1,"price":10.00}""";

            doThrow(new BaseException("菜品名称不能为空"))
                    .when(dishService).saveWithFlavor(argThat(dto ->
                            dto.getName() == null || dto.getName().isEmpty()));

            mockMvc.perform(post("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }

        /**
         * TC-DISH-005 (异常): price=-1.00
         * 预期: 返回参数校验失败 — 价格不能为负 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-005 价格为负时应返回校验失败")
        void shouldFailWhenPriceIsNegative() throws Exception {
            String body = """
                    {"name":"测试菜品","categoryId":1,"price":-1.00}""";

            doThrow(new BaseException("价格不能为负"))
                    .when(dishService).saveWithFlavor(argThat(dto ->
                            dto.getPrice().compareTo(BigDecimal.ZERO) < 0));

            mockMvc.perform(post("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 3.2: 分页查询菜品 GET /admin/dish/page
    // ============================================================

    @Nested
    @DisplayName("Task 3.2 — 分页查询菜品 GET /admin/dish/page")
    class PageQueryTest {

        /**
         * TC-DISH-101 (等价类): DB 有 20 条菜品, 无条件筛选
         * 预期: 返回第 1 页 10 条 DishVO, total=20
         */
        @Test
        @DisplayName("TC-DISH-101 无条件分页应返回第一页 10 条, total=20")
        void shouldReturnFirstPage10Of20() throws Exception {
            List<DishVO> dishes = buildDishVOList(10);
            PageResult<DishVO> pageResult = new PageResult<>(20L, dishes);

            when(dishService.queryPage(any())).thenReturn(pageResult);

            mockMvc.perform(get("/admin/dish/page")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(20))
                    .andExpect(jsonPath("$.data.records.length()").value(10));
        }

        /**
         * TC-DISH-102 (等价类): 按 name + categoryId + status 复合条件筛选
         * 预期: 返回同时满足条件的菜品
         */
        @Test
        @DisplayName("TC-DISH-102 复合条件筛选应返回过滤结果")
        void shouldReturnFilteredDishes() throws Exception {
            List<DishVO> filtered = List.of(
                    DishVO.builder().id(1L).name("宫保鸡丁").categoryId(1L)
                            .price(new BigDecimal("28.00")).status(1).build()
            );
            PageResult<DishVO> pageResult = new PageResult<>(1L, filtered);

            when(dishService.queryPage(any())).thenReturn(pageResult);

            mockMvc.perform(get("/admin/dish/page")
                            .param("page", "1")
                            .param("pageSize", "10")
                            .param("name", "鸡")
                            .param("categoryId", "1")
                            .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.records[0].name").value("宫保鸡丁"));
        }

        /**
         * TC-DISH-103 (边界值): pageSize=1（最小 pageSize）
         * 预期: 返回 1 条, 分页正常
         */
        @Test
        @DisplayName("TC-DISH-103 pageSize=1 (最小) 应返回 1 条记录")
        void shouldReturnOneRecordWhenMinPageSize() throws Exception {
            List<DishVO> singleItem = List.of(
                    DishVO.builder().id(1L).name("宫保鸡丁").categoryId(1L)
                            .price(new BigDecimal("28.00")).status(1).build()
            );
            PageResult<DishVO> pageResult = new PageResult<>(20L, singleItem);

            when(dishService.queryPage(any())).thenReturn(pageResult);

            mockMvc.perform(get("/admin/dish/page")
                            .param("page", "1")
                            .param("pageSize", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(20))
                    .andExpect(jsonPath("$.data.records.length()").value(1));
        }

        /**
         * TC-DISH-104 (异常): page=0 (非法页码)
         * 预期: 返回参数校验失败 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-104 page=0 时应返回校验失败")
        void shouldFailWhenPageIsZero() throws Exception {
            doThrow(new BaseException("页码不能为0"))
                    .when(dishService).queryPage(argThat(dto ->
                            dto.getPage() == 0));

            mockMvc.perform(get("/admin/dish/page")
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 3.3: 删除菜品 DELETE /admin/dish
    // ============================================================

    @Nested
    @DisplayName("Task 3.3 — 删除菜品 DELETE /admin/dish")
    class DeleteTest {

        /**
         * TC-DISH-201 (等价类): DB 存在菜品 id=1, 未关联起售套餐
         * 预期: success, 菜品删除, Redis dish_* 清除
         */
        @Test
        @DisplayName("TC-DISH-201 未关联起售套餐时应删除成功并清除 Redis")
        void shouldDeleteSingleDish() throws Exception {
            mockMvc.perform(delete("/admin/dish")
                            .param("ids", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).deleteBatch(List.of(1L));
            verify(redisTemplate).keys("dish_*");
        }

        /**
         * TC-DISH-202 (等价类): 批量删除 id=1,2,3
         * 预期: success, 3 个菜品全部删除
         */
        @Test
        @DisplayName("TC-DISH-202 批量删除应成功")
        void shouldDeleteMultipleDishes() throws Exception {
            mockMvc.perform(delete("/admin/dish")
                            .param("ids", "1", "2", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).deleteBatch(List.of(1L, 2L, 3L));
        }

        /**
         * TC-DISH-203 (异常): DB 存在菜品 id=5, 已关联起售套餐
         * 预期: 返回业务异常 — 关联起售套餐不可删除 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-203 关联起售套餐时删除应返回业务异常")
        void shouldFailWhenRelatedToEnabledSetmeal() throws Exception {
            doThrow(new BaseException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL))
                    .when(dishService).deleteBatch(List.of(5L));

            mockMvc.perform(delete("/admin/dish")
                            .param("ids", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value(MessageConstant.DISH_BE_RELATED_BY_SETMEAL));
        }
    }

    // ============================================================
    // Task 3.4: 根据 ID 查询菜品 GET /admin/dish/{id}
    // ============================================================

    @Nested
    @DisplayName("Task 3.4 — 根据 ID 查询菜品 GET /admin/dish/{id}")
    class GetByIdTest {

        /**
         * TC-DISH-301 (等价类): DB 菜品 id=1 有 2 条口味
         * 预期: 返回 DishVO, 含基本字段 + 2 条口味
         */
        @Test
        @DisplayName("TC-DISH-301 有口味的菜品应返回完整 DishVO")
        void shouldReturnDishWithFlavors() throws Exception {
            DishVO dishVO = DishVO.builder()
                    .id(1L).name("宫保鸡丁").categoryId(1L)
                    .price(new BigDecimal("28.00")).status(1)
                    .flavors(List.of(
                            DishFlavor.builder().name("辣度").value("微辣").build(),
                            DishFlavor.builder().name("口味").value("咸鲜").build()
                    ))
                    .build();

            when(dishService.getByIdWithFlavor(1L)).thenReturn(dishVO);

            mockMvc.perform(get("/admin/dish/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.name").value("宫保鸡丁"))
                    .andExpect(jsonPath("$.data.price").value(28.00))
                    .andExpect(jsonPath("$.data.flavors.length()").value(2))
                    .andExpect(jsonPath("$.data.flavors[0].name").value("辣度"))
                    .andExpect(jsonPath("$.data.flavors[1].name").value("口味"));
        }

        /**
         * TC-DISH-302 (等价类): DB 菜品 id=2 无口味
         * 预期: 返回 DishVO, flavors 为空列表
         */
        @Test
        @DisplayName("TC-DISH-302 无口味的菜品应返回空 flavors")
        void shouldReturnDishWithoutFlavors() throws Exception {
            DishVO dishVO = DishVO.builder()
                    .id(2L).name("白米饭").categoryId(2L)
                    .price(new BigDecimal("2.00")).status(1)
                    .flavors(List.of())
                    .build();

            when(dishService.getByIdWithFlavor(2L)).thenReturn(dishVO);

            mockMvc.perform(get("/admin/dish/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.flavors.length()").value(0));
        }

        /**
         * TC-DISH-303 (异常): DB 不存在 id=99999
         * 预期: 返回业务异常 — 菜品不存在 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-303 菜品不存在时应返回业务异常")
        void shouldFailWhenDishNotFound() throws Exception {
            when(dishService.getByIdWithFlavor(99999L))
                    .thenThrow(new BaseException("菜品不存在"));

            mockMvc.perform(get("/admin/dish/99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 3.5: 更新菜品 PUT /admin/dish
    // ============================================================

    @Nested
    @DisplayName("Task 3.5 — 更新菜品 PUT /admin/dish")
    class UpdateTest {

        /**
         * TC-DISH-401 (等价类): DB 菜品 id=1 原有 2 条口味, 替换为全新 1 条口味
         * 预期: success, 旧口味删除、新口味保存, Redis dish_* 清除
         */
        @Test
        @DisplayName("TC-DISH-401 替换口味应成功并清除 Redis")
        void shouldUpdateDishWithNewFlavors() throws Exception {
            String body = """
                    {
                      "id": 1,
                      "name": "宫保鸡丁",
                      "categoryId": 1,
                      "price": 30.00,
                      "flavors": [
                        {"name": "辣度", "value": "麻辣"}
                      ]
                    }""";

            mockMvc.perform(put("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).updateWithFlavor(argThat(dto ->
                    dto.getId() == 1L && dto.getFlavors().size() == 1));
            verify(redisTemplate).keys("dish_*");
        }

        /**
         * TC-DISH-402 (异常): PUT body 中 id 为 null
         * 预期: 返回业务异常 — id 必填 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-402 id 为 null 时应返回业务异常")
        void shouldFailWhenIdIsNull() throws Exception {
            String body = """
                    {"name":"测试菜品","price":10.00}""";

            doThrow(new BaseException("菜品id不能为空"))
                    .when(dishService).updateWithFlavor(argThat(dto ->
                            dto.getId() == null));

            mockMvc.perform(put("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }

        /**
         * TC-DISH-403 (异常): DB 不存在 id=99999
         * 预期: 返回业务异常 — 菜品不存在 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-403 菜品不存在时应返回业务异常")
        void shouldFailWhenDishNotFound() throws Exception {
            String body = """
                    {"id":99999,"name":"测试"}""";

            doThrow(new BaseException("菜品不存在"))
                    .when(dishService).updateWithFlavor(argThat(dto ->
                            dto.getId() == 99999L));

            mockMvc.perform(put("/admin/dish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 3.6: 启用/停用菜品 POST /admin/dish/status/{status}
    // ============================================================

    @Nested
    @DisplayName("Task 3.6 — 启用/停用菜品 POST /admin/dish/status/{status}")
    class StartOrStopTest {

        /**
         * TC-DISH-501 (等价类): DB 菜品 id=1, status=0 (停售), 启用
         * 预期: success, status 变为 1, Redis 缓存清除
         */
        @Test
        @DisplayName("TC-DISH-501 停售→起售应成功并清除 Redis")
        void shouldEnableDish() throws Exception {
            mockMvc.perform(post("/admin/dish/status/1")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).startOrStop(1, 1L);
            verify(redisTemplate).keys("dish_*");
        }

        /**
         * TC-DISH-502 (等价类): DB 菜品 id=1, status=1 (起售), 停售
         * 预期: success, status 变为 0, Redis 缓存清除
         */
        @Test
        @DisplayName("TC-DISH-502 起售→停售应成功并清除 Redis")
        void shouldDisableDish() throws Exception {
            mockMvc.perform(post("/admin/dish/status/0")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));

            verify(dishService).startOrStop(0, 1L);
            verify(redisTemplate).keys("dish_*");
        }

        /**
         * TC-DISH-503 (异常): status=2 (非法值)
         * 预期: 返回参数校验失败 (code=0)
         */
        @Test
        @DisplayName("TC-DISH-503 status=2 非法值时应返回校验失败")
        void shouldFailWhenInvalidStatus() throws Exception {
            doThrow(new BaseException("状态值非法"))
                    .when(dishService).startOrStop(2, 1L);

            mockMvc.perform(post("/admin/dish/status/2")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").isNotEmpty());
        }
    }

    // ============================================================
    // Task 3.7: 根据分类 ID 查询菜品 GET /admin/dish/list
    // ============================================================

    @Nested
    @DisplayName("Task 3.7 — 根据分类 ID 查询菜品 GET /admin/dish/list")
    class ListByCategoryTest {

        /**
         * TC-DISH-601 (等价类): DB 有 categoryId=1 的菜品
         * 预期: 返回该分类下全部菜品列表
         */
        @Test
        @DisplayName("TC-DISH-601 按分类 ID 应返回对应菜品列表")
        void shouldReturnDishesByCategoryId() throws Exception {
            List<Dish> dishes = List.of(
                    Dish.builder().id(1L).name("宫保鸡丁").categoryId(1L)
                            .price(new BigDecimal("28.00")).status(1).build(),
                    Dish.builder().id(2L).name("鱼香肉丝").categoryId(1L)
                            .price(new BigDecimal("32.00")).status(1).build()
            );
            when(dishService.list(1L)).thenReturn(dishes);

            mockMvc.perform(get("/admin/dish/list")
                            .param("categoryId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("宫保鸡丁"))
                    .andExpect(jsonPath("$.data[1].name").value("鱼香肉丝"));
        }

        /**
         * TC-DISH-602 (边界值): DB 不存在 categoryId=99
         * 预期: 返回空列表, 不报错
         */
        @Test
        @DisplayName("TC-DISH-602 不存在的分类 ID 应返回空列表")
        void shouldReturnEmptyListWhenCategoryNotFound() throws Exception {
            when(dishService.list(99L)).thenReturn(List.of());

            mockMvc.perform(get("/admin/dish/list")
                            .param("categoryId", "99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    private List<DishVO> buildDishVOList(int count) {
        List<DishVO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(DishVO.builder()
                    .id((long) (i + 1))
                    .name("菜品" + (i + 1))
                    .categoryId((long) (i % 3 + 1))
                    .price(new BigDecimal("20.00"))
                    .status(1)
                    .build());
        }
        return list;
    }
}
