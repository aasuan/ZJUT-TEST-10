package com.sky.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;

/**
 * M5 — 管理端订单相关 {@link OrderServiceImpl} 行为（Mock Mapper / 支付工具）。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplAdminTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @Mock
    private AddressBookMapper addressBookMapper;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private WeChatPayUtil weChatPayUtil;

    @Mock
    private WebSocketServer webSocketServer;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void clearPageHelper() {
        PageHelper.clearPage();
    }

    /** 当前实现不校验原状态，仅把订单更新为已接单。 */
    @Test
    void confirm_updatesOrderToConfirmed() {
        OrdersConfirmDTO dto = new OrdersConfirmDTO();
        dto.setId(10L);

        orderService.confirm(dto);

        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).update(cap.capture());
        assertThat(cap.getValue().getId()).isEqualTo(10L);
        assertThat(cap.getValue().getStatus()).isEqualTo(Orders.CONFIRMED);
    }

    @Test
    void statistics_mapsThreeStatusCounts() {
        when(orderMapper.countStatus(Orders.TO_BE_CONFIRMED)).thenReturn(2);
        when(orderMapper.countStatus(Orders.CONFIRMED)).thenReturn(3);
        when(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS)).thenReturn(1);

        OrderStatisticsVO vo = orderService.statistics();

        assertThat(vo.getToBeConfirmed()).isEqualTo(2);
        assertThat(vo.getConfirmed()).isEqualTo(3);
        assertThat(vo.getDeliveryInProgress()).isEqualTo(1);
        verify(orderMapper).countStatus(Orders.TO_BE_CONFIRMED);
        verify(orderMapper).countStatus(Orders.CONFIRMED);
        verify(orderMapper).countStatus(Orders.DELIVERY_IN_PROGRESS);
    }

    @Test
    void details_fillsOrderDetailList() {
        Orders db = Orders.builder()
                .id(1L)
                .number("NO-1")
                .status(Orders.CONFIRMED)
                .build();
        when(orderMapper.getById(1L)).thenReturn(db);
        OrderDetail line = OrderDetail.builder().orderId(1L).name("宫保鸡丁").number(2).build();
        when(orderDetailMapper.getByOrderId(1L)).thenReturn(List.of(line));

        OrderVO vo = orderService.details(1L);

        assertThat(vo.getNumber()).isEqualTo("NO-1");
        assertThat(vo.getOrderDetailList()).hasSize(1);
        assertThat(vo.getOrderDetailList().get(0).getName()).isEqualTo("宫保鸡丁");
    }

    @Test
    void conditionSearch_returnsOrderDishesAndTotal() {
        OrdersPageQueryDTO q = new OrdersPageQueryDTO();
        q.setPage(1);
        q.setPageSize(10);

        Orders row = Orders.builder().id(5L).number("X-5").status(Orders.TO_BE_CONFIRMED).build();
        Page<Orders> page = new Page<>(1, 10);
        page.add(row);
        page.setTotal(1L);

        when(orderMapper.pageQuery(q)).thenReturn(page);
        when(orderDetailMapper.getByOrderId(5L)).thenReturn(List.of(
                OrderDetail.builder().name("米饭").number(1).build()));

        PageResult<OrderVO> result = orderService.conditionSearch(q);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getOrderDishes()).isEqualTo("米饭*1;");
    }

    @Test
    void rejection_whenToBeConfirmedAndPaid_refundsAndCancels() throws Exception {
        Orders db = Orders.builder()
                .id(2L)
                .number("P-2")
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .build();
        when(orderMapper.getById(2L)).thenReturn(db);
        when(weChatPayUtil.refund(any(), any(), any(), any())).thenReturn("ok");

        OrdersRejectionDTO dto = new OrdersRejectionDTO();
        dto.setId(2L);
        dto.setRejectionReason("太忙");

        orderService.rejection(dto);

        verify(weChatPayUtil, times(1)).refund(eq("P-2"), eq("P-2"), any(BigDecimal.class), any(BigDecimal.class));
        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).update(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(Orders.CANCELLED);
        assertThat(cap.getValue().getRejectionReason()).isEqualTo("太忙");
        assertThat(cap.getValue().getCancelTime()).isNotNull();
    }

    @Test
    void rejection_whenUnpaid_skipsRefund() throws Exception {
        Orders db = Orders.builder()
                .id(3L)
                .number("P-3")
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.UN_PAID)
                .build();
        when(orderMapper.getById(3L)).thenReturn(db);

        OrdersRejectionDTO dto = new OrdersRejectionDTO();
        dto.setId(3L);
        dto.setRejectionReason("无货");

        orderService.rejection(dto);

        verify(weChatPayUtil, never()).refund(any(), any(), any(), any());
        verify(orderMapper).update(any());
    }

    @Test
    void rejection_whenOrderMissing_throws() {
        when(orderMapper.getById(99L)).thenReturn(null);
        OrdersRejectionDTO dto = new OrdersRejectionDTO();
        dto.setId(99L);
        dto.setRejectionReason("x");

        assertThatThrownBy(() -> orderService.rejection(dto))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining(MessageConstant.ORDER_STATUS_ERROR);
    }

    @Test
    void rejection_whenNotToBeConfirmed_throws() {
        Orders db = Orders.builder().id(4L).status(Orders.CONFIRMED).build();
        when(orderMapper.getById(4L)).thenReturn(db);
        OrdersRejectionDTO dto = new OrdersRejectionDTO();
        dto.setId(4L);
        dto.setRejectionReason("x");

        assertThatThrownBy(() -> orderService.rejection(dto))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining(MessageConstant.ORDER_STATUS_ERROR);
    }

    @Test
    void cancel_whenPayStatusPaid_callsRefund() throws Exception {
        Orders db = Orders.builder().id(6L).number("C-6").payStatus(Orders.PAID).build();
        when(orderMapper.getById(6L)).thenReturn(db);

        OrdersCancelDTO dto = new OrdersCancelDTO();
        dto.setId(6L);
        dto.setCancelReason("顾客要求");

        orderService.cancel(dto);

        verify(weChatPayUtil, times(1)).refund(eq("C-6"), eq("C-6"), any(BigDecimal.class), any(BigDecimal.class));
        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).update(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(Orders.CANCELLED);
        assertThat(cap.getValue().getCancelReason()).isEqualTo("顾客要求");
    }

    @Test
    void cancel_whenUnpaid_skipsRefund() throws Exception {
        Orders db = Orders.builder().id(7L).number("C-7").payStatus(Orders.UN_PAID).build();
        when(orderMapper.getById(7L)).thenReturn(db);

        OrdersCancelDTO dto = new OrdersCancelDTO();
        dto.setId(7L);
        dto.setCancelReason("测试");

        orderService.cancel(dto);

        verify(weChatPayUtil, never()).refund(any(), any(), any(), any());
        verify(orderMapper).update(any());
    }

    @Test
    void delivery_whenConfirmed_movesToDeliveryInProgress() {
        Orders db = Orders.builder().id(8L).status(Orders.CONFIRMED).build();
        when(orderMapper.getById(8L)).thenReturn(db);

        orderService.delivery(8L);

        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).update(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(Orders.DELIVERY_IN_PROGRESS);
    }

    @Test
    void delivery_whenOrderMissing_throws() {
        when(orderMapper.getById(8L)).thenReturn(null);

        assertThatThrownBy(() -> orderService.delivery(8L))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining(MessageConstant.ORDER_STATUS_ERROR);
    }

    @Test
    void delivery_whenNotConfirmed_throws() {
        when(orderMapper.getById(8L)).thenReturn(Orders.builder().id(8L).status(Orders.TO_BE_CONFIRMED).build());

        assertThatThrownBy(() -> orderService.delivery(8L))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining(MessageConstant.ORDER_STATUS_ERROR);
    }

    @Test
    void complete_whenDelivering_movesToCompleted() {
        Orders db = Orders.builder().id(9L).status(Orders.DELIVERY_IN_PROGRESS).build();
        when(orderMapper.getById(9L)).thenReturn(db);

        orderService.complete(9L);

        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).update(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(Orders.COMPLETED);
        assertThat(cap.getValue().getDeliveryTime()).isNotNull();
    }

    @Test
    void complete_whenNotDelivering_throws() {
        when(orderMapper.getById(9L)).thenReturn(Orders.builder().id(9L).status(Orders.CONFIRMED).build());

        assertThatThrownBy(() -> orderService.complete(9L))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining(MessageConstant.ORDER_STATUS_ERROR);
    }

}
