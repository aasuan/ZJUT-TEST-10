package com.sky;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.EmployeeLoginDTO;
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
}
