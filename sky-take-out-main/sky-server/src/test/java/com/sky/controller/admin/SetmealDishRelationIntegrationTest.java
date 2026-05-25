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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 套餐菜品关联修改集成测试
 * 测试场景：修改套餐的菜品关联 → 验证旧关联删除、新关联插入
 */
class SetmealDishRelationIntegrationTest extends BaseIntegrationTest {

    private String token;

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 先检查并创建管理员用户
        try {
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

        JSONObject jsonObject = JSON.parseObject(response);
        token = jsonObject.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("套餐菜品关联修改测试 - 验证旧关联删除、新关联插入")
    void testUpdateSetmealDishRelation() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // ========== Step 1: 创建三个菜品 ==========
        // 创建菜品1
        DishDTO dishDTO1 = new DishDTO();
        dishDTO1.setName("关联测试菜品1_" + timestamp);
        dishDTO1.setCategoryId(1L);
        dishDTO1.setPrice(new BigDecimal("10.00"));
        dishDTO1.setImage("http://localhost/media/dish1.jpg");
        dishDTO1.setDescription("测试菜品1");
        dishDTO1.setStatus(1);

        mockMvc.perform(post("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 创建菜品2
        DishDTO dishDTO2 = new DishDTO();
        dishDTO2.setName("关联测试菜品2_" + timestamp);
        dishDTO2.setCategoryId(1L);
        dishDTO2.setPrice(new BigDecimal("20.00"));
        dishDTO2.setImage("http://localhost/media/dish2.jpg");
        dishDTO2.setDescription("测试菜品2");
        dishDTO2.setStatus(1);

        mockMvc.perform(post("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 创建菜品3
        DishDTO dishDTO3 = new DishDTO();
        dishDTO3.setName("关联测试菜品3_" + timestamp);
        dishDTO3.setCategoryId(1L);
        dishDTO3.setPrice(new BigDecimal("30.00"));
        dishDTO3.setImage("http://localhost/media/dish3.jpg");
        dishDTO3.setDescription("测试菜品3");
        dishDTO3.setStatus(1);

        mockMvc.perform(post("/admin/dish")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dishDTO3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 获取菜品ID
        Long dishId1 = getDishIdByName("关联测试菜品1_" + timestamp);
        Long dishId2 = getDishIdByName("关联测试菜品2_" + timestamp);
        Long dishId3 = getDishIdByName("关联测试菜品3_" + timestamp);

        // ========== Step 2: 创建套餐，关联菜品1和菜品2 ==========
        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO.setName("关联测试套餐_" + timestamp);
        setmealDTO.setCategoryId(1L);
        setmealDTO.setPrice(new BigDecimal("30.00"));
        setmealDTO.setImage("http://localhost/media/setmeal.jpg");
        setmealDTO.setDescription("关联测试套餐");
        setmealDTO.setStatus(0);

        List<SetmealDish> setmealDishes = new ArrayList<>();
        SetmealDish dish1 = new SetmealDish();
        dish1.setDishId(dishId1);
        dish1.setName("关联测试菜品1_" + timestamp);
        dish1.setPrice(new BigDecimal("10.00"));
        dish1.setCopies(1);
        setmealDishes.add(dish1);

        SetmealDish dish2 = new SetmealDish();
        dish2.setDishId(dishId2);
        dish2.setName("关联测试菜品2_" + timestamp);
        dish2.setPrice(new BigDecimal("20.00"));
        dish2.setCopies(1);
        setmealDishes.add(dish2);

        setmealDTO.setSetmealDishes(setmealDishes);

        mockMvc.perform(post("/admin/setmeal")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 获取套餐ID
        Long setmealId = getSetmealIdByName("关联测试套餐_" + timestamp);

        // ========== Step 3: 验证初始关联（菜品1和菜品2） ==========
        verifySetmealDishes(setmealId, new Long[]{dishId1, dishId2});

        // ========== Step 4: 修改套餐，关联菜品2和菜品3 ==========
        SetmealDTO updateSetmealDTO = new SetmealDTO();
        updateSetmealDTO.setId(setmealId);
        updateSetmealDTO.setName("关联测试套餐_updated_" + timestamp);
        updateSetmealDTO.setCategoryId(1L);
        updateSetmealDTO.setPrice(new BigDecimal("50.00"));
        updateSetmealDTO.setImage("http://localhost/media/setmeal.jpg");
        updateSetmealDTO.setDescription("关联测试套餐（已修改）");
        updateSetmealDTO.setStatus(0);

        List<SetmealDish> updatedSetmealDishes = new ArrayList<>();
        SetmealDish updatedDish2 = new SetmealDish();
        updatedDish2.setDishId(dishId2);
        updatedDish2.setName("关联测试菜品2_" + timestamp);
        updatedDish2.setPrice(new BigDecimal("20.00"));
        updatedDish2.setCopies(1);
        updatedSetmealDishes.add(updatedDish2);

        SetmealDish updatedDish3 = new SetmealDish();
        updatedDish3.setDishId(dishId3);
        updatedDish3.setName("关联测试菜品3_" + timestamp);
        updatedDish3.setPrice(new BigDecimal("30.00"));
        updatedDish3.setCopies(1);
        updatedSetmealDishes.add(updatedDish3);

        updateSetmealDTO.setSetmealDishes(updatedSetmealDishes);

        mockMvc.perform(put("/admin/setmeal")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(updateSetmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // ========== Step 5: 验证新关联（菜品2和菜品3） ==========
        verifySetmealDishes(setmealId, new Long[]{dishId2, dishId3});
    }

    /**
     * 根据菜品名称获取菜品ID
     */
    private Long getDishIdByName(String name) throws Exception {
        String response = mockMvc.perform(get("/admin/dish/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject jsonObject = JSON.parseObject(response);
        JSONArray records = jsonObject.getJSONObject("data").getJSONArray("records");
        return records.getJSONObject(0).getLong("id");
    }

    /**
     * 根据套餐名称获取套餐ID
     */
    private Long getSetmealIdByName(String name) throws Exception {
        String response = mockMvc.perform(get("/admin/setmeal/page")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject jsonObject = JSON.parseObject(response);
        JSONArray records = jsonObject.getJSONObject("data").getJSONArray("records");
        return records.getJSONObject(0).getLong("id");
    }

    /**
     * 验证套餐菜品关联
     */
    private void verifySetmealDishes(Long setmealId, Long[] expectedDishIds) throws Exception {
        String response = mockMvc.perform(get("/admin/setmeal/" + setmealId)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject jsonObject = JSON.parseObject(response);
        JSONArray setmealDishes = jsonObject.getJSONObject("data").getJSONArray("setmealDishes");

        // 验证关联数量
        org.junit.jupiter.api.Assertions.assertEquals(expectedDishIds.length, setmealDishes.size());

        // 验证每个关联的菜品ID
        for (Long expectedDishId : expectedDishIds) {
            boolean found = false;
            for (int i = 0; i < setmealDishes.size(); i++) {
                Long actualDishId = setmealDishes.getJSONObject(i).getLong("dishId");
                if (actualDishId.equals(expectedDishId)) {
                    found = true;
                    break;
                }
            }
            org.junit.jupiter.api.Assertions.assertTrue(found, "未找到预期的菜品关联: dishId=" + expectedDishId);
        }
    }
}
