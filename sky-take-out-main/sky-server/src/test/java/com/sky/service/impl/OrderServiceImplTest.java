package com.sky.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.pagehelper.Page;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("订单服务层测试")
class OrderServiceImplTest {

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

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 用户下单 submitOrder() ====================

    @Test
    @DisplayName("下单 - 正常下单成功")
    void testSubmitOrder_Success() {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setPayMethod(1);
        dto.setAmount(new BigDecimal("86.00"));
        dto.setRemark("少放辣");
        dto.setDeliveryStatus(1);
        dto.setTablewareNumber(1);
        dto.setTablewareStatus(1);
        dto.setPackAmount(0);

        AddressBook addressBook = new AddressBook();
        addressBook.setId(1L);
        addressBook.setUserId(USER_ID);
        addressBook.setPhone("13800138000");
        addressBook.setConsignee("张三");
        addressBook.setDetail("北京市朝阳区");

        ShoppingCart cart1 = new ShoppingCart();
        cart1.setId(1L);
        cart1.setUserId(USER_ID);
        cart1.setDishId(10L);
        cart1.setName("宫保鸡丁");
        cart1.setNumber(2);
        cart1.setAmount(new BigDecimal("28.00"));
        cart1.setImage("gongbao.jpg");

        ShoppingCart cart2 = new ShoppingCart();
        cart2.setId(2L);
        cart2.setUserId(USER_ID);
        cart2.setSetmealId(100L);
        cart2.setName("豪华套餐");
        cart2.setNumber(1);
        cart2.setAmount(new BigDecimal("58.00"));
        cart2.setImage("setmeal.jpg");

        List<ShoppingCart> cartList = new ArrayList<>();
        cartList.add(cart1);
        cartList.add(cart2);

        when(addressBookMapper.getById(1L)).thenReturn(addressBook);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(cartList);

        OrderSubmitVO result = orderService.submitOrder(dto);

        assertNotNull(result);
        assertNotNull(result.getOrderNumber());
        assertNotNull(result.getOrderTime());
        assertEquals(0, new BigDecimal("86.00").compareTo(result.getOrderAmount()));

        verify(addressBookMapper).getById(1L);
        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(orderMapper).insert(any(Orders.class));
        verify(orderDetailMapper).insertBatch(any());
        verify(shoppingCartMapper).deleteByUserId(USER_ID);
    }

    @Test
    @DisplayName("下单 - 地址不存在抛异常")
    void testSubmitOrder_AddressNotFound() {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(999L);

        when(addressBookMapper.getById(999L)).thenReturn(null);

        assertThrows(AddressBookBusinessException.class, () -> orderService.submitOrder(dto));

        verify(addressBookMapper).getById(999L);
        verify(shoppingCartMapper, never()).list(any());
        verify(orderMapper, never()).insert(any());
    }

    @Test
    @DisplayName("下单 - 购物车为空抛异常")
    void testSubmitOrder_EmptyCart() {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);

        AddressBook addressBook = AddressBook.builder().id(1L).userId(USER_ID).build();

        when(addressBookMapper.getById(1L)).thenReturn(addressBook);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

        assertThrows(ShoppingCartBusinessException.class, () -> orderService.submitOrder(dto));

