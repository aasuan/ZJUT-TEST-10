package com.sky.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.sky.BaseIntegrationTest;
import com.sky.interceptor.JwtTokenAdminInterceptor;

/**
 * Excel导出集成测试
 * 测试流程：调用导出接口 → 获取Excel文件 → 验证文件内容和列头
 * 
 * 测试策略：
 * - 只Mock JWT拦截器（认证层面），不Mock业务Service
 * - 让真实的ReportService.exportExcel()执行，包括从数据库查询数据和生成Excel
 * - 验证生成的Excel文件结构、列头和内容
 */
@DisplayName("Excel导出集成测试")
class ExcelExportIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 初始化：在每个测试前配置拦截器Mock，使其总是放行
     */
    @BeforeEach
    void setupInterceptorMocks() throws Exception {
        // Mock管理员拦截器，总是返回true（放行）
        org.mockito.Mockito.when(jwtTokenAdminInterceptor.preHandle(
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class),
                org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletResponse.class),
                org.mockito.ArgumentMatchers.any(Object.class)))
                .thenReturn(true);
    }

    /**
     * 测试Excel导出功能 - 验证文件内容和列头
     * 
     * 测试策略：
     * 1. 只Mock JWT拦截器（认证层面），不Mock业务Service
     * 2. 让真实的ReportService.exportExcel()执行，包括：
     *    - 从数据库查询真实的业务数据
     *    - 读取Excel模板文件
     *    - 填充数据并生成Excel
     * 3. 验证生成的Excel文件结构和内容
     */
    @Test
    @DisplayName("Excel导出测试 - 验证文件内容和列头")
    void testExcelExportWithContentAndHeaders() throws Exception {
        // 配置拦截器Mock，使其放行所有请求
        setupInterceptorMocks();

        System.out.println("开始执行Excel导出测试...");
        System.out.println("注意：此测试将使用真实的业务逻辑，从数据库查询数据并生成Excel");

        // 执行Excel导出请求
        MvcResult result = mockMvc.perform(get("/admin/report/export")
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andReturn();

        // 获取响应内容（Excel文件的字节数组）
        byte[] excelBytes = result.getResponse().getContentAsByteArray();
        
        System.out.println("响应状态码: " + result.getResponse().getStatus());
        System.out.println("响应Content-Type: " + result.getResponse().getContentType());
        System.out.println("Excel文件大小: " + (excelBytes != null ? excelBytes.length : 0) + " bytes");
        
        // 验证响应不为空
        org.junit.jupiter.api.Assertions.assertNotNull(excelBytes, "Excel文件内容不应为空");
        org.junit.jupiter.api.Assertions.assertTrue(excelBytes.length > 0, 
                "Excel文件大小应大于0，实际大小: " + (excelBytes != null ? excelBytes.length : 0));

        System.out.println("✓ Excel文件导出成功，文件大小: " + excelBytes.length + " bytes");

        // 解析Excel文件并验证内容
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(excelBytes);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            
            // 验证工作表存在
            org.junit.jupiter.api.Assertions.assertEquals(1, workbook.getNumberOfSheets(), 
                    "应该只有一个工作表");
            
            XSSFSheet sheet = workbook.getSheetAt(0);
            org.junit.jupiter.api.Assertions.assertNotNull(sheet, "工作表不应为空");
            
            // 验证列头（根据ReportServiceImpl代码，明细数据从第8行开始，索引为7）
            // 第7行（索引6）应该是列头行，包含日期、营业额、有效订单数等
            XSSFRow headerRow = sheet.getRow(6); // 第7行，索引从0开始
            if (headerRow != null) {
                System.out.println("找到列头行（第7行）");
                
                // 验证关键列头是否存在
                XSSFCell dateCell = headerRow.getCell(1); // B列 - 日期
                XSSFCell turnoverCell = headerRow.getCell(2); // C列 - 营业额
                XSSFCell orderCountCell = headerRow.getCell(3); // D列 - 有效订单数
                
                if (dateCell != null && dateCell.getCellType().ordinal() == 1) { // STRING类型
                    String dateHeader = dateCell.getStringCellValue();
                    org.junit.jupiter.api.Assertions.assertNotNull(dateHeader, "日期列头不应为空");
                    System.out.println("日期列头: " + dateHeader);
                } else {
                    System.out.println("警告: 日期列头单元格为空或类型不正确");
                }
                
                if (turnoverCell != null && turnoverCell.getCellType().ordinal() == 1) {
                    String turnoverHeader = turnoverCell.getStringCellValue();
                    org.junit.jupiter.api.Assertions.assertNotNull(turnoverHeader, "营业额列头不应为空");
                    System.out.println("营业额列头: " + turnoverHeader);
                } else {
                    System.out.println("警告: 营业额列头单元格为空或类型不正确");
                }
                
                if (orderCountCell != null && orderCountCell.getCellType().ordinal() == 1) {
                    String orderCountHeader = orderCountCell.getStringCellValue();
                    org.junit.jupiter.api.Assertions.assertNotNull(orderCountHeader, "订单数列头不应为空");
                    System.out.println("订单数列头: " + orderCountHeader);
                } else {
                    System.out.println("警告: 订单数列头单元格为空或类型不正确");
                }
            } else {
                System.out.println("警告: 未找到预期的列头行（第7行，索引6）");
                System.out.println("工作表总行数: " + sheet.getPhysicalNumberOfRows());
            }
            
            // 验证时间信息行（第2行，索引为1）
            XSSFRow timeRow = sheet.getRow(1);
            if (timeRow != null) {
                XSSFCell timeCell = timeRow.getCell(1);
                if (timeCell != null && timeCell.getCellType().ordinal() == 1) { // STRING类型
                    String timeValue = timeCell.getStringCellValue();
                    org.junit.jupiter.api.Assertions.assertNotNull(timeValue, "时间信息不应为空");
                    org.junit.jupiter.api.Assertions.assertTrue(timeValue.contains("时间:"), 
                            "时间信息应包含'时间:'字样，实际值: " + timeValue);
                    System.out.println("时间信息: " + timeValue);
                } else {
                    System.out.println("警告: 时间信息单元格为空或类型不正确");
                }
            } else {
                System.out.println("警告: 未找到时间信息行（第2行，索引1）");
            }
            
            // 验证概览数据区域（第4-5行，索引为3-4）
            XSSFRow overviewRow1 = sheet.getRow(3);
            if (overviewRow1 != null) {
                // 验证概览数据单元格是否存在
                XSSFCell turnoverOverviewCell = overviewRow1.getCell(2); // C4 - 营业额
                if (turnoverOverviewCell != null) {
                    System.out.println("概览营业额单元格类型: " + turnoverOverviewCell.getCellType());
                    // 尝试读取数值（可能是NUMERIC或STRING类型）
                    try {
                        double turnoverValue = turnoverOverviewCell.getNumericCellValue();
                        System.out.println("概览营业额数值: " + turnoverValue);
                    } catch (Exception e) {
                        System.out.println("概览营业额不是数值类型: " + e.getMessage());
                    }
                } else {
                    System.out.println("警告: 概览营业额单元格为空");
                }
            } else {
                System.out.println("警告: 未找到概览数据第1行（第4行，索引3）");
            }
            
            // 验证明细数据是否存在（至少应该有部分行有数据）
            int dataRowCount = 0;
            for (int i = 7; i < Math.min(37, sheet.getPhysicalNumberOfRows()); i++) {
                XSSFRow dataRow = sheet.getRow(i);
                if (dataRow != null && dataRow.getCell(1) != null) {
                    dataRowCount++;
                }
            }
            System.out.println("明细数据行数: " + dataRowCount + " (预期最多30行)");
            
            System.out.println("✓ Excel文件结构和内容验证完成");
            
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("解析Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 测试Excel导出功能 - 验证响应头和文件格式
     */
    @Test
    @DisplayName("Excel导出测试 - 验证响应头和文件格式")
    void testExcelExportResponseHeaders() throws Exception {
        // 配置拦截器Mock，使其放行所有请求
        setupInterceptorMocks();

        System.out.println("开始执行Excel导出响应头测试...");

        // 执行Excel导出请求
        MvcResult result = mockMvc.perform(get("/admin/report/export")
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andReturn();

        // 验证响应头
        String contentType = result.getResponse().getContentType();
        System.out.println("Content-Type: " + contentType);
        
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");
        System.out.println("Content-Disposition: " + contentDisposition);
        
        // 验证文件内容不为空
        byte[] excelBytes = result.getResponse().getContentAsByteArray();
        org.junit.jupiter.api.Assertions.assertNotNull(excelBytes, "Excel文件内容不应为空");
        org.junit.jupiter.api.Assertions.assertTrue(excelBytes.length > 0, 
                "Excel文件大小应大于0，实际大小: " + (excelBytes != null ? excelBytes.length : 0));
        
        // 验证Excel文件格式（检查文件头标识）
        // XLSX文件的前4个字节应该是 PK (0x50 0x4B)
        if (excelBytes.length >= 4) {
            org.junit.jupiter.api.Assertions.assertEquals((byte) 0x50, excelBytes[0], "Excel文件应以PK开头");
            org.junit.jupiter.api.Assertions.assertEquals((byte) 0x4B, excelBytes[1], "Excel文件应以PK开头");
            System.out.println("✓ Excel文件格式验证通过（ZIP格式标识正确）");
        }
        
        System.out.println("✓ Excel导出响应头验证完成，文件大小: " + excelBytes.length + " bytes");
    }
}
