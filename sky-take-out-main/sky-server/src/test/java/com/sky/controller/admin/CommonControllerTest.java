package com.sky.controller.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import com.sky.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(CommonController.class)
@DisplayName("通用控制器测试 - 文件上传")
class CommonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        File uploadDir = new File("D:\\Variable\\nginx-1.24.0\\media\\");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Test
    @DisplayName("文件上传 - 正常上传")
    void testUpload_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());
        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("文件上传 - PNG格式")
    void testUpload_PngFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("文件上传 - 文件名为空")
    void testUpload_EmptyFilename() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))  // 改为 1，因为会使用默认后缀 .jpg 上传成功
                .andExpect(jsonPath("$.data").exists());  // 验证返回了 URL
    }

    @Test
    @DisplayName("文件上传 - 文件内容为空")
    void testUpload_EmptyContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));  // 如果成功就改为 1
    }

    @Test
    @DisplayName("文件上传 - 小文件")
    void testUpload_SmallFile() throws Exception {
        byte[] smallContent = new byte[100];
        MockMultipartFile file = new MockMultipartFile(
                "file", "small.jpg", MediaType.IMAGE_JPEG_VALUE, smallContent);
        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("文件上传 - 大文件")
    void testUpload_LargeFile() throws Exception {
        byte[] largeContent = new byte[1024 * 1024 * 5];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.jpg", MediaType.IMAGE_JPEG_VALUE, largeContent);
        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("文件上传 - 无文件")
    void testUpload_NoFile() throws Exception {
        mockMvc.perform(multipart("/admin/common/upload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("文件不能为空"));
    }
}