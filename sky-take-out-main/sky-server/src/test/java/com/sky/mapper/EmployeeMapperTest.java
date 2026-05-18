package com.sky.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

/**
 * 员工 Mapper 与 SQL 单元测试（H2 内存库）
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.profiles.active=test")
class EmployeeMapperTest {

    @Autowired
    private EmployeeMapper employeeMapper;

    private Employee newEmployee(String username, String name) {
        return Employee.builder()
                .username(username)
                .name(name)
                .password("pwd")
                .phone("13800000000")
                .sex("1")
                .idNumber("320000000000000000")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createUser(1L)
                .updateUser(1L)
                .build();
    }

    @Test
    void getByUsername_returnsRow() {
        employeeMapper.insert(newEmployee("mapper_user", "测试"));

        Employee found = employeeMapper.getByUsername("mapper_user");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("测试");
    }

    @Test
    void getByUsername_returnsNullWhenMissing() {
        assertThat(employeeMapper.getByUsername("not_exist_xxx")).isNull();
    }

    @Test
    void insert_duplicateUsername_throws() {
        employeeMapper.insert(newEmployee("dup_user", "A"));
        assertThrows(DataIntegrityViolationException.class,
                () -> employeeMapper.insert(newEmployee("dup_user", "B")));
    }

    @Test
    void pageQuery_filtersByNameLike() {
        employeeMapper.insert(newEmployee("p1", "张三"));
        employeeMapper.insert(newEmployee("p2", "张三三"));
        employeeMapper.insert(newEmployee("p3", "李四"));

        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setName("张");

        Page<Employee> page = employeeMapper.pageQuery(dto);
        // 未经过 PageHelper 时 total 可能为 0，只校验 SQL 条件与结果条数
        assertThat(page).hasSize(2);
    }

    @Test
    void getById_roundTrip() {
        employeeMapper.insert(newEmployee("byid_user", "按ID查"));
        long id = employeeMapper.getByUsername("byid_user").getId();

        Employee loaded = employeeMapper.getById(id);
        assertThat(loaded.getUsername()).isEqualTo("byid_user");
    }

    @Test
    void update_changesFields() {
        employeeMapper.insert(newEmployee("upd_user", "原名"));
        long id = employeeMapper.getByUsername("upd_user").getId();

        Employee patch = Employee.builder()
                .id(id)
                .name("新名")
                .build();
        employeeMapper.update(patch);

        Employee loaded = employeeMapper.getById(id);
        assertThat(loaded.getName()).isEqualTo("新名");
        assertThat(loaded.getUsername()).isEqualTo("upd_user");
    }
}