        verify(addressBookMapper).getById(1L);
        verify(shoppingCartMapper).list(any(ShoppingCart.class));
        verify(orderMapper, never()).insert(any());
    }

    // ==================== 订单支付 payment() / paySuccess() ====================

    @Test
    @DisplayName("支付 - 调用payment触发paySuccess")
    void testPayment() throws Exception {
        OrdersPaymentDTO dto = new OrdersPaymentDTO();
        dto.setOrderNumber("20240101000001");
        dto.setPayMethod(1);

        Orders ordersDB = Orders.builder()
                .id(100L).number("20240101000001").status(Orders.PENDING_PAYMENT)
                .payStatus(Orders.UN_PAID).build();

        when(orderMapper.getByNumber("20240101000001")).thenReturn(ordersDB);

        OrderPaymentVO result = orderService.payment(dto);

        // payment方法当前stubbed，直接调用paySuccess并返回null
        verify(orderMapper).getByNumber("20240101000001");
        verify(orderMapper).update(any(Orders.class));
        verify(webSocketServer).sendToAllClient(anyString());
    }

    @Test
    @DisplayName("支付成功 - 更新订单状态为待接单并通知商家")
    void testPaySuccess() {
        Orders ordersDB = Orders.builder()
                .id(100L).number("20240101000001").status(Orders.PENDING_PAYMENT)
                .payStatus(Orders.UN_PAID).build();

        when(orderMapper.getByNumber("20240101000001")).thenReturn(ordersDB);

        orderService.paySuccess("20240101000001");

        verify(orderMapper).getByNumber("20240101000001");
        verify(orderMapper).update(any(Orders.class));
        verify(webSocketServer).sendToAllClient(anyString());
    }

    // ==================== 查询历史订单 pageQueryByUser() ====================

    @Test
    @DisplayName("查询历史订单 - 正常分页查询")
    void testPageQueryByUser_Success() {
        Orders orders = Orders.builder().id(1L).number("NUM001")
                .status(Orders.COMPLETED).amount(new BigDecimal("58.00")).build();
        Page<Orders> page = new Page<>();
        page.add(orders);
        page.setTotal(1L);

        OrderDetail detail = OrderDetail.builder().id(1L).orderId(1L)
                .name("宫保鸡丁").number(2).amount(new BigDecimal("28.00")).build();
        List<OrderDetail> details = new ArrayList<>();
        details.add(detail);

        when(orderMapper.pageQuery(any())).thenReturn(page);
        when(orderDetailMapper.getByOrderId(1L)).thenReturn(details);

        PageResult<OrderVO> result = orderService.pageQueryByUser(1, 10, null);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("NUM001", result.getRecords().get(0).getNumber());
        verify(orderMapper).pageQuery(any());
        verify(orderDetailMapper).getByOrderId(1L);
    }

    @Test
    @DisplayName("查询历史订单 - 空结果")
    void testPageQueryByUser_Empty() {
        when(orderMapper.pageQuery(any())).thenReturn(null);

        PageResult<OrderVO> result = orderService.pageQueryByUser(1, 10, null);

        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getRecords().size());
    }

    // ==================== 查询订单详情 details() ====================

    @Test
    @DisplayName("查询订单详情 - 成功")
    void testDetails_Success() {
        Orders orders = Orders.builder().id(1L).number("NUM001")
                .status(Orders.TO_BE_CONFIRMED).amount(new BigDecimal("58.00")).build();

        OrderDetail detail = OrderDetail.builder().id(1L).orderId(1L)
                .name("宫保鸡丁").number(2).amount(new BigDecimal("28.00")).build();

        when(orderMapper.getById(1L)).thenReturn(orders);
        when(orderDetailMapper.getByOrderId(1L)).thenReturn(Collections.singletonList(detail));

        OrderVO result = orderService.details(1L);

        assertNotNull(result);
        assertEquals("NUM001", result.getNumber());
        assertEquals(1, result.getOrderDetailList().size());
        assertEquals("宫保鸡丁", result.getOrderDetailList().get(0).getName());

        verify(orderMapper).getById(1L);
        verify(orderDetailMapper).getByOrderId(1L);
    }

    // ==================== 用户取消订单 userCancelById() ====================

    @Test
    @DisplayName("取消订单 - 订单不存在")
    void testUserCancelById_OrderNotFound() {
        when(orderMapper.getById(999L)).thenReturn(null);

        assertThrows(OrderBusinessException.class, () -> orderService.userCancelById(999L));
        verify(orderMapper, never()).update(any());
    }

    @Test
    @DisplayName("取消订单 - 待付款状态可直接取消")
    void testUserCancelById_PendingPayment() throws Exception {
        Orders ordersDB = Orders.builder().id(1L).number("NUM001")
                .status(Orders.PENDING_PAYMENT).payStatus(Orders.UN_PAID).build();

        when(orderMapper.getById(1L)).thenReturn(ordersDB);

        orderService.userCancelById(1L);

        verify(orderMapper).getById(1L);
        verify(orderMapper).update(any(Orders.class));
        verify(weChatPayUtil, never()).refund(anyString(), anyString(), any(), any());
    }

    @Test
    @DisplayName("取消订单 - 待接单状态取消需退款")
    void testUserCancelById_ToBeConfirmed() throws Exception {
        Orders ordersDB = Orders.builder().id(1L).number("NUM001")
                .status(Orders.TO_BE_CONFIRMED).payStatus(Orders.PAID).build();

        when(orderMapper.getById(1L)).thenReturn(ordersDB);
        when(weChatPayUtil.refund(anyString(), anyString(), any(), any())).thenReturn("SUCCESS");

        orderService.userCancelById(1L);

        verify(orderMapper).getById(1L);
        verify(weChatPayUtil).refund(anyString(), anyString(), any(), any());
        verify(orderMapper).update(any(Orders.class));
    }

    @Test
    @DisplayName("取消订单 - 已接单及以上状态不可取消")
    void testUserCancelById_ConfirmedStatus() {
        Orders ordersDB = Orders.builder().id(1L).number("NUM001")
                .status(Orders.CONFIRMED).build();

        when(orderMapper.getById(1L)).thenReturn(ordersDB);

        assertThrows(OrderBusinessException.class, () -> orderService.userCancelById(1L));
        verify(orderMapper, never()).update(any());
    }

    // ==================== 再来一单 repetition() ====================

    @Test
    @DisplayName("再来一单 - 成功")
    void testRepetition_Success() {
        OrderDetail detail1 = OrderDetail.builder().id(1L).orderId(1L)
                .dishId(10L).name("宫保鸡丁").number(2).amount(new BigDecimal("28.00"))
                .image("gongbao.jpg").build();
        OrderDetail detail2 = OrderDetail.builder().id(2L).orderId(1L)
                .setmealId(100L).name("豪华套餐").number(1).amount(new BigDecimal("58.00"))
                .image("setmeal.jpg").build();
        List<OrderDetail> details = new ArrayList<>();
        details.add(detail1);
        details.add(detail2);

        when(orderDetailMapper.getByOrderId(1L)).thenReturn(details);

        orderService.repetition(1L);

        verify(orderDetailMapper).getByOrderId(1L);
        verify(shoppingCartMapper).insertBatch(any());
    }

    // ==================== 用户催单 reminder() ====================

    @Test
    @DisplayName("催单 - 成功")
    void testReminder_Success() {
        Orders orders = Orders.builder().id(1L).number("NUM001")
                .status(Orders.TO_BE_CONFIRMED).build();

        when(orderMapper.getById(1L)).thenReturn(orders);

        orderService.reminder(1L);

        verify(orderMapper).getById(1L);
        verify(webSocketServer).sendToAllClient(anyString());
    }

    @Test
    @DisplayName("催单 - 订单不存在抛异常")
    void testReminder_OrderNotFound() {
        when(orderMapper.getById(999L)).thenReturn(null);

        assertThrows(OrderBusinessException.class, () -> orderService.reminder(999L));
        verify(webSocketServer, never()).sendToAllClient(anyString());
    }
}
