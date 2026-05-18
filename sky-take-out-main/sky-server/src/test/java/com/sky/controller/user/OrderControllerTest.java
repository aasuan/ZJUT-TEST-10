package com.sky.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(OrderController.class)
@DisplayName("用户端订单控制器测试")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(orderService);
    }

    // ==================== 用户下单 POST /user/order/submit ====================

    @Test
    @DisplayName("用户下单 - 成功")
    void testSubmit_Success() throws Exception {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setPayMethod(1);
        dto.setAmount(new BigDecimal("58.00"));
        dto.setRemark("少放辣");
        dto.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        dto.setDeliveryStatus(1);
        dto.setTablewareNumber(2);
        dto.setTablewareStatus(1);
        dto.setPackAmount(2);

        OrderSubmitVO vo = OrderSubmitVO.builder()
                .id(100L)
                .orderNumber("20240101000001")
                .orderAmount(new BigDecimal("58.00"))
                .orderTime(LocalDateTime.now())
                .build();

        when(orderService.submitOrder(any(OrdersSubmitDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.orderNumber").value("20240101000001"))
                .andExpect(jsonPath("$.data.orderAmount").value(58.00));

        verify(orderService, times(1)).submitOrder(any(OrdersSubmitDTO.class));
    }

    @Test
    @DisplayName("用户下单 - 仅必填字段")
    void testSubmit_MinimalFields() throws Exception {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setPayMethod(1);
        dto.setAmount(new BigDecimal("28.00"));

        OrderSubmitVO vo = OrderSubmitVO.builder()
                .id(101L)
                .orderNumber("20240101000002")
                .orderAmount(new BigDecimal("28.00"))
                .orderTime(LocalDateTime.now())
                .build();

        when(orderService.submitOrder(any(OrdersSubmitDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(101));

        verify(orderService, times(1)).submitOrder(any(OrdersSubmitDTO.class));
    }

    // ==================== 订单支付 PUT /user/order/payment ====================

    @Test
    @DisplayName("订单支付 - 成功")
    void testPayment_Success() throws Exception {
        OrdersPaymentDTO dto = new OrdersPaymentDTO();
        dto.setOrderNumber("20240101000001");
        dto.setPayMethod(1);

        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("test_nonce")
                .paySign("test_sign")
                .timeStamp("1234567890")
                .signType("RSA")
                .packageStr("prepay_id=test")
                .build();

        when(orderService.payment(any(OrdersPaymentDTO.class))).thenReturn(vo);

        mockMvc.perform(put("/user/order/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.nonceStr").value("test_nonce"))
                .andExpect(jsonPath("$.data.paySign").value("test_sign"))
                .andExpect(jsonPath("$.data.timeStamp").value("1234567890"));

        verify(orderService, times(1)).payment(any(OrdersPaymentDTO.class));
    }

    @Test
    @DisplayName("订单支付 - 返回null (模拟支付)")
    void testPayment_ReturnsNull() throws Exception {
        OrdersPaymentDTO dto = new OrdersPaymentDTO();
        dto.setOrderNumber("20240101000001");
        dto.setPayMethod(1);

        when(orderService.payment(any(OrdersPaymentDTO.class))).thenReturn(null);

        mockMvc.perform(put("/user/order/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService, times(1)).payment(any(OrdersPaymentDTO.class));
    }

    // ==================== 查询历史订单 GET /user/order/historyOrders ====================

    @Test
    @DisplayName("查询历史订单 - 有数据")
    void testHistoryOrders_Success() throws Exception {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(1L);
        orderVO.setNumber("20240101000001");
        orderVO.setStatus(Orders.COMPLETED);
        orderVO.setAmount(new BigDecimal("58.00"));

        PageResult<OrderVO> pageResult = new PageResult<>(1L, Collections.singletonList(orderVO));

        when(orderService.pageQueryByUser(eq(1), eq(10), isNull())).thenReturn(pageResult);

        mockMvc.perform(get("/user/order/historyOrders")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].number").value("20240101000001"));

        verify(orderService, times(1)).pageQueryByUser(1, 10, null);
    }

    @Test
    @DisplayName("查询历史订单 - 按状态筛选")
    void testHistoryOrders_WithStatus() throws Exception {
        PageResult<OrderVO> pageResult = new PageResult<>(0L, Collections.emptyList());

        when(orderService.pageQueryByUser(eq(1), eq(10), eq(Orders.PENDING_PAYMENT)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/user/order/historyOrders")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", String.valueOf(Orders.PENDING_PAYMENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(0));

        verify(orderService, times(1)).pageQueryByUser(1, 10, Orders.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("查询历史订单 - 空结果")
    void testHistoryOrders_Empty() throws Exception {
        PageResult<OrderVO> pageResult = new PageResult<>(0L, Collections.emptyList());

        when(orderService.pageQueryByUser(anyInt(), anyInt(), any())).thenReturn(pageResult);

        mockMvc.perform(get("/user/order/historyOrders")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(orderService, times(1)).pageQueryByUser(anyInt(), anyInt(), any());
    }

    // ==================== 查询订单详情 GET /user/order/orderDetail/{id} ====================

    @Test
    @DisplayName("查询订单详情 - 成功")
    void testOrderDetail_Success() throws Exception {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(1L);
        orderVO.setNumber("20240101000001");
        orderVO.setStatus(Orders.TO_BE_CONFIRMED);
        orderVO.setAmount(new BigDecimal("58.00"));

        List<OrderDetail> details = new ArrayList<>();
        OrderDetail detail = OrderDetail.builder()
                .id(1L).orderId(1L).name("宫保鸡丁").number(2)
                .amount(new BigDecimal("28.00")).build();
        details.add(detail);
        orderVO.setOrderDetailList(details);

        when(orderService.details(1L)).thenReturn(orderVO);

        mockMvc.perform(get("/user/order/orderDetail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.number").value("20240101000001"))
                .andExpect(jsonPath("$.data.orderDetailList.length()").value(1))
                .andExpect(jsonPath("$.data.orderDetailList[0].name").value("宫保鸡丁"));

        verify(orderService, times(1)).details(1L);
    }

    // ==================== 用户取消订单 PUT /user/order/cancel/{id} ====================

    @Test
    @DisplayName("用户取消订单 - 成功")
    void testCancel_Success() throws Exception {
        doNothing().when(orderService).userCancelById(1L);

        mockMvc.perform(put("/user/order/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService, times(1)).userCancelById(1L);
    }

    // ==================== 再来一单 POST /user/order/repetition/{id} ====================

    @Test
    @DisplayName("再来一单 - 成功")
    void testRepetition_Success() throws Exception {
        doNothing().when(orderService).repetition(1L);

        mockMvc.perform(post("/user/order/repetition/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService, times(1)).repetition(1L);
    }

    // ==================== 用户催单 GET /user/order/reminder/{id} ====================

    @Test
    @DisplayName("用户催单 - 成功")
    void testReminder_Success() throws Exception {
        doNothing().when(orderService).reminder(1L);

        mockMvc.perform(get("/user/order/reminder/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService, times(1)).reminder(1L);
    }
}
