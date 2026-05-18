package com.sky.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;

/**
 * M5 — {@link OrderTask} 定时任务逻辑（Mock {@link OrderMapper}，直接调方法不测 cron）。
 */
@ExtendWith(MockitoExtension.class)
class OrderTaskTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderTask orderTask;

    @Test
    void processTimeoutOrder_whenNoOrders_doesNotUpdate() {
        when(orderMapper.getByStatusAndOrderTimeLT(eq(Orders.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        orderTask.processTimeoutOrder();

        verify(orderMapper).getByStatusAndOrderTimeLT(eq(Orders.PENDING_PAYMENT), any(LocalDateTime.class));
        verify(orderMapper, never()).update(any());
    }

    @Test
    void processTimeoutOrder_cancelsEachWithReason() {
        Orders o1 = Orders.builder().id(1L).number("T1").status(Orders.PENDING_PAYMENT).build();
        when(orderMapper.getByStatusAndOrderTimeLT(eq(Orders.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(List.of(o1));

        orderTask.processTimeoutOrder();

        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper, times(1)).update(cap.capture());
        Orders updated = cap.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getStatus()).isEqualTo(Orders.CANCELLED);
        assertThat(updated.getCancelReason()).isEqualTo("超时未支付");
        assertThat(updated.getCancelTime()).isNotNull();
    }

    @Test
    void processDeliveryOrder_whenNoOrders_doesNotUpdate() {
        when(orderMapper.getByStatusAndOrderTimeLT(eq(Orders.DELIVERY_IN_PROGRESS), any(LocalDateTime.class)))
                .thenReturn(null);

        orderTask.processDeliveryOrder();

        verify(orderMapper).getByStatusAndOrderTimeLT(eq(Orders.DELIVERY_IN_PROGRESS), any(LocalDateTime.class));
        verify(orderMapper, never()).update(any());
    }

    @Test
    void processDeliveryOrder_completesWithReason() {
        Orders o1 = Orders.builder().id(2L).status(Orders.DELIVERY_IN_PROGRESS).build();
        when(orderMapper.getByStatusAndOrderTimeLT(eq(Orders.DELIVERY_IN_PROGRESS), any(LocalDateTime.class)))
                .thenReturn(List.of(o1));

        orderTask.processDeliveryOrder();

        ArgumentCaptor<Orders> cap = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).update(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(Orders.COMPLETED);
        assertThat(cap.getValue().getCancelReason()).isEqualTo("超时未确认收货");
        assertThat(cap.getValue().getCancelTime()).isNotNull();
    }
}
