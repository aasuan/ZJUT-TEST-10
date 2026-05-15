package com.sky.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.service.ShoppingCartService;

@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(ShoppingCartController.class)
@DisplayName("购物车控制器测试")
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(shoppingCartService);
    }

    // ==================== 添加购物车 POST /user/shoppingCart/add ====================

    @Test
    @DisplayName("添加购物车 - 正常添加菜品")
    void testAdd_Success() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);

        doNothing().when(shoppingCartService).add(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).add(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("添加购物车 - 添加套餐")
    void testAdd_WithSetmealId() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setSetmealId(100L);

        doNothing().when(shoppingCartService).add(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).add(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("添加购物车 - 带口味添加菜品")
    void testAdd_WithDishFlavor() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);
        dto.setDishFlavor("微辣");

        doNothing().when(shoppingCartService).add(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).add(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("添加购物车 - 空请求体")
    void testAdd_EmptyBody() throws Exception {
        doNothing().when(shoppingCartService).add(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).add(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("添加购物车 - 同时有菜品和套餐ID")
    void testAdd_WithBothDishAndSetmeal() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);
        dto.setSetmealId(100L);
        dto.setDishFlavor("中辣");

        doNothing().when(shoppingCartService).add(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).add(any(ShoppingCartDTO.class));
    }

    // ==================== 查看购物车 GET /user/shoppingCart/list ====================

    @Test
    @DisplayName("查看购物车 - 有数据")
    void testList_Success() throws Exception {
        ShoppingCart item = ShoppingCart.builder()
                .id(1L)
                .userId(1L)
                .dishId(10L)
                .name("宫保鸡丁")
                .number(2)
                .amount(new BigDecimal("28.00"))
                .image("gongbao.jpg")
                .createTime(LocalDateTime.now())
                .build();
        List<ShoppingCart> cartList = new ArrayList<>();
        cartList.add(item);

        when(shoppingCartService.showShoppingCart()).thenReturn(cartList);

        mockMvc.perform(get("/user/shoppingCart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("宫保鸡丁"))
                .andExpect(jsonPath("$.data[0].number").value(2));

        verify(shoppingCartService, times(1)).showShoppingCart();
    }

    @Test
    @DisplayName("查看购物车 - 空购物车")
    void testList_Empty() throws Exception {
        when(shoppingCartService.showShoppingCart()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/shoppingCart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(shoppingCartService, times(1)).showShoppingCart();
    }

    @Test
    @DisplayName("查看购物车 - 多条商品")
    void testList_MultipleItems() throws Exception {
        List<ShoppingCart> cartList = new ArrayList<>();
        cartList.add(ShoppingCart.builder().id(1L).userId(1L).dishId(10L).name("宫保鸡丁")
                .number(2).amount(new BigDecimal("28.00")).build());
        cartList.add(ShoppingCart.builder().id(2L).userId(1L).setmealId(100L).name("豪华套餐")
                .number(1).amount(new BigDecimal("58.00")).build());
        cartList.add(ShoppingCart.builder().id(3L).userId(1L).dishId(20L).name("可乐")
                .number(3).amount(new BigDecimal("5.00")).build());

        when(shoppingCartService.showShoppingCart()).thenReturn(cartList);

        mockMvc.perform(get("/user/shoppingCart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(3));

        verify(shoppingCartService, times(1)).showShoppingCart();
    }

    @Test
    @DisplayName("查看购物车 - 包含套餐和菜品的混合数据")
    void testList_MixedItems() throws Exception {
        List<ShoppingCart> cartList = new ArrayList<>();
        cartList.add(ShoppingCart.builder().id(1L).userId(1L).dishId(10L).dishFlavor("微辣")
                .name("宫保鸡丁").number(1).amount(new BigDecimal("28.00")).build());
        cartList.add(ShoppingCart.builder().id(2L).userId(1L).dishId(10L).dishFlavor("不辣")
                .name("宫保鸡丁").number(2).amount(new BigDecimal("28.00")).build());
        cartList.add(ShoppingCart.builder().id(3L).userId(1L).setmealId(100L).name("超值套餐")
                .number(1).amount(new BigDecimal("39.90")).build());

        when(shoppingCartService.showShoppingCart()).thenReturn(cartList);

        mockMvc.perform(get("/user/shoppingCart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].dishFlavor").value("微辣"))
                .andExpect(jsonPath("$.data[1].dishFlavor").value("不辣"))
                .andExpect(jsonPath("$.data[2].name").value("超值套餐"));

        verify(shoppingCartService, times(1)).showShoppingCart();
    }

    // ==================== 清空购物车 DELETE /user/shoppingCart/clean ====================

    @Test
    @DisplayName("清空购物车 - 成功")
    void testClean_Success() throws Exception {
        doNothing().when(shoppingCartService).cleanShoppingCart();

        mockMvc.perform(delete("/user/shoppingCart/clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).cleanShoppingCart();
    }

    // ==================== 减少购物车 POST /user/shoppingCart/sub ====================

    @Test
    @DisplayName("减少购物车 - 正常减少菜品数量")
    void testSub_Success() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);

        doNothing().when(shoppingCartService).subShoppingCart(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).subShoppingCart(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("减少购物车 - 减少套餐数量")
    void testSub_Setmeal() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setSetmealId(100L);

        doNothing().when(shoppingCartService).subShoppingCart(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).subShoppingCart(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("减少购物车 - 减少指定口味菜品数量")
    void testSub_WithFlavor() throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);
        dto.setDishFlavor("中辣");

        doNothing().when(shoppingCartService).subShoppingCart(any(ShoppingCartDTO.class));

        mockMvc.perform(post("/user/shoppingCart/sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(shoppingCartService, times(1)).subShoppingCart(any(ShoppingCartDTO.class));
    }
}
