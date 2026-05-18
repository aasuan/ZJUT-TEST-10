package com.sky.controller.admin;

import com.sky.service.ReportService;
import com.sky.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    void testTurnoverStatistics() throws Exception {
        TurnoverReportVO mockVO = new TurnoverReportVO();
        mockVO.setDateList("2024-01-01,2024-01-02");
        mockVO.setTurnoverList("100.0,200.0");
        when(reportService.getTurnoverReport(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)))
                .thenReturn(mockVO);

        mockMvc.perform(get("/admin/report/turnoverStatistics")
                        .param("begin", "2024-01-01")
                        .param("end", "2024-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dateList").value("2024-01-01,2024-01-02"))
                .andExpect(jsonPath("$.data.turnoverList").value("100.0,200.0"));
    }

    @Test
    void testUserStatistics() throws Exception {
        UserReportVO mockVO = new UserReportVO();
        mockVO.setDateList("2024-01-01,2024-01-02");
        mockVO.setNewUserList("5,10");
        mockVO.setTotalUserList("100,110");
        when(reportService.getUserStatistics(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)))
                .thenReturn(mockVO);

        mockMvc.perform(get("/admin/report/userStatistics")
                        .param("begin", "2024-01-01")
                        .param("end", "2024-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.newUserList").value("5,10"));
    }

    @Test
    void testOrderStatistics() throws Exception {
        OrderReportVO mockVO = new OrderReportVO();
        mockVO.setDateList("2024-01-01,2024-01-02");
        mockVO.setOrderCountList("10,20");
        mockVO.setValidOrderCountList("8,18");
        when(reportService.getOrderStatistics(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)))
                .thenReturn(mockVO);

        mockMvc.perform(get("/admin/report/ordersStatistics")
                        .param("begin", "2024-01-01")
                        .param("end", "2024-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.orderCountList").value("10,20"));
    }

    @Test
    void testTop10() throws Exception {
        SalesTop10ReportVO mockVO = new SalesTop10ReportVO();
        mockVO.setNameList("菜品A,菜品B");
        mockVO.setNumberList("10,5");
        when(reportService.getSalesTop10(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)))
                .thenReturn(mockVO);

        mockMvc.perform(get("/admin/report/top10")
                        .param("begin", "2024-01-01")
                        .param("end", "2024-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.nameList").value("菜品A,菜品B"));
    }

    @Test
    void testExportExcel() throws Exception {
        doNothing().when(reportService).exportExcel(any());

        MvcResult result = mockMvc.perform(get("/admin/report/export"))
                .andExpect(status().isOk())
                .andReturn();

        // 可选：验证响应头（如果 Service 设置了 Content-Disposition）
        String disposition = result.getResponse().getHeader("Content-Disposition");
        if (disposition != null) {
            assertThat(disposition).contains("attachment;filename=");
        }

        verify(reportService, times(1)).exportExcel(any());
    }

    @Test
    void testTurnoverStatistics_invalidDateFormat() throws Exception {
        mockMvc.perform(get("/admin/report/turnoverStatistics")
                        .param("begin", "2024/01/01")   // 错误格式
                        .param("end", "2024-01-02"))
                .andExpect(status().isBadRequest());
    }
}