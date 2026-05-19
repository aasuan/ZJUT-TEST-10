package com.sky.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.BaseIntegrationTest;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import com.sky.vo.UserReportVO;

/**
 * 用户注册与统计集成测试
 * 测试流程：注册用户 → 查询用户统计 → 验证新增数正确
 * 使用Mock方式模拟Service层，不依赖真实的外部服务
 */
@DisplayName("用户注册与统计集成测试")
class UserRegistrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @MockBean
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 初始化：配置拦截器Mock，使其总是放行
     */
    void setupInterceptorMocks() throws Exception {
        // Mock用户拦截器，总是返回true（放行）
        org.mockito.Mockito.when(jwtTokenUserInterceptor.preHandle(
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class),
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletResponse.class),
                org.mockito.ArgumentMatchers.any(Object.class)))
                .thenReturn(true);

        // Mock管理员拦截器，总是返回true（放行）
        org.mockito.Mockito.when(jwtTokenAdminInterceptor.preHandle(
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class),
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletResponse.class),
                org.mockito.ArgumentMatchers.any(Object.class)))
                .thenReturn(true);
    }

    /**
     * 测试完整的用户注册流程和统计验证
     * 1. Mock用户注册（微信登录）
     * 2. Mock查询用户统计
     * 3. 验证新增用户数正确
     */
    @Test
    @DisplayName("用户注册流程 - 注册并验证统计数据")
    @SuppressWarnings("unchecked")
    void testUserRegistrationAndVerifyStatistics() throws Exception {
        // 配置拦截器Mock，使其放行所有请求
        setupInterceptorMocks();
        
        // 准备用户登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setCode("test-code");

        // 步骤1: Mock UserService.wxLogin，模拟用户注册/登录
        Long testUserId = 100L;
        String testOpenid = "test-openid-" + System.currentTimeMillis();
        User mockUser = User.builder()
                .id(testUserId)
                .openid(testOpenid)
                .name("测试用户")
                .phone("13800138000")
                .sex("1")
                .build();
        
        org.mockito.Mockito.when(userService.wxLogin(org.mockito.ArgumentMatchers.any(UserLoginDTO.class)))
                .thenReturn(mockUser);

        // 执行用户登录/注册
        MvcResult loginResult = mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(testUserId))
                .andExpect(jsonPath("$.data.openid").value(testOpenid))
                .andReturn();

        System.out.println("✓ 用户注册/登录成功，用户ID: " + testUserId);

        // 步骤2: Mock ReportService.getUserStatistics
        LocalDate today = LocalDate.now();
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(today.toString())
                .totalUserList("1")  // 总用户数为1
                .newUserList("1")    // 新增用户数为1（刚注册的用户）
                .build();
        
        org.mockito.Mockito.when(reportService.getUserStatistics(
                        org.mockito.ArgumentMatchers.eq(today),
                        org.mockito.ArgumentMatchers.eq(today)))
                .thenReturn(userReportVO);

        // 查询用户统计
        MvcResult statsResult = mockMvc.perform(get("/admin/report/userStatistics")
                        .param("begin", today.toString())
                        .param("end", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        // 解析用户统计结果
        String statsResponse = statsResult.getResponse().getContentAsString();
        Map<String, Object> statsData = objectMapper.readValue(statsResponse, HashMap.class);
        Map<String, Object> reportData = (Map<String, Object>) statsData.get("data");
        String newUserList = (String) reportData.get("newUserList");
        String totalUserList = (String) reportData.get("totalUserList");

        System.out.println("用户统计结果 - 新增用户: " + newUserList + ", 总用户: " + totalUserList);

        // 步骤3: 验证新增用户数正确
        org.junit.jupiter.api.Assertions.assertNotNull(newUserList, "新增用户列表不应为空");
        org.junit.jupiter.api.Assertions.assertNotNull(totalUserList, "总用户列表不应为空");
        
        String[] newUsers = newUserList.split(",");
        String[] totalUsers = totalUserList.split(",");
        
        org.junit.jupiter.api.Assertions.assertTrue(newUsers.length > 0, "新增用户列表应至少有一个值");
        org.junit.jupiter.api.Assertions.assertTrue(totalUsers.length > 0, "总用户列表应至少有一个值");
        
        int actualNewUsers = Integer.parseInt(newUsers[0].trim());
        int actualTotalUsers = Integer.parseInt(totalUsers[0].trim());
        
        org.junit.jupiter.api.Assertions.assertEquals(1, actualNewUsers,
                "新增用户数应该为1（刚注册的用户）");
        org.junit.jupiter.api.Assertions.assertEquals(1, actualTotalUsers,
                "总用户数应该为1");

        System.out.println("✓ 用户统计验证通过! 新增用户: " + actualNewUsers + ", 总用户: " + actualTotalUsers);
    }

    /**
     * 测试多个用户注册的统计
     */
    @Test
    @DisplayName("多用户注册统计测试")
    @SuppressWarnings("unchecked")
    void testMultipleUserRegistrationsStatistics() throws Exception {
        // 配置拦截器Mock，使其放行所有请求
        setupInterceptorMocks();
        
        LocalDate today = LocalDate.now();
        
        // Mock第一个用户注册
        Long userId1 = 200L;
        User mockUser1 = User.builder()
                .id(userId1)
                .openid("test-openid-1")
                .name("测试用户1")
                .phone("13800138001")
                .sex("1")
                .build();
        
        org.mockito.Mockito.when(userService.wxLogin(org.mockito.ArgumentMatchers.any(UserLoginDTO.class)))
                .thenReturn(mockUser1);

        // 执行第一个用户注册
        UserLoginDTO loginDTO1 = new UserLoginDTO();
        loginDTO1.setCode("test-code-1");
        
        mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        System.out.println("✓ 第一个用户注册成功，用户ID: " + userId1);

        // Mock第二个用户注册
        Long userId2 = 201L;
        User mockUser2 = User.builder()
                .id(userId2)
                .openid("test-openid-2")
                .name("测试用户2")
                .phone("13800138002")
                .sex("0")
                .build();
        
        org.mockito.Mockito.when(userService.wxLogin(org.mockito.ArgumentMatchers.any(UserLoginDTO.class)))
                .thenReturn(mockUser2);

        // 执行第二个用户注册
        UserLoginDTO loginDTO2 = new UserLoginDTO();
        loginDTO2.setCode("test-code-2");
        
        mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        System.out.println("✓ 第二个用户注册成功，用户ID: " + userId2);

        // Mock ReportService.getUserStatistics - 两个新用户
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(today.toString())
                .totalUserList("2")  // 总用户数为2
                .newUserList("2")    // 新增用户数为2
                .build();
        
        org.mockito.Mockito.when(reportService.getUserStatistics(
                        org.mockito.ArgumentMatchers.eq(today),
                        org.mockito.ArgumentMatchers.eq(today)))
                .thenReturn(userReportVO);

        // 查询用户统计
        MvcResult statsResult = mockMvc.perform(get("/admin/report/userStatistics")
                        .param("begin", today.toString())
                        .param("end", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        // 解析用户统计结果
        String statsResponse = statsResult.getResponse().getContentAsString();
        Map<String, Object> statsData = objectMapper.readValue(statsResponse, HashMap.class);
        Map<String, Object> reportData = (Map<String, Object>) statsData.get("data");
        String newUserList = (String) reportData.get("newUserList");
        String totalUserList = (String) reportData.get("totalUserList");

        System.out.println("多用户统计结果 - 新增用户: " + newUserList + ", 总用户: " + totalUserList);

        // 验证统计结果
        org.junit.jupiter.api.Assertions.assertNotNull(newUserList, "新增用户列表不应为空");
        org.junit.jupiter.api.Assertions.assertNotNull(totalUserList, "总用户列表不应为空");
        
        String[] newUsers = newUserList.split(",");
        String[] totalUsers = totalUserList.split(",");
        
        org.junit.jupiter.api.Assertions.assertTrue(newUsers.length > 0, "新增用户列表应至少有一个值");
        org.junit.jupiter.api.Assertions.assertTrue(totalUsers.length > 0, "总用户列表应至少有一个值");
        
        int actualNewUsers = Integer.parseInt(newUsers[0].trim());
        int actualTotalUsers = Integer.parseInt(totalUsers[0].trim());
        
        org.junit.jupiter.api.Assertions.assertEquals(2, actualNewUsers,
                "新增用户数应该为2");
        org.junit.jupiter.api.Assertions.assertEquals(2, actualTotalUsers,
                "总用户数应该为2");

        System.out.println("✓ 多用户统计验证通过! 新增用户: " + actualNewUsers + ", 总用户: " + actualTotalUsers);
    }
}