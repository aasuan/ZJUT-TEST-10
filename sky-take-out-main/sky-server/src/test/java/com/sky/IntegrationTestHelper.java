package com.sky;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.utils.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试工具类
 * <p>
 * 提供登录获取 Token 等公共方法，避免各模块重复编写。
 */
public class IntegrationTestHelper {

    /**
     * 管理端登录，返回 JWT Token
     *
     * @param mockMvc  MockMvc 实例
     * @param username 用户名
     * @param password 密码
     * @return JWT token 字符串
     */
    public static String adminLogin(MockMvc mockMvc, String username, String password) throws Exception {
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);

        MvcResult result = mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JSONObject json = JSON.parseObject(responseBody);
        return json.getJSONObject("data").getString("token");
    }

    /**
     * 管理端登录（默认账号 admin/123456）
     */
    public static String adminLogin(MockMvc mockMvc) throws Exception {
        return adminLogin(mockMvc, "admin", "123456");
    }

    /**
     * 生成 C 端用户 JWT（集成测试用，与 application-integration.yml 中 user-secret-key 一致）
     */
    public static String userToken(long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        return JwtUtil.createJWT("itcast", 7_200_000L, claims);
    }

    /**
     * 解析 Result JSON 的 data 节点
     */
    public static JSONObject parseDataJson(MvcResult result) throws Exception {
        JSONObject json = JSON.parseObject(result.getResponse().getContentAsString());
        return json.getJSONObject("data");
    }
}
