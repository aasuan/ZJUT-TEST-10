package com.sky.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;

/**
 * M5 — 管理端 {@link OrderController} MockMvc 单测（独立 Standalone，不经过 JWT 拦截器）。
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController();
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void conditionSearch_returnsOkAndCode1() throws Exception {
        PageResult<OrderVO> page = new PageResult<>(0L, List.of());
        when(orderService.conditionSearch(any(OrdersPageQueryDTO.class))).thenReturn(page);

        mockMvc.perform(get("/admin/order/conditionSearch")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(orderService).conditionSearch(any(OrdersPageQueryDTO.class));
    }

    @Test
    void statistics_returnsVo() throws Exception {
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(1);
        vo.setConfirmed(2);
        vo.setDeliveryInProgress(3);
        when(orderService.statistics()).thenReturn(vo);

        mockMvc.perform(get("/admin/order/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.toBeConfirmed").value(1))
                .andExpect(jsonPath("$.data.confirmed").value(2))
                .andExpect(jsonPath("$.data.deliveryInProgress").value(3));
    }

    @Test
    void details_returnsOrderVo() throws Exception {
        OrderVO vo = new OrderVO();
        vo.setId(100L);
        vo.setNumber("N-100");
        when(orderService.details(100L)).thenReturn(vo);

        mockMvc.perform(get("/admin/order/details/{id}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.number").value("N-100"));

        verify(orderService).details(eq(100L));
    }

    @Test
    void confirm_callsService() throws Exception {
        OrdersConfirmDTO dto = new OrdersConfirmDTO();
        dto.setId(1L);
        dto.setStatus(Orders.CONFIRMED);

        mockMvc.perform(put("/admin/order/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService).confirm(any(OrdersConfirmDTO.class));
    }

    @Test
    void rejection_callsService() throws Exception {
        OrdersRejectionDTO dto = new OrdersRejectionDTO();
        dto.setId(2L);
        dto.setRejectionReason("太忙");

        mockMvc.perform(put("/admin/order/rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService).rejection(any(OrdersRejectionDTO.class));
    }

    @Test
    void cancel_callsService() throws Exception {
        OrdersCancelDTO dto = new OrdersCancelDTO();
        dto.setId(3L);
        dto.setCancelReason("顾客取消");

        mockMvc.perform(put("/admin/order/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService).cancel(any(OrdersCancelDTO.class));
    }

    @Test
    void delivery_callsService() throws Exception {
        mockMvc.perform(put("/admin/order/delivery/{id}", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService).delivery(eq(4L));
    }

    @Test
    void complete_callsService() throws Exception {
        mockMvc.perform(put("/admin/order/complete/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(orderService).complete(eq(5L));
    }
}
