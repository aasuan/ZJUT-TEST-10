package com.sky;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 场景1：浏览菜品 → 加购 → 修改数量 → 清空 → 重新加购 → 提交订单 → 验证订单入库
 *
 * 前置条件：docker compose up -d
 */
@DisplayName("场景1-购物车下单全流程")
public class CartOrderFlowIntegrationTest extends BaseIntegrationTest {

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
        userId = insertUser("test_flow_" + ts);
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
    @DisplayName("浏览菜品→加购→修改数量→清空→重新加购→提交订单→验证订单入库")
    void testCartOrderFullFlow() throws Exception {
        // 1. 浏览分类
        MvcResult catResult = mockMvc.perform(get("/user/category/list")
                .param("type", "1")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        JSONArray categories = JSON.parseObject(catResult.getResponse().getContentAsString())
                .getJSONArray("data");
        assertNotNull(categories, "分类列表不应为空");
        assertTrue(categories.size() > 0, "至少需要1个分类");
        Long categoryId = categories.getJSONObject(0).getLong("id");

        // 2. 浏览菜品
        MvcResult dishResult = mockMvc.perform(get("/user/dish/list")
                .param("categoryId", String.valueOf(categoryId))
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        JSONArray dishes = JSON.parseObject(dishResult.getResponse().getContentAsString())
                .getJSONArray("data");
        assertNotNull(dishes, "菜品列表不应为空");
        assertTrue(dishes.size() > 0, "至少需要1个菜品");
        Long dishId = dishes.getJSONObject(0).getLong("id");

        // 3. 添加购物车
        ShoppingCartDTO cartDTO = new ShoppingCartDTO();
        cartDTO.setDishId(dishId);
        cartDTO.setDishFlavor("中辣");
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(cartDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 验证购物车有1件商品
        MvcResult cartResult = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        JSONArray cartItems = toArray(cartResult);
        assertEquals(1, cartItems.size(), "应有1个购物车商品");
        assertEquals(1, cartItems.getJSONObject(0).getInteger("number"), "数量应为1");

        // 4. 再加一次同一菜品 → 数量变为2
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(cartDTO)))
                .andExpect(status().isOk());
        cartResult = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        cartItems = toArray(cartResult);
        assertEquals(1, cartItems.size(), "同一菜品应合并为1条");
        assertEquals(2, cartItems.getJSONObject(0).getInteger("number"), "数量应变为2");

        // 5. 减少数量
        mockMvc.perform(post("/user/shoppingCart/sub")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(cartDTO)))
                .andExpect(status().isOk());
        cartResult = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        cartItems = toArray(cartResult);
        assertEquals(1, cartItems.size());
        assertEquals(1, cartItems.getJSONObject(0).getInteger("number"), "减少后数量应为1");

        // 6. 清空购物车
        mockMvc.perform(delete("/user/shoppingCart/clean")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk());
        cartResult = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(0, toArray(cartResult).size(), "清空后购物车应为空");

        // 7. 重新加购（不同口味）
        ShoppingCartDTO cartDTO2 = new ShoppingCartDTO();
        cartDTO2.setDishId(dishId);
        cartDTO2.setDishFlavor("微辣");
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(cartDTO2)))
                .andExpect(status().isOk());

        // 8. 提交订单
        OrdersSubmitDTO submitDTO = buildSubmitDTO(addressId);
        MvcResult submitResult = mockMvc.perform(post("/user/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(submitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        JSONObject orderData = JSON.parseObject(submitResult.getResponse().getContentAsString())
                .getJSONObject("data");
        assertNotNull(orderData, "订单提交应返回数据");
        Long orderId = orderData.getLong("id");
        String orderNumber = orderData.getString("orderNumber");
        assertNotNull(orderId);
        assertNotNull(orderNumber);

        // 9. 验证订单入库
        Map<String, Object> orderRow = jdbcTemplate.queryForMap("SELECT * FROM orders WHERE id = ?", orderId);
        assertEquals(userId.intValue(), ((Number) orderRow.get("user_id")).intValue());
        assertEquals(1, ((Number) orderRow.get("status")).intValue(), "订单状态应为待付款(1)");
        assertEquals(0, ((Number) orderRow.get("pay_status")).intValue(), "支付状态应为未支付(0)");

        // 验证订单详情入库
        List<Map<String, Object>> details = jdbcTemplate.queryForList(
                "SELECT * FROM order_detail WHERE order_id = ?", orderId);
        assertFalse(details.isEmpty(), "订单详情不应为空");

        // 验证购物车已清空
        cartResult = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(0, toArray(cartResult).size(), "下单后购物车应清空");
    }

    // ==================== 辅助方法 ====================

    private String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        return JwtUtil.createJWT(USER_SECRET, USER_TTL, claims);
    }

    private JSONArray toArray(MvcResult result) throws Exception {
        JSONObject json = JSON.parseObject(result.getResponse().getContentAsString());
        JSONArray data = json.getJSONArray("data");
        return data != null ? data : new JSONArray();
    }

    private OrdersSubmitDTO buildSubmitDTO(Long addressId) {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(addressId);
        dto.setPayMethod(1);
        dto.setAmount(new BigDecimal("36.00"));
        dto.setRemark("集成测试订单");
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
