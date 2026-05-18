package com.sky.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sky.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sky.entity.Setmeal;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;

@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(SetmealController.class)
@DisplayName("C端套餐控制器测试")
class SetmealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SetmealService setmealService;

    @BeforeEach
    void setUp() {
        reset(setmealService);
    }

    @Test
    @DisplayName("查询套餐 - 按分类ID查询")
    void testList_ByCategoryId() throws Exception {
        List<Setmeal> setmeals = new ArrayList<>();
        setmeals.add(Setmeal.builder().id(1L).categoryId(1L).name("超值套餐")
                .price(new java.math.BigDecimal("39.90")).build());
        setmeals.add(Setmeal.builder().id(2L).categoryId(1L).name("豪华套餐")
                .price(new java.math.BigDecimal("58.00")).build());

        when(setmealService.list(any(Setmeal.class))).thenReturn(setmeals);

        mockMvc.perform(get("/user/setmeal/list").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("超值套餐"));

        verify(setmealService, times(1)).list(any(Setmeal.class));
    }

    @Test
    @DisplayName("查询套餐 - 空结果")
    void testList_Empty() throws Exception {
        when(setmealService.list(any(Setmeal.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/setmeal/list").param("categoryId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(setmealService, times(1)).list(any(Setmeal.class));
    }

    @Test
    @DisplayName("查询套餐菜品 - 根据套餐ID查询")
    void testDishList_Success() throws Exception {
        List<DishItemVO> dishItems = new ArrayList<>();
        dishItems.add(DishItemVO.builder().name("宫保鸡丁").copies(1)
                .image("gongbao.jpg").description("经典川菜").build());
        dishItems.add(DishItemVO.builder().name("可乐").copies(2)
                .image("cola.jpg").description("冰镇可乐").build());

        when(setmealService.getDishItemById(1L)).thenReturn(dishItems);

        mockMvc.perform(get("/user/setmeal/dish/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("宫保鸡丁"))
                .andExpect(jsonPath("$.data[0].copies").value(1))
                .andExpect(jsonPath("$.data[1].name").value("可乐"));

        verify(setmealService, times(1)).getDishItemById(1L);
    }

    @Test
    @DisplayName("查询套餐菜品 - 套餐无菜品")
    void testDishList_Empty() throws Exception {
        when(setmealService.getDishItemById(99L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/setmeal/dish/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(setmealService, times(1)).getDishItemById(99L);
    }
}
