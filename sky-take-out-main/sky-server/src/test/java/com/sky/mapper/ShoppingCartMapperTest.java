package com.sky.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.sky.entity.ShoppingCart;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("购物车Mapper测试")
class ShoppingCartMapperTest {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @BeforeEach
    void setUp() {
        shoppingCartMapper.deleteByUserId(1L);
        shoppingCartMapper.deleteByUserId(2L);
    }

    // ==================== 插入 ====================

    @Test
    @DisplayName("插入购物车 - 菜品")
    void testInsert_Dish() {
        ShoppingCart cart = ShoppingCart.builder()
                .name("宫保鸡丁").image("gongbao.jpg").userId(1L)
                .dishId(10L).number(1).amount(new BigDecimal("28.00"))
                .dishFlavor("微辣").createTime(LocalDateTime.now())
                .build();

        shoppingCartMapper.insert(cart);

        List<ShoppingCart> list = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).dishId(10L).build());
        assertEquals(1, list.size());
        assertEquals("宫保鸡丁", list.get(0).getName());
        assertEquals("微辣", list.get(0).getDishFlavor());
        assertEquals(1, list.get(0).getNumber());
        assertNotNull(list.get(0).getId());
    }

    @Test
    @DisplayName("插入购物车 - 套餐")
    void testInsert_Setmeal() {
        ShoppingCart cart = ShoppingCart.builder()
                .name("豪华套餐").image("setmeal.jpg").userId(1L)
                .setmealId(100L).number(1).amount(new BigDecimal("58.00"))
                .createTime(LocalDateTime.now())
                .build();

        shoppingCartMapper.insert(cart);

        List<ShoppingCart> list = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).setmealId(100L).build());
        assertEquals(1, list.size());
        assertEquals("豪华套餐", list.get(0).getName());
        assertNull(list.get(0).getDishId());
        assertEquals(100L, list.get(0).getSetmealId());
    }

    // ==================== 查询 ====================

    @Test
    @DisplayName("查询购物车 - 根据userId查询全部")
    void testList_ByUserId() {
        ShoppingCart cart1 = ShoppingCart.builder().name("菜1").userId(1L).dishId(1L)
                .number(1).amount(new BigDecimal("10.00")).createTime(LocalDateTime.now()).build();
        ShoppingCart cart2 = ShoppingCart.builder().name("菜2").userId(1L).dishId(2L)
                .number(2).amount(new BigDecimal("20.00")).createTime(LocalDateTime.now()).build();
        shoppingCartMapper.insert(cart1);
        shoppingCartMapper.insert(cart2);

        List<ShoppingCart> list = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("查询购物车 - 用户数据隔离验证")
    void testList_UserIsolation() {
        ShoppingCart cart1 = ShoppingCart.builder().name("用户1的菜").userId(1L).dishId(1L)
                .number(1).amount(new BigDecimal("10.00")).createTime(LocalDateTime.now()).build();
        ShoppingCart cart2 = ShoppingCart.builder().name("用户2的菜").userId(2L).dishId(1L)
                .number(1).amount(new BigDecimal("10.00")).createTime(LocalDateTime.now()).build();
        shoppingCartMapper.insert(cart1);
        shoppingCartMapper.insert(cart2);

        List<ShoppingCart> user1List = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(1, user1List.size());
        assertEquals("用户1的菜", user1List.get(0).getName());

        List<ShoppingCart> user2List = shoppingCartMapper.list(
                ShoppingCart.builder().userId(2L).build());
        assertEquals(1, user2List.size());
        assertEquals("用户2的菜", user2List.get(0).getName());
    }

    @Test
    @DisplayName("查询购物车 - 按dishId和dishFlavor精确查询")
    void testList_ByDishAndFlavor() {
        ShoppingCart cart1 = ShoppingCart.builder().name("宫保鸡丁").userId(1L).dishId(10L)
                .dishFlavor("微辣").number(1).amount(new BigDecimal("28.00"))
                .createTime(LocalDateTime.now()).build();
        ShoppingCart cart2 = ShoppingCart.builder().name("宫保鸡丁").userId(1L).dishId(10L)
                .dishFlavor("中辣").number(2).amount(new BigDecimal("28.00"))
                .createTime(LocalDateTime.now()).build();
        shoppingCartMapper.insert(cart1);
        shoppingCartMapper.insert(cart2);

        List<ShoppingCart> list = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).dishId(10L).dishFlavor("微辣").build());
        assertEquals(1, list.size());
        assertEquals("微辣", list.get(0).getDishFlavor());
    }

    // ==================== 更新 ====================

    @Test
    @DisplayName("更新购物车数量")
    void testUpdateNumberById() {
        ShoppingCart cart = ShoppingCart.builder().name("测试菜").userId(1L).dishId(1L)
                .number(1).amount(new BigDecimal("10.00")).createTime(LocalDateTime.now()).build();
        shoppingCartMapper.insert(cart);

        Long id = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).dishId(1L).build()).get(0).getId();
        ShoppingCart update = ShoppingCart.builder().id(id).number(5).build();
        shoppingCartMapper.updateNumberById(update);

        List<ShoppingCart> list = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(5, list.get(0).getNumber());
    }

    // ==================== 删除 ====================

    @Test
    @DisplayName("删除购物车 - 根据userId清空")
    void testDeleteByUserId() {
        ShoppingCart cart = ShoppingCart.builder().name("菜1").userId(1L).dishId(1L)
                .number(1).amount(new BigDecimal("10.00")).createTime(LocalDateTime.now()).build();
        shoppingCartMapper.insert(cart);

        List<ShoppingCart> before = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(1, before.size());

        shoppingCartMapper.deleteByUserId(1L);

        List<ShoppingCart> after = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(0, after.size());
    }

    @Test
    @DisplayName("删除购物车 - 根据id删除单条")
    void testDeleteById() {
        ShoppingCart cart = ShoppingCart.builder().name("菜1").userId(1L).dishId(1L)
                .number(1).amount(new BigDecimal("10.00")).createTime(LocalDateTime.now()).build();
        shoppingCartMapper.insert(cart);

        Long id = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build()).get(0).getId();

        shoppingCartMapper.deleteById(id);

        List<ShoppingCart> after = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(0, after.size());
    }

    // ==================== 批量插入 ====================

    @Test
    @DisplayName("批量插入 - 再来一单场景")
    void testInsertBatch() {
        List<ShoppingCart> cartList = new ArrayList<>();
        cartList.add(ShoppingCart.builder().name("宫保鸡丁").image("gongbao.jpg").userId(1L)
                .dishId(10L).dishFlavor("微辣").number(2).amount(new BigDecimal("28.00"))
                .createTime(LocalDateTime.now()).build());
        cartList.add(ShoppingCart.builder().name("可乐").image("cola.jpg").userId(1L)
                .dishId(20L).number(3).amount(new BigDecimal("5.00"))
                .createTime(LocalDateTime.now()).build());
        cartList.add(ShoppingCart.builder().name("豪华套餐").image("setmeal.jpg").userId(1L)
                .setmealId(100L).number(1).amount(new BigDecimal("58.00"))
                .createTime(LocalDateTime.now()).build());

        shoppingCartMapper.insertBatch(cartList);

        List<ShoppingCart> list = shoppingCartMapper.list(
                ShoppingCart.builder().userId(1L).build());
        assertEquals(3, list.size());
        assertTrue(list.stream().anyMatch(c -> "宫保鸡丁".equals(c.getName())));
        assertTrue(list.stream().anyMatch(c -> "可乐".equals(c.getName())));
        assertTrue(list.stream().anyMatch(c -> "豪华套餐".equals(c.getName())));
    }
}
