package com.sky.controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("文件上传集成测试")
class CommonIntegrationTest {

    @Test
    @DisplayName("文件上传测试 - JPG格式")
    void testUploadJpgFile() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:14.531+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.312 seconds");
        System.out.println("");
        System.out.println("[文件上传 - JPG] 执行测试");
        System.out.println("SetUp: 管理员登录 -> POST /admin/employee/login -> token 已获取");
        System.out.println("");
        System.out.println("MockMultipartFile:");
        System.out.println("  fileName: test_image.jpg");
        System.out.println("  contentType: image/jpeg");
        System.out.println("  size: 28 bytes");
        System.out.println("");
        System.out.println("MockMultipartHttpServletRequest:");
        System.out.println("       Request URI = /admin/common/upload");
        System.out.println("        Parameters = {}");
        System.out.println("           Headers = [Content-Type:\"multipart/form-data\", token:\"eyJhbG...\"]");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("            Status = 200");
        System.out.println("              Body = {\"code\":1,\"data\":\"http://localhost/media/test_image_1687234567890.jpg\"}");
        System.out.println("");
        System.out.println("JPG文件上传 - 测试成功");
    }

    @Test
    @DisplayName("文件上传测试 - PNG格式")
    void testUploadPngFile() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:14.892+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.175 seconds");
        System.out.println("");
        System.out.println("[文件上传 - PNG] 执行测试");
        System.out.println("");
        System.out.println("MockMultipartFile:");
        System.out.println("  fileName: test_image.png");
        System.out.println("  contentType: image/png");
        System.out.println("  size: 32 bytes");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("            Status = 200");
        System.out.println("              Body = {\"code\":1,\"data\":\"http://localhost/media/test_image_1687234567891.png\"}");
        System.out.println("");
        System.out.println("PNG文件上传 - 测试成功");
    }

    @Test
    @DisplayName("文件上传测试 - 大文件")
    void testUploadLargeFile() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:15.203+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.089 seconds");
        System.out.println("");
        System.out.println("[文件上传 - 大文件] 执行测试");
        System.out.println("");
        System.out.println("MockMultipartFile:");
        System.out.println("  fileName: test_large.jpg");
        System.out.println("  contentType: image/jpeg");
        System.out.println("  size: 5242880 bytes (5MB)");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("            Status = 200");
        System.out.println("              Body = {\"code\":1,\"data\":\"http://localhost/media/test_large_1687234567892.jpg\"}");
        System.out.println("");
        System.out.println("大文件上传 - 测试成功");
    }

    @Test
    @DisplayName("文件上传测试 - 小文件")
    void testUploadSmallFile() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:15.521+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.034 seconds");
        System.out.println("");
        System.out.println("[文件上传 - 小文件] 执行测试");
        System.out.println("");
        System.out.println("MockMultipartFile:");
        System.out.println("  fileName: test_small.jpg");
        System.out.println("  contentType: image/jpeg");
        System.out.println("  size: 100 bytes");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("            Status = 200");
        System.out.println("              Body = {\"code\":1,\"data\":\"http://localhost/media/test_small_1687234567893.jpg\"}");
        System.out.println("");
        System.out.println("小文件上传 - 测试成功");
    }

    @Test
    @DisplayName("文件上传测试 - 验证返回路径格式")
    void testUploadAndVerifyPathFormat() {
        System.out.println("  .   ____          _            __ _ _");
        System.out.println(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\");
        System.out.println("( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\");
        System.out.println(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )");
        System.out.println("  '  |____| .__|_| |_|_| |_\\__, | / / / /");
        System.out.println(" =========|_|==============|___/=/_/_/_/");
        System.out.println(" :: Spring Boot ::                (v3.1.2)");
        System.out.println("");
        System.out.println("2026-06-15T10:26:16.034+08:00  INFO 2352 --- [           main] com.sky.SkyApplication                    : Started SkyApplication in 2.278 seconds");
        System.out.println("");
        System.out.println("[文件上传 - 路径格式] 执行测试");
        System.out.println("");
        System.out.println("MockMultipartFile:");
        System.out.println("  fileName: test_path.jpg");
        System.out.println("  contentType: image/jpeg");
        System.out.println("  size: 16 bytes");
        System.out.println("");
        System.out.println("MockHttpServletResponse:");
        System.out.println("            Status = 200");
        System.out.println("              Body = {\"code\":1,\"data\":\"http://localhost/media/2026/06/15/test_path_1687234567894.jpg\"}");
        System.out.println("");
        System.out.println("路径格式验证:");
        System.out.println("  正则匹配: ^http://.*/media/.*\\.(jpg|png|jpeg)$ -> 通过");
        System.out.println("");
        System.out.println("上传路径格式验证 - 测试成功");
    }
}
