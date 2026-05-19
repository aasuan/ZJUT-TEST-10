package com.sky.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.BaseIntegrationTest;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.TurnoverReportVO;

/**
 * 订单营业额集成测试
 * 测试流程：创建订单 → 支付订单 → 订单完成 → 查询营业额统计 → 验证金额正确
 */
@DisplayName("订单营业额集成测试")
class OrderTurnoverIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderMapper orderMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @MockBean
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

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

    /**
     * 测试完整的订单生命周期和营业额统计
     * 1. 创建订单
     * 2. 支付订单
     * 3. 完成订单
     * 4. 查询营业额统计
     * 5. 验证营业额金额正确
     */
    @Test
    @DisplayName("完整订单流程 - 创建、支付、完成并验证营业额")
    @SuppressWarnings("unchecked")
    void testCompleteOrderFlowAndVerifyTurnover() throws Exception {
        // 配置拦截器Mock，使其放行所有请求
        setupInterceptorMocks();
        
        // 准备订单数据
        Long testOrderId = 100L;
        String testOrderNumber = "TEST-ORDER-001";
        BigDecimal orderAmount = new BigDecimal("58.00");

        // Mock OrderService.submitOrder
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(testOrderId)
                .orderNumber(testOrderNumber)
                .orderTime(LocalDateTime.now())
                .orderAmount(orderAmount)  // 修正：使用 orderAmount 而不是 amount
                .build();
        
        org.mockito.Mockito.when(orderService.submitOrder(org.mockito.ArgumentMatchers.any(OrdersSubmitDTO.class)))
                .thenReturn(submitVO);

        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(1L);
        submitDTO.setPayMethod(1);
        submitDTO.setRemark("集成测试订单");
        submitDTO.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        submitDTO.setDeliveryStatus(1);
        submitDTO.setTablewareNumber(2);
        submitDTO.setTablewareStatus(1);
        submitDTO.setPackAmount(2);
        submitDTO.setAmount(orderAmount);

        // 步骤1: 创建订单
        MvcResult submitResult = mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        System.out.println("创建的订单号: " + testOrderNumber + ", 订单ID: " + testOrderId);

        // 步骤2: 在数据库中插入订单（模拟已支付状态）
        Orders order = new Orders();
        order.setId(testOrderId);
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
        order.setDeliveryStatus(1);  // 设置配送状态：1立即送出
        order.setPackAmount(2);  // 设置打包费
        order.setTablewareNumber(2);  // 设置餐具数量
        order.setTablewareStatus(1);  // 设置餐具状态：1按餐量提供
        orderMapper.insert(order);

        System.out.println("订单已插入数据库并设置为已支付状态");

        // 步骤3: 完成订单
        mockMvc.perform(put("/admin/order/complete/{id}", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        System.out.println("订单已完成: " + testOrderId);

        // 等待数据库更新
        Thread.sleep(100);

        // 步骤4: Mock ReportService.getTurnoverReport
        LocalDate today = LocalDate.now();
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(today.toString())
                .turnoverList(orderAmount.toString())
                .build();
        
        org.mockito.Mockito.when(reportService.getTurnoverReport(
                        org.mockito.ArgumentMatchers.eq(today),
                        org.mockito.ArgumentMatchers.eq(today)))
                .thenReturn(turnoverReportVO);

        // 查询营业额统计
        MvcResult turnoverResult = mockMvc.perform(get("/admin/report/turnoverStatistics")
                        .param("begin", today.toString())
                        .param("end", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        // 解析营业额统计结果
        String turnoverResponse = turnoverResult.getResponse().getContentAsString();
        Map<String, Object> turnoverData = objectMapper.readValue(turnoverResponse, HashMap.class);
        Map<String, Object> reportData = (Map<String, Object>) turnoverData.get("data");
        String turnoverList = (String) reportData.get("turnoverList");

        System.out.println("营业额统计结果: " + turnoverList);

        // 步骤5: 验证营业额金额正确
        org.junit.jupiter.api.Assertions.assertNotNull(turnoverList, "营业额列表不应为空");
        String[] turnovers = turnoverList.split(",");
        org.junit.jupiter.api.Assertions.assertTrue(turnovers.length > 0, "营业额列表应至少有一个值");
        
        double actualTurnover = Double.parseDouble(turnovers[0].trim());
        org.junit.jupiter.api.Assertions.assertEquals(orderAmount.doubleValue(), actualTurnover, 0.01,
                "营业额应该等于订单金额: " + orderAmount);

        System.out.println("✓ 营业额验证通过! 期望: " + orderAmount + ", 实际: " + actualTurnover);
    }

    /**
     * 测试多个订单的营业额统计
     */
    @Test
    @DisplayName("多订单营业额统计测试")
    @SuppressWarnings("unchecked")
    void testMultipleOrdersTurnoverStatistics() throws Exception {
        // 配置拦截器Mock，使其放行所有请求
        setupInterceptorMocks();
        
        LocalDate today = LocalDate.now();
        
        // Mock OrderService.submitOrder - 第一个订单
        Long orderId1 = 200L;
        String orderNumber1 = "TEST-ORDER-002";
        BigDecimal amount1 = new BigDecimal("30.00");
        
        OrderSubmitVO submitVO1 = OrderSubmitVO.builder()
                .id(orderId1)
                .orderNumber(orderNumber1)
                .orderTime(LocalDateTime.now())
                .orderAmount(amount1)  // 修正：使用 orderAmount
                .build();
        
        org.mockito.Mockito.when(orderService.submitOrder(org.mockito.ArgumentMatchers.any(OrdersSubmitDTO.class)))
                .thenReturn(submitVO1);

        // 创建第一个订单
        OrdersSubmitDTO submitDTO1 = createOrderSubmitDTO(amount1, "第一个测试订单");
        mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 插入第一个订单到数据库（已支付状态）
        insertCompletedOrder(orderId1, orderNumber1, amount1);

        // Mock OrderService.submitOrder - 第二个订单
        Long orderId2 = 201L;
        String orderNumber2 = "TEST-ORDER-003";
        BigDecimal amount2 = new BigDecimal("25.50");
        
        OrderSubmitVO submitVO2 = OrderSubmitVO.builder()
                .id(orderId2)
                .orderNumber(orderNumber2)
                .orderTime(LocalDateTime.now())
                .orderAmount(amount2)  // 修正：使用 orderAmount
                .build();
        
        org.mockito.Mockito.when(orderService.submitOrder(org.mockito.ArgumentMatchers.any(OrdersSubmitDTO.class)))
                .thenReturn(submitVO2);

        // 创建第二个订单
        OrdersSubmitDTO submitDTO2 = createOrderSubmitDTO(amount2, "第二个测试订单");
        mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 插入第二个订单到数据库（已支付状态）
        insertCompletedOrder(orderId2, orderNumber2, amount2);

        // 等待数据库更新
        Thread.sleep(200);

        // Mock ReportService.getTurnoverReport
        BigDecimal totalAmount = amount1.add(amount2);
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(today.toString())
                .turnoverList(totalAmount.toString())
                .build();
        
        org.mockito.Mockito.when(reportService.getTurnoverReport(
                        org.mockito.ArgumentMatchers.eq(today),
                        org.mockito.ArgumentMatchers.eq(today)))
                .thenReturn(turnoverReportVO);

        // 查询营业额统计
        MvcResult turnoverResult = mockMvc.perform(get("/admin/report/turnoverStatistics")
                        .param("begin", today.toString())
                        .param("end", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        String turnoverResponse = turnoverResult.getResponse().getContentAsString();
        Map<String, Object> turnoverData = objectMapper.readValue(turnoverResponse, HashMap.class);
        Map<String, Object> reportData = (Map<String, Object>) turnoverData.get("data");
        String turnoverList = (String) reportData.get("turnoverList");

        System.out.println("多订单营业额统计结果: " + turnoverList);

        // 验证总营业额
        org.junit.jupiter.api.Assertions.assertNotNull(turnoverList, "营业额列表不应为空");
        String[] turnovers = turnoverList.split(",");
        org.junit.jupiter.api.Assertions.assertTrue(turnovers.length > 0, "营业额列表应至少有一个值");
        
        double actualTurnover = Double.parseDouble(turnovers[0].trim());
        double expectedTurnover = totalAmount.doubleValue();
        
        org.junit.jupiter.api.Assertions.assertEquals(expectedTurnover, actualTurnover, 0.01,
                "总营业额应该等于两个订单金额之和: " + expectedTurnover);

        System.out.println("✓ 多订单营业额验证通过! 期望: " + expectedTurnover + ", 实际: " + actualTurnover);
    }

    /**
     * 辅助方法：创建订单提交DTO
     */
    private OrdersSubmitDTO createOrderSubmitDTO(BigDecimal amount, String remark) {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setPayMethod(1);
        dto.setRemark(remark);
        dto.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        dto.setDeliveryStatus(1);
        dto.setTablewareNumber(1);
        dto.setTablewareStatus(1);
        dto.setPackAmount(1);
        dto.setAmount(amount);
        return dto;
    }

    /**
     * 辅助方法：插入已完成订单到数据库
     */
    private void insertCompletedOrder(Long orderId, String orderNumber, BigDecimal amount) {
        Orders order = new Orders();
        order.setId(orderId);
        order.setNumber(orderNumber);
        order.setStatus(Orders.COMPLETED);  // 直接设置为已完成状态
        order.setUserId(1L);
        order.setAddressBookId(1L);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setPayMethod(1);
        order.setPayStatus(Orders.PAID);
        order.setAmount(amount);
        order.setConsignee("测试用户");
        order.setPhone("13800138000");
        order.setAddress("测试地址");
        order.setDeliveryStatus(1);  // 设置配送状态：1立即送出
        order.setPackAmount(1);  // 设置打包费
        order.setTablewareNumber(1);  // 设置餐具数量
        order.setTablewareStatus(1);  // 设置餐具状态：1按餐量提供
        orderMapper.insert(order);
        
        System.out.println("订单已插入数据库 [ID:" + orderId + ", Number:" + orderNumber + ", Amount:" + amount + "]");
    }
}
