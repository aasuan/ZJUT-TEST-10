package com.sky;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 集成测试基类
 * <p>
 * 使用方式：各模块集成测试类继承此类即可。
 * 连接 Docker Compose 启动的 MySQL + Redis（profile=integration）。
 * <p>
 * 启动前确保已执行: docker compose up -d
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.profiles.active=integration",
        "spring.profiles.include=",
        "spring.sql.init.mode=never"
})
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

}
