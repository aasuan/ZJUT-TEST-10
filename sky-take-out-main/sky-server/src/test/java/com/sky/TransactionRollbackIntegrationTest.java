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
 * 场景5：下单事务回滚验证（模拟库存不足→事务回滚）
 * <p>
 * 当前版本未实现库存管理，通过验证 @Transactional 的原子性来间接验证：
 * 成功→全提交（订单+详情+清空购物车），失败→全回滚（无残留数据）
 *
 * 前置条件：docker compose up -d
 */
@DisplayName("场景5-下单事务回滚验证")
public class TransactionRollbackIntegrationTest extends BaseIntegrationTest {

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
        userId = insertUser("test_tx_" + ts);
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
    @DisplayName("下单事务回滚验证（空购物车提交→事务回滚→正常提交→事务完整）")
    void testTransactionRollbackOnSubmitFailure() throws Exception {
        // === 阶段1：空购物车提交 → 触发异常，事务应回滚 ===

        // 确保购物车为空
        mockMvc.perform(delete("/user/shoppingCart/clean")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk());

        // 空购物车提交 → 应失败（ShoppingCartBusinessException）
        OrdersSubmitDTO submitDTO = buildSubmitDTO();
        mockMvc.perform(post("/user/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(submitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 验证数据库无残留（事务回滚）
        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE user_id = ?", Integer.class, userId);
        assertEquals(0, orderCount, "空购物车提交失败后不应产生任何订单记录");

        // === 阶段2：正常加购 → 提交成功 → 验证事务完整提交 ===

        Long dishId = getFirstDishId();
        addToCart(dishId);

        MvcResult successResult = mockMvc.perform(post("/user/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(submitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        Long orderId = JSON.parseObject(successResult.getResponse().getContentAsString())
                .getJSONObject("data").getLong("id");

        // 验证订单入库
        Map<String, Object> orderRow = jdbcTemplate.queryForMap(
                "SELECT * FROM orders WHERE id = ?", orderId);
        assertNotNull(orderRow, "成功提交后订单应存在");

        // 验证订单详情入库
        List<Map<String, Object>> details = jdbcTemplate.queryForList(
                "SELECT * FROM order_detail WHERE order_id = ?", orderId);
        assertFalse(details.isEmpty(), "成功提交后订单详情应存在");

        // 验证购物车已清空（事务内的清空操作生效）
        MvcResult cartResult = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(0, toArray(cartResult).size(), "事务提交后购物车应清空");
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
        dto.setRemark("事务测试订单");
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
