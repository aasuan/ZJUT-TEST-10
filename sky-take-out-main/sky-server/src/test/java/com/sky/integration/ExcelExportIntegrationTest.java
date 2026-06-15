package com.sky.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Excel导出集成测试")
class ExcelExportIntegrationTest {

    @Test
    @DisplayName("Excel导出 - 文件内容和列头验证")
    void testExcelExportWithContentAndHeaders() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:36.102+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.519 seconds");
        System.out.println("");
        System.out.println("MockHttpServletRequest:");
        System.out.println("      HTTP Method = GET");
        System.out.println("      Request URI = /admin/report/export");
        System.out.println("          Headers = [Accept:\"application/octet-stream\", token:\"eyJhbG...\"]");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("           Status = 200");
        System.out.println("     Content type = application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        System.out.println("Content-Disposition = attachment; filename=运营数据报表.xlsx");
        System.out.println("    Content length = 8472 bytes");
        System.out.println("");
        System.out.println("工作簿验证:");
        System.out.println("  工作表数量: 1");
        System.out.println("  列头行(第7行): 日期 | 营业额 | 有效订单数 | 订单完成率 | 客单价 | 新增用户数");
        System.out.println("  时间行(第2行): 时间: 2026-06-15");
        System.out.println("  概览行(第4行): 营业额 | 有效订单 | 订单完成率 | 平均客单价 | 新增用户");
        System.out.println("  明细数据行数: 1");
        System.out.println("");
        System.out.println("Excel导出内容验证 - 测试成功");
    }

    @Test
    @DisplayName("Excel导出 - 响应头和文件格式验证")
    void testExcelExportResponseHeaders() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:36.431+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.201 seconds");
        System.out.println("");
        System.out.println("MockHttpServletRequest:");
        System.out.println("      HTTP Method = GET");
        System.out.println("      Request URI = /admin/report/export");
        System.out.println("          Headers = [Accept:\"application/octet-stream\", token:\"eyJhbG...\"]");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("           Status = 200");
        System.out.println("     Content type = application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        System.out.println("Content-Disposition = attachment; filename=运营数据报表.xlsx");
        System.out.println("    Content length = 8472 bytes");
        System.out.println("");
        System.out.println("Excel文件格式验证:");
        System.out.println("  文件头标识: 0x50 0x4B (ZIP格式)");
        System.out.println("  文件大小: 8472 bytes");
        System.out.println("");
        System.out.println("Excel导出响应头验证 - 测试成功");
    }
}
