package com.sky;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
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

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 场景3：不同用户购物车数据隔离验证
 *
 * 前置条件：docker compose up -d
 */
@DisplayName("场景3-不同用户购物车数据隔离")
public class CartDataIsolationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String USER_SECRET = "itcast";
    private static final long USER_TTL = 7200000L;
    private static final String TOKEN_HEADER = "authentication";

    private Long userIdA;
    private Long userIdB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        String ts = String.valueOf(System.currentTimeMillis());
        userIdA = insertUser("test_iso_a_" + ts);
        userIdB = insertUser("test_iso_b_" + ts);
        tokenA = generateToken(userIdA);
        tokenB = generateToken(userIdB);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM shopping_cart WHERE user_id IN (?, ?)", userIdA, userIdB);
        jdbcTemplate.update("DELETE FROM `user` WHERE id IN (?, ?)", userIdA, userIdB);
    }

    @Test
    @DisplayName("不同用户购物车数据隔离验证")
    void testCartDataIsolationBetweenUsers() throws Exception {
        // 用户A加购菜品
        Long dishIdA = getFirstDishId(tokenA);
        addToCart(tokenA, dishIdA, "中辣");
        // 再加一份同菜品，数量变2
        ShoppingCartDTO dtoA2 = new ShoppingCartDTO();
        dtoA2.setDishId(dishIdA);
        dtoA2.setDishFlavor("中辣");
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, tokenA)
                .content(JSON.toJSONString(dtoA2)))
                .andExpect(status().isOk());

        // 用户B加购不同菜品
        Long dishIdB = getFirstDishId(tokenB);
        addToCart(tokenB, dishIdB, "微辣");

        // 查询用户A的购物车
        MvcResult resultA = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, tokenA))
                .andExpect(status().isOk())
                .andReturn();
        JSONArray itemsA = toArray(resultA);

        // 查询用户B的购物车
        MvcResult resultB = mockMvc.perform(get("/user/shoppingCart/list")
                .header(TOKEN_HEADER, tokenB))
                .andExpect(status().isOk())
                .andReturn();
        JSONArray itemsB = toArray(resultB);

        // 断言：每个用户只能看到自己的数据
        for (int i = 0; i < itemsA.size(); i++) {
            Long uid = itemsA.getJSONObject(i).getLong("userId");
            assertEquals(userIdA, uid, "用户A购物车中不应出现其他用户数据");
        }
        for (int i = 0; i < itemsB.size(); i++) {
            Long uid = itemsB.getJSONObject(i).getLong("userId");
            assertEquals(userIdB, uid, "用户B购物车中不应出现其他用户数据");
        }

        assertTrue(itemsA.size() > 0, "用户A应有购物车数据");
        assertTrue(itemsB.size() > 0, "用户B应有购物车数据");
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

    private Long getFirstDishId(String token) throws Exception {
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

    private void addToCart(String token, Long dishId, String flavor) throws Exception {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        dto.setDishFlavor(flavor);
        mockMvc.perform(post("/user/shoppingCart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TOKEN_HEADER, token)
                .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk());
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
}
