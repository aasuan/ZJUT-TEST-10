package com.sky;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.utils.JwtUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 场景2：支付回调 → 验证订单状态变为已支付
 *
 * 前置条件：docker compose up -d
 */
@DisplayName("场景2-支付回调验证订单状态")
public class PaymentCallbackIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String USER_SECRET = "itcast";
    private static final long USER_TTL = 7200000L;
    private static final String TOKEN_HEADER = "authentication";

    private Long userId;
    private Long addressId;
    private String token;

    @BeforeEach
    void setUp() {
        String ts = String.valueOf(System.currentTimeMillis());
        userId = insertUser("test_pay_" + ts);
        addressId = insertAddress(userId);
        token = generateToken(userId);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM order_detail WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)", userId);
        jdbcTemplate.update("DELETE FROM orders WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM shopping_cart WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM address_book WHERE id = ?", addressId);
        jdbcTemplate.update("DELETE FROM `user` WHERE id = ?", userId);
    }

    @Test
    @DisplayName("支付回调→验证订单状态变为已支付")
    void testPaymentChangesOrderStatus() throws Exception {
        // 先加购
        Long dishId = getFirstDishId();
        addToCart(dishId);
        // 再加一次，数量变2
        ShoppingCartDTO dto2 = new ShoppingCartDTO();
        dto2.setDishId(dishId);
        dto2.setDishFlavor("微辣");
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(dto2)))
                .andExpect(status().isOk());

        // 提交订单
        OrdersSubmitDTO submitDTO = buildSubmitDTO();
        MvcResult submitResult = mockMvc.perform(post("/user/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(submitDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String orderNumber = JSON.parseObject(submitResult.getResponse().getContentAsString())
                .getJSONObject("data").getString("orderNumber");

        // 验证付款前状态：待付款(1)、未支付(0)
        Map<String, Object> before = jdbcTemplate.queryForMap(
                "SELECT status, pay_status FROM orders WHERE number = ?", orderNumber);
        assertEquals(1, ((Number) before.get("status")).intValue(), "付款前状态应为待付款");
        assertEquals(0, ((Number) before.get("pay_status")).intValue(), "付款前支付状态应为未支付");

        // 调用支付接口（当前代码 WeChatPayUtil 调用已注释，直接完成支付）
        OrdersPaymentDTO paymentDTO = new OrdersPaymentDTO();
        paymentDTO.setOrderNumber(orderNumber);
        paymentDTO.setPayMethod(1);

        mockMvc.perform(put("/user/order/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(paymentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 验证付款后状态：待接单(2)、已支付(1)、结账时间已记录
        Map<String, Object> after = jdbcTemplate.queryForMap(
                "SELECT status, pay_status, checkout_time FROM orders WHERE number = ?", orderNumber);
        assertEquals(2, ((Number) after.get("status")).intValue(), "支付后状态应为待接单(2)");
        assertEquals(1, ((Number) after.get("pay_status")).intValue(), "支付后支付状态应为已支付(1)");
        assertNotNull(after.get("checkout_time"), "结账时间不应为空");
    }

    // ==================== 辅助方法 ====================

    private String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        return JwtUtil.createJWT(USER_SECRET, USER_TTL, claims);
    }

    private Long getFirstDishId() throws Exception {
        MvcResult catResult = mockMvc.perform(get("/user/category/list")
                .param("type", "1").header(TOKEN_HEADER, token))
                .andExpect(status().isOk()).andReturn();
        Long catId = JSON.parseObject(catResult.getResponse().getContentAsString())
                .getJSONArray("data").getJSONObject(0).getLong("id");

        MvcResult dishResult = mockMvc.perform(get("/user/dish/list")
                .param("categoryId", String.valueOf(catId)).header(TOKEN_HEADER, token))
                .andExpect(status().isOk()).andReturn();
        return JSON.parseObject(dishResult.getResponse().getContentAsString())
                .getJSONArray("data").getJSONObject(0).getLong("id");
    }

    private void addToCart(Long dishId) throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        dto.setDishFlavor("微辣");
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk());
    }

    private OrdersSubmitDTO buildSubmitDTO() {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(addressId);
        dto.setPayMethod(1);
        dto.setAmount(new BigDecimal("36.00"));
        dto.setRemark("支付测试订单");
        dto.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        dto.setDeliveryStatus(1);
        dto.setTablewareNumber(1);
        dto.setTablewareStatus(1);
        dto.setPackAmount(2);
        return dto;
    }

    private Long insertUser(String openid) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO `user` (openid, name, create_time) VALUES (?, '测试用户', NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, openid);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    private Long insertAddress(Long userId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO address_book (user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) "
                            +
                            "VALUES (?, '测试收货人', '13800000001', '1', '330000', '浙江省', '330100', '杭州市', '330106', '西湖区', '测试地址', '公司', 1)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }
}
