package com.sky.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.BaseIntegrationTest;
import com.sky.IntegrationTestHelper;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.task.OrderTask;
import com.sky.utils.WeChatPayUtil;

/**
 * M5 集成测试 — 对应 Jira SCRUM-261 / 262 / 263
 * <p>
 * 前置：在 sky-take-out-main 目录执行 {@code docker compose up -d}
 */
@Sql(scripts = "/integration-m5-setup.sql", config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
@Transactional
class OrderIntegrationTest extends BaseIntegrationTest {

    private static final long TEST_USER_ID = 9001L;
    private static final long TEST_DISH_ID = 9001L;
    private static final long TEST_ADDRESS_ID = 9001L;
    private static final String USER_AUTH_HEADER = "authentication";
    private static final String ADMIN_TOKEN_HEADER = "token";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderTask orderTask;

    @MockBean
    private WeChatPayUtil weChatPayUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        when(weChatPayUtil.refund(any(), any(), any(), any())).thenReturn("mock-refund-ok");
        adminToken = IntegrationTestHelper.adminLogin(mockMvc);
        userToken = IntegrationTestHelper.userToken(TEST_USER_ID);
    }

    /**
     * SCRUM-261：下单 → 接单 → 派送 → 完成，验证状态链 2→3→4→5
     */
    @Test
    void scenario_fullOrderLifecycle_confirmDeliveryComplete() throws Exception {
        long orderId = createPaidOrder();

        assertThat(orderMapper.getById(orderId).getStatus()).isEqualTo(Orders.TO_BE_CONFIRMED);

        OrdersConfirmDTO confirmDTO = new OrdersConfirmDTO();
        confirmDTO.setId(orderId);
        mockMvc.perform(put("/admin/order/confirm")
                        .header(ADMIN_TOKEN_HEADER, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(confirmDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
        assertThat(orderMapper.getById(orderId).getStatus()).isEqualTo(Orders.CONFIRMED);

        mockMvc.perform(put("/admin/order/delivery/{id}", orderId)
                        .header(ADMIN_TOKEN_HEADER, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
        assertThat(orderMapper.getById(orderId).getStatus()).isEqualTo(Orders.DELIVERY_IN_PROGRESS);

        mockMvc.perform(put("/admin/order/complete/{id}", orderId)
                        .header(ADMIN_TOKEN_HEADER, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
        assertThat(orderMapper.getById(orderId).getStatus()).isEqualTo(Orders.COMPLETED);
    }

    /**
     * SCRUM-262：下单 → 拒单，验证状态与拒单原因入库
     */
    @Test
    void scenario_orderRejection_persistsStatusAndReason() throws Exception {
        long orderId = createPaidOrder();

        OrdersRejectionDTO rejectionDTO = new OrdersRejectionDTO();
        rejectionDTO.setId(orderId);
        rejectionDTO.setRejectionReason("店铺太忙");

        mockMvc.perform(put("/admin/order/rejection")
                        .header(ADMIN_TOKEN_HEADER, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(rejectionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Orders cancelled = orderMapper.getById(orderId);
        assertThat(cancelled.getStatus()).isEqualTo(Orders.CANCELLED);
        assertThat(cancelled.getRejectionReason()).isEqualTo("店铺太忙");
        assertThat(cancelled.getCancelTime()).isNotNull();
    }

    /**
     * SCRUM-263：模拟超时待付款订单，触发 OrderTask，验证自动取消
     */
    @Test
    void scenario_timeoutOrder_autoCancelledByOrderTask() {
        Orders timeoutOrder = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(Orders.PENDING_PAYMENT)
                .userId(TEST_USER_ID)
                .addressBookId(TEST_ADDRESS_ID)
                .orderTime(LocalDateTime.now().minusMinutes(20))
                .payMethod(1)
                .payStatus(Orders.UN_PAID)
                .amount(new BigDecimal("10.00"))
                .phone("13900009001")
                .consignee("测试收货人")
                .address("测试地址1号")
                .deliveryStatus(1)
                .tablewareStatus(1)
                .tablewareNumber(1)
                .packAmount(0)
                .build();
        orderMapper.insert(timeoutOrder);
        Long orderId = timeoutOrder.getId();

        orderTask.processTimeoutOrder();

        Orders updated = orderMapper.getById(orderId);
        assertThat(updated.getStatus()).isEqualTo(Orders.CANCELLED);
        assertThat(updated.getCancelReason()).isEqualTo("超时未支付");
        assertThat(updated.getCancelTime()).isNotNull();
    }

    private long createPaidOrder() throws Exception {
        ShoppingCartDTO cartDTO = new ShoppingCartDTO();
        cartDTO.setDishId(TEST_DISH_ID);
        mockMvc.perform(post("/user/shoppingCart/add")
                        .header(USER_AUTH_HEADER, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(cartDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(TEST_ADDRESS_ID);
        submitDTO.setPayMethod(1);
        submitDTO.setAmount(new BigDecimal("10.00"));
        submitDTO.setDeliveryStatus(1);
        submitDTO.setTablewareStatus(1);
        submitDTO.setTablewareNumber(1);
        submitDTO.setPackAmount(0);

        var submitResult = mockMvc.perform(post("/user/order/submit")
                        .header(USER_AUTH_HEADER, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(submitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        JSONObject submitData = IntegrationTestHelper.parseDataJson(submitResult);
        long orderId = submitData.getLongValue("id");
        String orderNumber = submitData.getString("orderNumber");

        OrdersPaymentDTO paymentDTO = new OrdersPaymentDTO();
        paymentDTO.setOrderNumber(orderNumber);
        paymentDTO.setPayMethod(1);

        mockMvc.perform(put("/user/order/payment")
                        .header(USER_AUTH_HEADER, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(paymentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Orders paid = orderMapper.getById(orderId);
        assertThat(paid.getStatus()).isEqualTo(Orders.TO_BE_CONFIRMED);
        assertThat(paid.getPayStatus()).isEqualTo(Orders.PAID);
        return orderId;
    }
}
