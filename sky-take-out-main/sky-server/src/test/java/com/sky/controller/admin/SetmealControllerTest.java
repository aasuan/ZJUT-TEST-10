package com.sky.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sky.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.alibaba.fastjson.JSON;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(SetmealController.class)
@DisplayName("套餐管理控制器测试")
class SetmealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SetmealService setmealService;

    private SetmealDTO setmealDTO;
    private SetmealVO setmealVO;
    private PageResult<SetmealVO> pageResult;

    @BeforeEach
    void setUp() {
        setmealDTO = new SetmealDTO();
        setmealDTO.setId(1L);
        setmealDTO.setName("测试套餐");
        setmealDTO.setCategoryId(1L);
        setmealDTO.setPrice(new BigDecimal("100.00"));
        setmealDTO.setStatus(1);
        setmealDTO.setDescription("测试套餐描述");
        setmealDTO.setImage("http://localhost/media/test.jpg");
        
        List<SetmealDish> dishes = new ArrayList<>();
        SetmealDish dish = new SetmealDish();
        dish.setDishId(1L);
        dish.setName("菜品1");
        dish.setCopies(2);
        dishes.add(dish);
        setmealDTO.setSetmealDishes(dishes);

        setmealVO = new SetmealVO();
        setmealVO.setId(1L);
        setmealVO.setName("测试套餐");
        setmealVO.setCategoryId(1L);
        setmealVO.setCategoryName("测试分类");
        setmealVO.setPrice(new BigDecimal("100.00"));
        setmealVO.setStatus(1);
        setmealVO.setDescription("测试套餐描述");
        setmealVO.setImage("http://localhost/media/test.jpg");
        setmealVO.setUpdateTime(LocalDateTime.now());
        setmealVO.setSetmealDishes(dishes);

        pageResult = new PageResult<>();
        pageResult.setTotal(10L);
        pageResult.setRecords(Arrays.asList(setmealVO));
    }

    @Test
    @DisplayName("新增套餐 - 正常输入")
    void testSave_Success() throws Exception {
        doNothing().when(setmealService).saveWithDish(any(SetmealDTO.class));

        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("新增套餐 - 名称为空")
    void testSave_EmptyName() throws Exception {
        setmealDTO.setName("");
        doNothing().when(setmealService).saveWithDish(any(SetmealDTO.class));

        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("新增套餐 - 价格为负数")
    void testSave_NegativePrice() throws Exception {
        setmealDTO.setPrice(new BigDecimal("-10.00"));
        doNothing().when(setmealService).saveWithDish(any(SetmealDTO.class));

        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("新增套餐 - 套餐菜品为空")
    void testSave_EmptyDishes() throws Exception {
        setmealDTO.setSetmealDishes(new ArrayList<>());
        doNothing().when(setmealService).saveWithDish(any(SetmealDTO.class));

        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("分页查询 - 正常查询")
    void testPage_Success() throws Exception {
        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "测试")
                        .param("categoryId", "1")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.records[0].name").value("测试套餐"));
    }

    @Test
    @DisplayName("分页查询 - 边界值：page=0")
    void testPage_PageZero() throws Exception {
        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("分页查询 - 边界值：pageSize=1")
    void testPage_PageSizeOne() throws Exception {
        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("分页查询 - 边界值：pageSize=100")
    void testPage_PageSizeMax() throws Exception {
        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("分页查询 - 无查询条件")
    void testPage_NoCondition() throws Exception {
        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("根据id查询套餐 - 正常查询")
    void testGetById_Success() throws Exception {
        when(setmealService.getById(1L)).thenReturn(setmealVO);

        mockMvc.perform(get("/admin/setmeal/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试套餐"));
    }

    @Test
    @DisplayName("根据id查询套餐 - id为负数")
    void testGetById_NegativeId() throws Exception {
        when(setmealService.getById(-1L)).thenReturn(null);

        mockMvc.perform(get("/admin/setmeal/{id}", -1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("根据id查询套餐 - id为0")
    void testGetById_ZeroId() throws Exception {
        when(setmealService.getById(0L)).thenReturn(null);

        mockMvc.perform(get("/admin/setmeal/{id}", 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("根据id查询套餐 - 不存在的id")
    void testGetById_NotFound() throws Exception {
        when(setmealService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/admin/setmeal/{id}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("修改套餐 - 正常修改")
    void testUpdate_Success() throws Exception {
        doNothing().when(setmealService).update(any(SetmealDTO.class));

        mockMvc.perform(put("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("修改套餐 - 修改状态为停用")
    void testUpdate_StatusDisable() throws Exception {
        setmealDTO.setStatus(0);
        doNothing().when(setmealService).update(any(SetmealDTO.class));

        mockMvc.perform(put("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("修改套餐 - 修改为空名称")
    void testUpdate_EmptyName() throws Exception {
        setmealDTO.setName("");
        doNothing().when(setmealService).update(any(SetmealDTO.class));

        mockMvc.perform(put("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(setmealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("删除套餐 - 正常删除单个")
    void testDelete_Success() throws Exception {
        doNothing().when(setmealService).deleteBatch(anyList());

        mockMvc.perform(delete("/admin/setmeal")
                        .param("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("删除套餐 - 批量删除")
    void testDelete_Batch() throws Exception {
        doNothing().when(setmealService).deleteBatch(anyList());

        mockMvc.perform(delete("/admin/setmeal")
                        .param("ids", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("删除套餐 - 空列表")
    void testDelete_EmptyList() throws Exception {
        doNothing().when(setmealService).deleteBatch(anyList());

        mockMvc.perform(delete("/admin/setmeal")
                        .param("ids", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("启用套餐 - 正常启用")
    void testStartOrStop_EnableSuccess() throws Exception {
        doNothing().when(setmealService).startOrStop(1, 1L);

        mockMvc.perform(post("/admin/setmeal/status/{status}", 1)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("停用套餐 - 正常停用")
    void testStartOrStop_DisableSuccess() throws Exception {
        doNothing().when(setmealService).startOrStop(0, 1L);

        mockMvc.perform(post("/admin/setmeal/status/{status}", 0)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("启用套餐 - 状态参数异常")
    void testStartOrStop_InvalidStatus() throws Exception {
        doNothing().when(setmealService).startOrStop(2, 1L);

        mockMvc.perform(post("/admin/setmeal/status/{status}", 2)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
}