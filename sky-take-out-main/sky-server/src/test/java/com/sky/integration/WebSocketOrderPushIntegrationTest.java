package com.sky.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.BaseIntegrationTest;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;

/**
 * WebSocket订单推送集成测试
 * 测试流程：下单 → 催单 → 验证WebSocket推送逻辑被调用
 * 由于WebSocket连接的复杂性，我们主要测试催单功能是否正确调用了WebSocket推送
 */
@DisplayName("WebSocket订单推送集成测试")
class WebSocketOrderPushIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;  // 使用真实的OrderService，不Mock业务逻辑

    @MockBean
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @MockBean
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @SpyBean
    private WebSocketServer webSocketServer;

    /**
     * 初始化：配置拦截器Mock，使其总是放行
     */
    void setupInterceptorMocks() throws Exception {
        // Mock用户拦截器，总是返回true（放行）
        org.mockito.Mockito.when(jwtTokenUserInterceptor.preHandle(
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class),
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletResponse.class),
                org.mockito.ArgumentMatchers.any(Object.class)))
                .thenReturn(true);

        // Mock管理员拦截器，总是返回true（放行）
        org.mockito.Mockito.when(jwtTokenAdminInterceptor.preHandle(
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class),
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletResponse.class),
                org.mockito.ArgumentMatchers.any(Object.class)))
                .thenReturn(true);
    }

    @BeforeEach
    void setUp() throws Exception {
        setupInterceptorMocks();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据，避免影响其他测试
        try {
            // 注意：由于使用自增ID，我们无法预知具体ID，这里只清理已知的测试订单号
            // 实际项目中应该使用事务回滚或专门的测试数据清理策略
            System.out.println("测试结束（数据清理依赖数据库配置）");
        } catch (Exception e) {
            System.err.println("清理测试数据时出错: " + e.getMessage());
        }
    }

    /**
     * 测试完整的订单催单流程，验证WebSocket推送逻辑
     * 1. 在数据库中插入订单
     * 2. 催单操作
     * 3. 验证WebSocket推送方法被调用
     */
    @Test
    @DisplayName("WebSocket订单推送 - 下单并催单验证推送逻辑")
    void testWebSocketOrderPushWithReminder() throws Exception {
        // 准备订单数据
        String testOrderNumber = "TEST-ORDER-WS-001";
        BigDecimal orderAmount = new BigDecimal("68.00");

        // 步骤1: 在数据库中插入订单（模拟已支付状态）
        Orders order = new Orders();
        order.setNumber(testOrderNumber);
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setUserId(1L);
        order.setAddressBookId(1L);
        order.setOrderTime(LocalDateTime.now());
        order.setPayMethod(1);
        order.setPayStatus(Orders.PAID);
        order.setAmount(orderAmount);
        order.setConsignee("测试用户");
        order.setPhone("13800138000");
        order.setAddress("测试地址");
        order.setDeliveryStatus(1);
        order.setPackAmount(2);
        order.setTablewareNumber(2);
        order.setTablewareStatus(1);
        
        orderMapper.insert(order);
        // 获取数据库生成的真实ID
        Long testOrderId = order.getId();
        System.out.println("订单已插入数据库，ID: " + testOrderId);

        // 步骤2: 执行催单操作（这会触发WebSocket推送）
        MvcResult reminderResult = mockMvc.perform(get("/user/order/reminder/{id}", testOrderId))
                .andExpect(status().isOk())
                .andReturn();
        
        // 打印响应内容以便调试
        String responseContent = reminderResult.getResponse().getContentAsString();
        System.out.println("催单接口响应: " + responseContent);

        // 验证返回码为1（成功）
        org.junit.jupiter.api.Assertions.assertTrue(
                responseContent.contains("\"code\":1") || responseContent.contains("\"code\": 1"),
                "催单应该成功，但实际响应: " + responseContent
        );

        System.out.println("已执行催单操作");

        // 步骤3: 验证WebSocket推送方法被调用
        org.mockito.Mockito.verify(webSocketServer).sendToAllClient(org.mockito.ArgumentMatchers.anyString());
        
        System.out.println("✓ WebSocket推送验证成功! sendToAllClient方法已被调用");
    }

    /**
     * 测试催单时订单不存在的情况
     */
    @Test
    @DisplayName("催单异常处理 - 订单不存在")
    void testReminderWithNonExistentOrder() throws Exception {
        Long nonExistentOrderId = 999L;

        // 尝试对不存在的订单进行催单
        MvcResult result = mockMvc.perform(get("/user/order/reminder/{id}", nonExistentOrderId))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("订单不存在时的催单响应: " + response);
        
        // 验证返回码为0（失败）
        org.junit.jupiter.api.Assertions.assertTrue(
                response.contains("\"code\":0") || response.contains("\"code\": 0"),
                "订单不存在时应该返回错误，但实际响应: " + response
        );

        System.out.println("✓ 催单异常处理验证成功!");
    }

    /**
     * 测试多个订单的催单操作
     */
    @Test
    @DisplayName("多订单催单测试")
    void testMultipleOrderReminders() throws Exception {
        // 创建第一个订单
        String orderNumber1 = "TEST-ORDER-WS-002";
        BigDecimal amount1 = new BigDecimal("55.00");
        Long orderId1 = insertOrder(orderNumber1, amount1);

        // 创建第二个订单
        String orderNumber2 = "TEST-ORDER-WS-003";
        BigDecimal amount2 = new BigDecimal("62.50");
        Long orderId2 = insertOrder(orderNumber2, amount2);

        // 对第一个订单催单
        MvcResult reminder1 = mockMvc.perform(get("/user/order/reminder/{id}", orderId1))
                .andExpect(status().isOk())
                .andReturn();
        
        String response1 = reminder1.getResponse().getContentAsString();
        System.out.println("第一个订单催单响应: " + response1);
        org.junit.jupiter.api.Assertions.assertTrue(
                response1.contains("\"code\":1") || response1.contains("\"code\": 1"),
                "第一个订单催单应该成功，但实际响应: " + response1
        );

        // 对第二个订单催单
        MvcResult reminder2 = mockMvc.perform(get("/user/order/reminder/{id}", orderId2))
                .andExpect(status().isOk())
                .andReturn();
        
        String response2 = reminder2.getResponse().getContentAsString();
        System.out.println("第二个订单催单响应: " + response2);
        org.junit.jupiter.api.Assertions.assertTrue(
                response2.contains("\"code\":1") || response2.contains("\"code\": 1"),
                "第二个订单催单应该成功，但实际响应: " + response2
        );

        // 验证WebSocket推送方法被调用了两次
        org.mockito.Mockito.verify(webSocketServer, org.mockito.Mockito.times(2)).sendToAllClient(org.mockito.ArgumentMatchers.anyString());
        
        System.out.println("✓ 多订单催单测试验证成功! WebSocket推送被调用了2次");
    }

    /**
     * 辅助方法：插入订单到数据库
     */
    private Long insertOrder(String orderNumber, BigDecimal amount) {
        Orders order = new Orders();
        // 不设置ID，让数据库自动生成
        order.setNumber(orderNumber);
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setUserId(1L);
        order.setAddressBookId(1L);
        order.setOrderTime(LocalDateTime.now());
        order.setPayMethod(1);
        order.setPayStatus(Orders.PAID);
        order.setAmount(amount);
        order.setConsignee("测试用户");
        order.setPhone("13800138000");
        order.setAddress("测试地址");
        order.setDeliveryStatus(1);
        order.setPackAmount(1);
        order.setTablewareNumber(1);
        order.setTablewareStatus(1);
        
        orderMapper.insert(order);
        
        // 返回数据库生成的真实ID
        Long generatedId = order.getId();
        System.out.println("订单已插入数据库 [ID:" + generatedId + ", Number:" + orderNumber + "]");
        return generatedId;
    }
}