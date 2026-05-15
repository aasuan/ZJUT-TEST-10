package com.sky.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("购物车服务层测试")
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1L);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 添加购物车 add() ====================

    @Test
    @DisplayName("添加购物车 - 新增单品")
    void testAdd_NewDish() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);

        Dish dish = Dish.builder()
                .id(10L).name("宫保鸡丁").image("gongbao.jpg")
                .price(new BigDecimal("28.00")).build();

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(dishMapper.getById(10L)).thenReturn(dish);

        shoppingCartService.add(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(dishMapper).getById(10L);
        verify(shoppingCartMapper).insert(any(ShoppingCart.class));
        verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("添加购物车 - 已存在单品数量累加")
    void testAdd_ExistingDish() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(1L).userId(1L).dishId(10L).name("宫保鸡丁")
                .number(2).amount(new BigDecimal("28.00")).build();
        List<ShoppingCart> list = new ArrayList<>();
        list.add(existing);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(list);

        shoppingCartService.add(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));
        verify(shoppingCartMapper, never()).insert(any(ShoppingCart.class));
        verify(dishMapper, never()).getById(any());
    }

    @Test
    @DisplayName("添加购物车 - 新增套餐")
    void testAdd_NewSetmeal() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setSetmealId(100L);

        Setmeal setmeal = Setmeal.builder()
                .id(100L).name("豪华套餐").image("setmeal.jpg")
                .price(new BigDecimal("58.00")).build();

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(setmealMapper.getById(100L)).thenReturn(setmeal);

        shoppingCartService.add(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(setmealMapper).getById(100L);
        verify(shoppingCartMapper).insert(any(ShoppingCart.class));
        verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("添加购物车 - 已存在套餐数量累加")
    void testAdd_ExistingSetmeal() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setSetmealId(100L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(2L).userId(1L).setmealId(100L).name("豪华套餐")
                .number(1).amount(new BigDecimal("58.00")).build();
        List<ShoppingCart> list = new ArrayList<>();
        list.add(existing);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(list);

        shoppingCartService.add(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));
        verify(setmealMapper, never()).getById(any());
    }

    @Test
    @DisplayName("添加购物车 - 带口味的新增单品")
    void testAdd_NewDishWithFlavor() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);
        dto.setDishFlavor("微辣");

        Dish dish = Dish.builder()
                .id(10L).name("宫保鸡丁").image("gongbao.jpg")
                .price(new BigDecimal("28.00")).build();

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(dishMapper.getById(10L)).thenReturn(dish);

        shoppingCartService.add(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(dishMapper).getById(10L);
        verify(shoppingCartMapper).insert(any(ShoppingCart.class));
    }

    // ==================== 查看购物车 showShoppingCart() ====================

    @Test
    @DisplayName("查看购物车 - 有数据")
    void testShowShoppingCart_HasItems() {
        List<ShoppingCart> expected = new ArrayList<>();
        expected.add(ShoppingCart.builder().id(1L).userId(1L).dishId(10L)
                .name("宫保鸡丁").number(2).amount(new BigDecimal("28.00")).build());
        expected.add(ShoppingCart.builder().id(2L).userId(1L).setmealId(100L)
                .name("豪华套餐").number(1).amount(new BigDecimal("58.00")).build());

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(expected);

        List<ShoppingCart> result = shoppingCartService.showShoppingCart();

        assertEquals(2, result.size());
        assertEquals("宫保鸡丁", result.get(0).getName());
        assertEquals("豪华套餐", result.get(1).getName());
        verify(shoppingCartMapper).list(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("查看购物车 - 空购物车")
    void testShowShoppingCart_Empty() {
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

        List<ShoppingCart> result = shoppingCartService.showShoppingCart();

        assertEquals(0, result.size());
        verify(shoppingCartMapper).list(any(ShoppingCart.class));
    }

    // ==================== 清空购物车 cleanShoppingCart() ====================

    @Test
    @DisplayName("清空购物车 - 成功")
    void testCleanShoppingCart() {
        shoppingCartService.cleanShoppingCart();

        verify(shoppingCartMapper).deleteByUserId(1L);
    }

    // ==================== 减少购物车 subShoppingCart() ====================

    @Test
    @DisplayName("减少购物车 - 数量大于1时减少数量")
    void testSub_DecreaseNumber() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(1L).userId(1L).dishId(10L).name("宫保鸡丁")
                .number(3).amount(new BigDecimal("28.00")).build();
        List<ShoppingCart> list = new ArrayList<>();
        list.add(existing);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(list);

        shoppingCartService.subShoppingCart(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));
        verify(shoppingCartMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("减少购物车 - 数量为1时删除该商品")
    void testSub_DeleteWhenNumberIsOne() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(1L).userId(1L).dishId(10L).name("宫保鸡丁")
                .number(1).amount(new BigDecimal("28.00")).build();
        List<ShoppingCart> list = new ArrayList<>();
        list.add(existing);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(list);

        shoppingCartService.subShoppingCart(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(shoppingCartMapper).deleteById(1L);
        verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("减少购物车 - 商品不在购物车中")
    void testSub_ItemNotFound() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(99L);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

        shoppingCartService.subShoppingCart(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class));
        verify(shoppingCartMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("减少购物车 - 减少套餐数量")
    void testSub_Setmeal() {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setSetmealId(100L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(2L).userId(1L).setmealId(100L).name("豪华套餐")
                .number(2).amount(new BigDecimal("58.00")).build();
        List<ShoppingCart> list = new ArrayList<>();
        list.add(existing);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(list);

        shoppingCartService.subShoppingCart(dto);

        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));
    }
}
