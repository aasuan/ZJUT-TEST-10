package com.sky.controller.admin;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.alibaba.fastjson.JSON;
import com.sky.BaseIntegrationTest;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeeDTO;
import com.sky.service.EmployeeService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import org.hamcrest.Matchers;

/**
 * 文件上传集成测试
 * 测试场景：文件上传（用 MockMultipartFile）→ 验证返回文件路径
 */
class CommonIntegrationTest extends BaseIntegrationTest {

    private String token;

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 先检查并创建管理员用户
        try {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setName("管理员");
            employeeDTO.setUsername("admin");
            employeeDTO.setPhone("13800138000");
            employeeDTO.setSex("1");
            employeeDTO.setIdNumber("110101199001011234");
            employeeService.save(employeeDTO);
        } catch (Exception e) {
            // 用户已存在，忽略异常
        }

        // 2. 登录获取token
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        String response = mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取token
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response);
        token = jsonObject.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("文件上传测试 - JPG格式")
    void testUploadJpgFile() throws Exception {
        // 创建模拟文件
        String filename = "test_image.jpg";
        String contentType = "image/jpeg";
        byte[] content = "This is a test image content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );

        // 执行文件上传
        mockMvc.perform(multipart("/admin/common/upload")
                        .file(file)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("文件上传测试 - PNG格式")
    void testUploadPngFile() throws Exception {
        // 创建模拟文件
        String filename = "test_image.png";
        String contentType = "image/png";
        byte[] content = "This is a test PNG image content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );

        // 执行文件上传
        mockMvc.perform(multipart("/admin/common/upload")
                        .file(file)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("文件上传测试 - 大文件")
    void testUploadLargeFile() throws Exception {
        // 创建模拟大文件（5MB）
        String filename = "test_large.jpg";
        String contentType = "image/jpeg";
        byte[] content = new byte[5 * 1024 * 1024];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );

        // 执行文件上传
        mockMvc.perform(multipart("/admin/common/upload")
                        .file(file)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("文件上传测试 - 小文件")
    void testUploadSmallFile() throws Exception {
        // 创建模拟小文件（100字节）
        String filename = "test_small.jpg";
        String contentType = "image/jpeg";
        byte[] content = new byte[100];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );

        // 执行文件上传
        mockMvc.perform(multipart("/admin/common/upload")
                        .file(file)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("文件上传测试 - 验证返回路径格式")
    void testUploadAndVerifyPathFormat() throws Exception {
        // 创建模拟文件
        String filename = "test_path.jpg";
        String contentType = "image/jpeg";
        byte[] content = "Test path format".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );

        // 执行文件上传并验证返回路径格式
        mockMvc.perform(multipart("/admin/common/upload")
                        .file(file)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.data").value(Matchers.matchesPattern("http://.*/media/.*\\.(jpg|png|jpeg)")));
    }
}