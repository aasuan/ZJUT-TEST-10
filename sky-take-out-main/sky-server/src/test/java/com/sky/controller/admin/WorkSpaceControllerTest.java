package com.sky.controller.admin;

import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkSpaceControllerTest {

    @Mock
    private WorkspaceService workspaceService;

    @InjectMocks
    private WorkSpaceController workSpaceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workSpaceController).build();
    }

    @Test
    void testBusinessData() throws Exception {
        BusinessDataVO mockVO = new BusinessDataVO();
        mockVO.setTurnover(12345.67);
        mockVO.setValidOrderCount(50);
        mockVO.setOrderCompletionRate(0.85);
        when(workspaceService.getBusinessData(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockVO);

        mockMvc.perform(get("/admin/workspace/businessData"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.turnover").value(12345.67))
                .andExpect(jsonPath("$.data.validOrderCount").value(50));
    }

    @Test
    void testOrderOverView() throws Exception {
        OrderOverViewVO mockVO = new OrderOverViewVO();
        mockVO.setWaitingOrders(5);
        mockVO.setDeliveredOrders(20);
        when(workspaceService.getOrderOverView()).thenReturn(mockVO);

        mockMvc.perform(get("/admin/workspace/overviewOrders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.waitingOrders").value(5));
    }

    @Test
    void testDishOverView() throws Exception {
        DishOverViewVO mockVO = new DishOverViewVO();
        mockVO.setSold(100);
        mockVO.setDiscontinued(2);
        when(workspaceService.getDishOverView()).thenReturn(mockVO);

        mockMvc.perform(get("/admin/workspace/overviewDishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.sold").value(100));
    }

    @Test
    void testSetmealOverView() throws Exception {
        SetmealOverViewVO mockVO = new SetmealOverViewVO();
        mockVO.setSold(30);
        mockVO.setDiscontinued(0);
        when(workspaceService.getSetmealOverView()).thenReturn(mockVO);

        mockMvc.perform(get("/admin/workspace/overviewSetmeals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.sold").value(30));
    }
}