package com.sky.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.sky.entity.OrderDetail;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("订单明细Mapper测试")
class OrderDetailMapperTest {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Test
    @DisplayName("批量插入订单明细")
    void testInsertBatch() {
        List<OrderDetail> details = new ArrayList<>();
        details.add(OrderDetail.builder().name("宫保鸡丁").image("gongbao.jpg")
                .orderId(1L).dishId(10L).dishFlavor("微辣")
                .number(2).amount(new BigDecimal("28.00")).build());
        details.add(OrderDetail.builder().name("可乐").image("cola.jpg")
                .orderId(1L).dishId(20L)
                .number(3).amount(new BigDecimal("5.00")).build());
        details.add(OrderDetail.builder().name("豪华套餐").image("setmeal.jpg")
                .orderId(1L).setmealId(100L)
                .number(1).amount(new BigDecimal("58.00")).build());

        orderDetailMapper.insertBatch(details);

        List<OrderDetail> result = orderDetailMapper.getByOrderId(1L);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(d -> "宫保鸡丁".equals(d.getName()) && d.getDishFlavor().equals("微辣")));
        assertTrue(result.stream().anyMatch(d -> "可乐".equals(d.getName())));
        assertTrue(result.stream().anyMatch(d -> "豪华套餐".equals(d.getName())));
    }

    @Test
    @DisplayName("根据订单ID查询订单明细")
    void testGetByOrderId() {
        List<OrderDetail> details = new ArrayList<>();
        details.add(OrderDetail.builder().name("宫保鸡丁").image("gongbao.jpg")
                .orderId(100L).dishId(10L).number(2)
                .amount(new BigDecimal("28.00")).build());

        orderDetailMapper.insertBatch(details);

        List<OrderDetail> result = orderDetailMapper.getByOrderId(100L);
        assertEquals(1, result.size());
        assertEquals("宫保鸡丁", result.get(0).getName());
        assertEquals(2, result.get(0).getNumber());
        assertNotNull(result.get(0).getId());
    }

    @Test
    @DisplayName("查询不存在的订单ID返回空列表")
    void testGetByOrderId_NotFound() {
        List<OrderDetail> result = orderDetailMapper.getByOrderId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("订单明细与订单关联验证")
    void testOrderAssociation() {
        // 订单1有2个明细项
        List<OrderDetail> details1 = new ArrayList<>();
        details1.add(OrderDetail.builder().name("宫保鸡丁").orderId(1L).dishId(10L)
                .number(1).amount(new BigDecimal("28.00")).build());
        details1.add(OrderDetail.builder().name("可乐").orderId(1L).dishId(20L)
                .number(2).amount(new BigDecimal("5.00")).build());
        orderDetailMapper.insertBatch(details1);

        // 订单2有1个明细项
        List<OrderDetail> details2 = new ArrayList<>();
        details2.add(OrderDetail.builder().name("豪华套餐").orderId(2L).setmealId(100L)
                .number(1).amount(new BigDecimal("58.00")).build());
        orderDetailMapper.insertBatch(details2);

        // 验证订单1的明细
        List<OrderDetail> order1Details = orderDetailMapper.getByOrderId(1L);
        assertEquals(2, order1Details.size());

        // 验证订单2的明细
        List<OrderDetail> order2Details = orderDetailMapper.getByOrderId(2L);
        assertEquals(1, order2Details.size());
        assertEquals("豪华套餐", order2Details.get(0).getName());

        // 验证订单3无明细
        List<OrderDetail> order3Details = orderDetailMapper.getByOrderId(3L);
        assertEquals(0, order3Details.size());
    }
}
