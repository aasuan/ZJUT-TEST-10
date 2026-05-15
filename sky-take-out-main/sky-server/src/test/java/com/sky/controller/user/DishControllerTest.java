package com.sky.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.servlet.MockMvc;

import com.sky.service.DishService;
import com.sky.vo.DishVO;

@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(DishController.class)
@DisplayName("C端菜品控制器测试")
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DishService dishService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        reset(dishService);
        reset(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("查询菜品 - Redis命中缓存")
    @SuppressWarnings("unchecked")
    void testList_CacheHit() throws Exception {
        List<DishVO> cachedList = new ArrayList<>();
        cachedList.add(DishVO.builder().id(1L).name("宫保鸡丁").categoryId(1L)
                .price(new BigDecimal("28.00")).build());

        when(valueOperations.get("dish_1")).thenReturn(cachedList);

        mockMvc.perform(get("/user/dish/list").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("宫保鸡丁"));

        verify(dishService, never()).listWithFlavor(any());
    }

    @Test
    @DisplayName("查询菜品 - Redis未命中时查数据库")
    @SuppressWarnings("unchecked")
    void testList_CacheMiss() throws Exception {
        List<DishVO> dbList = new ArrayList<>();
        dbList.add(DishVO.builder().id(1L).name("宫保鸡丁").categoryId(1L)
                .price(new BigDecimal("28.00")).build());

        when(valueOperations.get("dish_1")).thenReturn(null);
        when(dishService.listWithFlavor(any())).thenReturn(dbList);

        mockMvc.perform(get("/user/dish/list").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("宫保鸡丁"));

        verify(dishService, times(1)).listWithFlavor(any());
        verify(valueOperations, times(1)).set(eq("dish_1"), any());
    }

    @Test
    @DisplayName("查询菜品 - 指定分类无菜品")
    @SuppressWarnings("unchecked")
    void testList_Empty() throws Exception {
        when(valueOperations.get("dish_999")).thenReturn(null);
        when(dishService.listWithFlavor(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/dish/list").param("categoryId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(dishService, times(1)).listWithFlavor(any());
    }
}
