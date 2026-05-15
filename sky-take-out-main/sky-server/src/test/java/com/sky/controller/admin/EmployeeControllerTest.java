package com.sky.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

/**
 * 员工管理 Controller 单元测试（MockMvc + Mock Service）
 */
@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUpJwt() {
        when(jwtProperties.getAdminSecretKey()).thenReturn("abcdefghijabcdefghijabcdefghijabcdefghij");
        when(jwtProperties.getAdminTtl()).thenReturn(7200000L);
    }

    @Test
    void login_success_returnsToken() throws Exception {
        Employee employee = Employee.builder()
                .id(1L)
                .username("admin")
                .name("管理员")
                .password("x")
                .build();
        when(employeeService.login(any(EmployeeLoginDTO.class))).thenReturn(employee);

        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        mockMvc.perform(post("/admin/employee/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userName").value("admin"));
    }

    @Test
    void login_accountNotFound_returnsBusinessError() throws Exception {
        when(employeeService.login(any(EmployeeLoginDTO.class)))
                .thenThrow(new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND));

        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("nouser");
        dto.setPassword("123456");

        mockMvc.perform(post("/admin/employee/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstant.ACCOUNT_NOT_FOUND));
    }

    @Test
    void login_accountLocked_returnsBusinessError() throws Exception {
        when(employeeService.login(any(EmployeeLoginDTO.class)))
                .thenThrow(new AccountLockedException(MessageConstant.ACCOUNT_LOCKED));

        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        mockMvc.perform(post("/admin/employee/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstant.ACCOUNT_LOCKED));
    }

    @Test
    void logout_returnsSuccess() throws Exception {
        mockMvc.perform(post("/admin/employee/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void save_delegatesToService() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("zhangsan");
        dto.setName("张三");
        dto.setPhone("13800138000");

        mockMvc.perform(post("/admin/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(employeeService).save(any(EmployeeDTO.class));
    }

    @Test
    void page_returnsPageResult() throws Exception {
        PageResult<Employee> pageResult = new PageResult<>(1L, Collections.emptyList());
        when(employeeService.pageQuery(any(EmployeePageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/employee/page")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(employeeService).pageQuery(any(EmployeePageQueryDTO.class));
    }

    @Test
    void startOrStop_delegatesToService() throws Exception {
        mockMvc.perform(post("/admin/employee/status/1")
                .param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(employeeService).startOrStop(1, 5L);
    }

    @Test
    void getById_returnsEmployee() throws Exception {
        Employee employee = Employee.builder().id(3L).username("lisi").name("李四").build();
        when(employeeService.getById(3L)).thenReturn(employee);

        mockMvc.perform(get("/admin/employee/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("lisi"));

        verify(employeeService).getById(3L);
    }

    @Test
    void update_delegatesToService() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(1L);
        dto.setName("改名");

        mockMvc.perform(put("/admin/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(employeeService).update(any(EmployeeDTO.class));
    }

}
