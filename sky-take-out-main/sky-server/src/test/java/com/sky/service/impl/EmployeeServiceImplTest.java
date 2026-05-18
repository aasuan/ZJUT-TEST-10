package com.sky.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import com.github.pagehelper.Page;

/** 员工业务层单元测试（Mock Mapper） */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private static final String MD5_123456 = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());

    @Test
    void login_success_returnsEmployee() {
        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        Employee db = Employee.builder()
                .id(10L)
                .username("admin")
                .password(MD5_123456)
                .status(StatusConstant.ENABLE)
                .build();
        when(employeeMapper.getByUsername("admin")).thenReturn(db);

        Employee result = employeeService.login(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void login_usernameNotFound_throwsAccountNotFoundException() {
        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("ghost");
        dto.setPassword("123456");
        when(employeeMapper.getByUsername("ghost")).thenReturn(null);

        AccountNotFoundException ex = assertThrows(AccountNotFoundException.class, () -> employeeService.login(dto));
        assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void login_wrongPassword_throwsPasswordErrorException() {
        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong-password");

        Employee db = Employee.builder()
                .username("admin")
                .password(MD5_123456)
                .status(StatusConstant.ENABLE)
                .build();
        when(employeeMapper.getByUsername("admin")).thenReturn(db);

        PasswordErrorException ex = assertThrows(PasswordErrorException.class, () -> employeeService.login(dto));
        assertEquals(MessageConstant.PASSWORD_ERROR, ex.getMessage());
    }

    @Test
    void login_accountDisabled_throwsAccountLockedException() {
        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        Employee db = Employee.builder()
                .username("admin")
                .password(MD5_123456)
                .status(StatusConstant.DISABLE)
                .build();
        when(employeeMapper.getByUsername("admin")).thenReturn(db);

        AccountLockedException ex = assertThrows(AccountLockedException.class, () -> employeeService.login(dto));
        assertEquals(MessageConstant.ACCOUNT_LOCKED, ex.getMessage());
    }

    @Test
    void save_hashesDefaultPasswordAndInserts() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("newuser");
        dto.setName("新员工");
        dto.setPhone("13900000000");

        employeeService.save(dto);

        verify(employeeMapper).insert(any(Employee.class));
    }

    @Test
    void pageQuery_returnsPageResult() {
        EmployeePageQueryDTO query = new EmployeePageQueryDTO();
        query.setPage(1);
        query.setPageSize(10);

        Page<Employee> page = new Page<>(1, 10);
        page.setTotal(2);
        page.add(Employee.builder().id(1L).name("A").build());
        page.add(Employee.builder().id(2L).name("B").build());
        when(employeeMapper.pageQuery(query)).thenReturn(page);

        PageResult<Employee> result = employeeService.pageQuery(query);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getRecords().size());
    }

    @Test
    void startOrStop_updatesMapper() {
        employeeService.startOrStop(0, 99L);
        verify(employeeMapper).update(any(Employee.class));
    }

    @Test
    void getById_delegatesToMapper() {
        when(employeeMapper.getById(5L)).thenReturn(Employee.builder().id(5L).username("u").build());
        assertEquals("u", employeeService.getById(5L).getUsername());
    }

    @Test
    void update_delegatesToMapper() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(1L);
        dto.setName("改");
        employeeService.update(dto);
        verify(employeeMapper).update(any(Employee.class));
    }
}
