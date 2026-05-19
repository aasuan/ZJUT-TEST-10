package com.sky.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.fastjson.JSON;
import com.sky.BaseIntegrationTest;
import com.sky.IntegrationTestHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

/**
 * M2 员工管理 + JWT 集成测试（连接 Docker MySQL/Redis，全链路）
 */
@Sql(scripts = "/data-integration.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EmployeeIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN_TOKEN_HEADER = "token";

    @Test
    @DisplayName("集成：登录获取Token → 带Token访问分页/新增/修改 → 无Token返回401")
    void login_withToken_accessEmployeeApis_withoutToken_unauthorized() throws Exception {
        String token = IntegrationTestHelper.adminLogin(mockMvc);

        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header(ADMIN_TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").exists());

        String uniqueUser = "integ_m2_" + System.currentTimeMillis();
        EmployeeDTO createDto = new EmployeeDTO();
        createDto.setUsername(uniqueUser);
        createDto.setName("集成测试员工");
        createDto.setPhone("13900001111");
        createDto.setSex("1");
        createDto.setIdNumber("320101199001011234");

        mockMvc.perform(post("/admin/employee")
                        .header(ADMIN_TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setId(1L);
        updateDto.setName("管理员-集成测");

        mockMvc.perform(put("/admin/employee")
                        .header(ADMIN_TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("集成：错误密码登录 → GlobalExceptionHandler 返回错误JSON")
    void login_wrongPassword_returnsBusinessErrorJson() throws Exception {
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrong-password");

        mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstant.PASSWORD_ERROR));
    }
}
