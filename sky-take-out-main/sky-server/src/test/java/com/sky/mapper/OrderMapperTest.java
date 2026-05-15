package com.sky.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;

/**
 * M5 — OrderMapper 单元测试（@MybatisTest + H2）。
 * 覆盖：pageQuery 动态条件、countStatus、getByStatusAndOrderTimeLT。
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(locations = "classpath:application.properties")
@Import(PageHelperAutoConfiguration.class)
@Transactional
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    private final LocalDateTime t2024 = LocalDateTime.of(2024, 6, 1, 10, 0, 0);
    private final LocalDateTime t2024Later = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
    private final LocalDateTime t2025 = LocalDateTime.of(2025, 1, 10, 9, 0, 0);

    @Test
    void pageQuery_noCondition_orderByOrderTimeDesc() {
        insert(buildOrder("N-A", Orders.TO_BE_CONFIRMED, "13900000001", t2024));
        insert(buildOrder("N-B", Orders.CONFIRMED, "13900000002", t2025));

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).hasSize(2);
        assertThat(page.get(0).getOrderTime()).isAfter(page.get(1).getOrderTime());
    }

    @Test
    void pageQuery_byNumberLike() {
        insert(buildOrder("SKY-1001", Orders.TO_BE_CONFIRMED, "13800000000", t2024));
        insert(buildOrder("OTHER-9", Orders.TO_BE_CONFIRMED, "13800000000", t2024Later));

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        dto.setNumber("SKY");
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).extracting(Orders::getNumber).containsExactly("SKY-1001");
    }

    @Test
    void pageQuery_byPhoneLike() {
        insert(buildOrder("P1", Orders.TO_BE_CONFIRMED, "13911112222", t2024));
        insert(buildOrder("P2", Orders.TO_BE_CONFIRMED, "13900001111", t2024Later));

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        dto.setPhone("222");
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).hasSize(1);
        assertThat(page.get(0).getNumber()).isEqualTo("P1");
    }

    @Test
    void pageQuery_byStatus() {
        insert(buildOrder("S1", Orders.TO_BE_CONFIRMED, "13800000001", t2024));
        insert(buildOrder("S2", Orders.CONFIRMED, "13800000002", t2024Later));

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        dto.setStatus(Orders.CONFIRMED);
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).extracting(Orders::getNumber).containsExactly("S2");
    }

    @Test
    void pageQuery_byUserId() {
        Orders a = buildOrder("UID-A", Orders.TO_BE_CONFIRMED, "13200000001", t2024);
        a.setUserId(100L);
        insert(a);
        Orders b = buildOrder("UID-B", Orders.TO_BE_CONFIRMED, "13200000002", t2024Later);
        b.setUserId(200L);
        insert(b);

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        dto.setUserId(100L);
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).extracting(Orders::getNumber).containsExactly("UID-A");
    }

    @Test
    void pageQuery_byBeginEndTime() {
        insert(buildOrder("E1", Orders.TO_BE_CONFIRMED, "13800000001", t2024));
        insert(buildOrder("E2", Orders.TO_BE_CONFIRMED, "13800000002", t2025));

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        dto.setBeginTime(LocalDateTime.of(2024, 5, 1, 0, 0));
        dto.setEndTime(LocalDateTime.of(2024, 12, 31, 23, 59, 59));
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).extracting(Orders::getNumber).containsExactlyInAnyOrder("E1");
    }

    @Test
    void pageQuery_combinedConditions() {
        insert(buildOrder("C1", Orders.TO_BE_CONFIRMED, "13700001111", t2024));
        insert(buildOrder("C2", Orders.TO_BE_CONFIRMED, "13700002222", t2024Later));
        insert(buildOrder("C3", Orders.CONFIRMED, "13700001111", t2024));

        PageHelper.startPage(1, 10);
        OrdersPageQueryDTO dto = baseQueryDto();
        dto.setPhone("1111");
        dto.setStatus(Orders.TO_BE_CONFIRMED);
        dto.setBeginTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        dto.setEndTime(LocalDateTime.of(2024, 12, 31, 23, 59, 59));
        Page<Orders> page = orderMapper.pageQuery(dto);

        assertThat(page).hasSize(1);
        assertThat(page.get(0).getNumber()).isEqualTo("C1");
    }

    @Test
    void getByStatusAndOrderTimeLT_returnsOnlyOldPendingPayment() {
        LocalDateTime old = LocalDateTime.now().minusMinutes(20);
        LocalDateTime recent = LocalDateTime.now().minusMinutes(5);
        insert(buildOrder("T-OLD", Orders.PENDING_PAYMENT, "13600000001", old));
        insert(buildOrder("T-NEW", Orders.PENDING_PAYMENT, "13600000002", recent));

        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));

        assertThat(list).extracting(Orders::getNumber).containsExactly("T-OLD");
    }

    @Test
    void countStatus_countsByStatus() {
        insert(buildOrder("K1", Orders.TO_BE_CONFIRMED, "13500000001", t2024));
        insert(buildOrder("K2", Orders.TO_BE_CONFIRMED, "13500000002", t2024Later));
        insert(buildOrder("K3", Orders.CONFIRMED, "13500000003", t2024));

        assertThat(orderMapper.countStatus(Orders.TO_BE_CONFIRMED)).isEqualTo(2);
        assertThat(orderMapper.countStatus(Orders.CONFIRMED)).isEqualTo(1);
    }

    @Test
    void getById_and_getByNumber() {
        Orders o = buildOrder("NUM-1", Orders.DELIVERY_IN_PROGRESS, "13400000001", t2024);
        insert(o);
        Long id = o.getId();

        Orders byId = orderMapper.getById(id);
        assertThat(byId.getNumber()).isEqualTo("NUM-1");

        Orders byNum = orderMapper.getByNumber("NUM-1");
        assertThat(byNum.getId()).isEqualTo(id);
    }

    private OrdersPageQueryDTO baseQueryDto() {
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);
        return dto;
    }

    private Orders buildOrder(String number, Integer status, String phone, LocalDateTime orderTime) {
        Orders o = new Orders();
        o.setNumber(number);
        o.setStatus(status);
        o.setPhone(phone);
        o.setOrderTime(orderTime);
        o.setUserId(1L);
        o.setAddressBookId(1L);
        o.setPayStatus(Orders.UN_PAID);
        o.setAmount(new BigDecimal("10.00"));
        o.setConsignee("test");
        o.setAddress("addr");
        return o;
    }

    private void insert(Orders o) {
        orderMapper.insert(o);
    }
}
