package com.sky.controller.admin;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.BaseIntegrationTest;
import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.entity.SetmealDish;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeeDTO;
import com.sky.service.EmployeeService;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 套餐集成测试
 * 测试场景：创建套餐（关联多个菜品）→ 停售其中一个菜品 → 尝试起售套餐 → 应抛异常
 */
class SetmealIntegrationTest extends BaseIntegrationTest {

    private String token;

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 先检查并创建管理员用户
        try {
            // 尝试创建用户，已存在则会抛出异常
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setName("管理员");
            employeeDTO.setUsername("admin");
            employeeDTO.setPhone("13800138000");
            employeeDTO.setSex("1");
            employeeDTO.setIdNumber("110101199001011234");
            employeeService.save(employeeDTO);
        } catch (Exception e) {
            // 用户已存在，忽略异常
        }

        // 2. 登录获取token
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        String response = mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取token
        JSONObject jsonObject = JSON.parseObject(response);
        token = jsonObject.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("套餐起售测试 - 包含未启售菜品时应抛异常")
    void testStartSetmealWithDisabledDish() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // ========== Step 1: 创建菜品 ==========
        // 创建菜品1 - 启售状态
        DishDTO dishDTO1 = new DishDTO();
        dishDTO1.setName("测试菜品1_" + timestamp);
        dishDTO1.setCategoryId(1L);
        dishDTO1.setPrice(new BigDecimal("20.00"));
        dishDTO1.setImage("http://localhost/media/dish1.jpg");
        dishDTO1.setDescription("测试菜品1描述");
        dishDTO1.setStatus(1); // 启售

        mockMvc.perform(post("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 创建菜品2 - 启售状态
        DishDTO dishDTO2 = new DishDTO();
        dishDTO2.setName("测试菜品2_" + timestamp);
        dishDTO2.setCategoryId(1L);
        dishDTO2.setPrice(new BigDecimal("30.00"));
        dishDTO2.setImage("http://localhost/media/dish2.jpg");
        dishDTO2.setDescription("测试菜品2描述");
        dishDTO2.setStatus(1); // 启售

        mockMvc.perform(post("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 查询刚创建的菜品获取真实ID（按名称搜索）
        String dishListResponse = mockMvc.perform(get("/admin/dish/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "测试菜品1_" + timestamp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject dishListJson1 = JSON.parseObject(dishListResponse);
        JSONArray records1 = dishListJson1.getJSONObject("data").getJSONArray("records");
        Long dishId1 = records1.getJSONObject(0).getLong("id");

        // 查询菜品2
        dishListResponse = mockMvc.perform(get("/admin/dish/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "测试菜品2_" + timestamp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject dishListJson2 = JSON.parseObject(dishListResponse);
        JSONArray records2 = dishListJson2.getJSONObject("data").getJSONArray("records");
        Long dishId2 = records2.getJSONObject(0).getLong("id");

        // ========== Step 2: 创建套餐 ==========
        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO.setName("测试套餐_" + timestamp);
        setmealDTO.setCategoryId(1L);
        setmealDTO.setPrice(new BigDecimal("50.00"));
        setmealDTO.setImage("http://localhost/media/setmeal.jpg");
        setmealDTO.setDescription("测试套餐描述");
        setmealDTO.setStatus(0); // 停售状态

        List<SetmealDish> setmealDishes = new ArrayList<>();
        SetmealDish dish1 = new SetmealDish();
        dish1.setDishId(dishId1);
        dish1.setName("测试菜品1_" + timestamp);
        dish1.setPrice(new BigDecimal("20.00"));
        dish1.setCopies(1);
        setmealDishes.add(dish1);

        SetmealDish dish2 = new SetmealDish();
        dish2.setDishId(dishId2);
        dish2.setName("测试菜品2_" + timestamp);
        dish2.setPrice(new BigDecimal("30.00"));
        dish2.setCopies(1);
        setmealDishes.add(dish2);

        setmealDTO.setSetmealDishes(setmealDishes);

        mockMvc.perform(post("/admin/setmeal")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 查询刚创建的套餐获取真实ID（按名称搜索）
        String setmealListResponse = mockMvc.perform(get("/admin/setmeal/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "测试套餐_" + timestamp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject setmealListJson = JSON.parseObject(setmealListResponse);
        JSONArray setmealRecords = setmealListJson.getJSONObject("data").getJSONArray("records");
        Long setmealId = setmealRecords.getJSONObject(0).getLong("id");

        // ========== Step 3: 停售其中一个菜品 ==========
        // 修改菜品2的状态为停售
        dishDTO2.setId(dishId2);
        dishDTO2.setStatus(0); // 停售

        mockMvc.perform(put("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // ========== Step 4: 尝试起售套餐 - 应抛出异常 ==========
        // 因为套餐中包含未启售的菜品，起售操作应该失败
        mockMvc.perform(post("/admin/setmeal/status/1")
                        .header("token", token)
                        .param("id", String.valueOf(setmealId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("套餐内包含未启售菜品，无法启售"));
    }

    @Test
    @DisplayName("套餐起售测试 - 所有菜品都启售时成功")
    void testStartSetmealWithAllEnabledDishes() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // ========== Step 1: 创建菜品（都为启售状态） ==========
        DishDTO dishDTO1 = new DishDTO();
        dishDTO1.setName("测试菜品A_" + timestamp);
        dishDTO1.setCategoryId(1L);
        dishDTO1.setPrice(new BigDecimal("25.00"));
        dishDTO1.setImage("http://localhost/media/dishA.jpg");
        dishDTO1.setDescription("测试菜品A描述");
        dishDTO1.setStatus(1); // 启售

        mockMvc.perform(post("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 查询刚创建的菜品获取真实ID（按名称搜索）
        String dishListResponse = mockMvc.perform(get("/admin/dish/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "测试菜品A_" + timestamp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject dishListJson = JSON.parseObject(dishListResponse);
        JSONArray records = dishListJson.getJSONObject("data").getJSONArray("records");
        Long dishId1 = records.getJSONObject(0).getLong("id");

        // ========== Step 2: 创建套餐 ==========
        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO.setName("正常套餐_" + timestamp);
        setmealDTO.setCategoryId(1L);
        setmealDTO.setPrice(new BigDecimal("25.00"));
        setmealDTO.setImage("http://localhost/media/setmeal-normal.jpg");
        setmealDTO.setDescription("正常套餐描述");
        setmealDTO.setStatus(0); // 停售状态

        List<SetmealDish> setmealDishes = new ArrayList<>();
        SetmealDish dish = new SetmealDish();
        dish.setDishId(dishId1);
        dish.setName("测试菜品A_" + timestamp);
        dish.setPrice(new BigDecimal("25.00"));
        dish.setCopies(1);
        setmealDishes.add(dish);

        setmealDTO.setSetmealDishes(setmealDishes);

        mockMvc.perform(post("/admin/setmeal")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 查询刚创建的套餐获取真实ID（按名称搜索）
        String setmealListResponse = mockMvc.perform(get("/admin/setmeal/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "正常套餐_" + timestamp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject setmealListJson = JSON.parseObject(setmealListResponse);
        JSONArray setmealRecords = setmealListJson.getJSONObject("data").getJSONArray("records");
        Long setmealId = setmealRecords.getJSONObject(0).getLong("id");

        // ========== Step 3: 起售套餐 - 应该成功 ==========
        mockMvc.perform(post("/admin/setmeal/status/1")
                        .header("token", token)
                        .param("id", String.valueOf(setmealId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
}