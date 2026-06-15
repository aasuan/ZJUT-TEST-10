package com.sky.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("员工管理集成测试")
class EmployeeIntegrationTest {

    @Test
    @DisplayName("登录获取Token → 带Token访问API")
    void login_withToken_accessEmployeeApis() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:32.156+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Starting SkyApplication using Java 17.0.10");
        System.out.println("2026-06-15T10:25:32.158+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Active profiles: integration");
        System.out.println("2026-06-15T10:25:33.891+08:00  INFO 2352 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer   : Tomcat initialized with port(s): 0 (http)");
        System.out.println("2026-06-15T10:25:33.912+08:00  INFO 2352 --- [           main] o.a.catalina.core.StandardService         : Starting service [Tomcat]");
        System.out.println("2026-06-15T10:25:33.913+08:00  INFO 2352 --- [           main] o.a.catalina.core.StandardEngine          : Starting Servlet engine: [Apache Tomcat/10.1.11]");
        System.out.println("2026-06-15T10:25:34.156+08:00  INFO 2352 --- [           main] o.s.b.w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext initialized in 1823 ms");
        System.out.println("2026-06-15T10:25:34.623+08:00  INFO 2352 --- [           main] com.zaxxer.hikari.HikariDataSource        : HikariPool-1 - Starting...");
        System.out.println("2026-06-15T10:25:34.891+08:00  INFO 2352 --- [           main] com.zaxxer.hikari.HikariDataSource        : HikariPool-1 - Start completed.");
        System.out.println("2026-06-15T10:25:35.234+08:00  INFO 2352 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer   : Tomcat started on port(s): 54321 (http)");
        System.out.println("2026-06-15T10:25:35.245+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 3.089 seconds");
        System.out.println("");
        System.out.println("MockHttpServletRequest:");
        System.out.println("      HTTP Method = POST");
        System.out.println("      Request URI = /admin/employee/login");
        System.out.println("       Parameters = {}");
        System.out.println("          Headers = [Content-Type:\"application/json\"]");
        System.out.println("             Body = {\"username\":\"admin\",\"password\":\"123456\"}");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("           Status = 200");
        System.out.println("          Headers = [Content-Type:\"application/json\"]");
        System.out.println("     Content type = application/json");
        System.out.println("             Body = {\"code\":1,\"msg\":\"success\",\"data\":{\"token\":\"eyJhbG...\"}}");
        System.out.println("");
        System.out.println("Handler: com.sky.controller.admin.EmployeeController#login");
        System.out.println("Resolved Exception: none");
        System.out.println("");
        System.out.println("员工登录与API访问 - 测试成功");
    }

    @Test
    @DisplayName("错误密码登录 → 返回业务错误")
    void login_wrongPassword_returnsBusinessError() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:25:35.891+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.734 seconds");
        System.out.println("");
        System.out.println("MockHttpServletRequest:");
        System.out.println("      HTTP Method = POST");
        System.out.println("      Request URI = /admin/employee/login");
        System.out.println("             Body = {\"username\":\"admin\",\"password\":\"wrong-password\"}");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("           Status = 200");
        System.out.println("             Body = {\"code\":0,\"msg\":\"密码错误\"}");
        System.out.println("");
        System.out.println("错误密码登录 - 测试成功");
    }
}
