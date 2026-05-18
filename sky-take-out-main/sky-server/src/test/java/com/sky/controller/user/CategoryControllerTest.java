package com.sky.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sky.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sky.entity.Category;
import com.sky.service.CategoryService;

@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(CategoryController.class)
@DisplayName("C端分类控制器测试")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        reset(categoryService);
    }

    @Test
    @DisplayName("查询分类 - 按类型查询菜品分类")
    void testList_ByType() throws Exception {
        List<Category> categories = new ArrayList<>();
        categories.add(Category.builder().id(1L).type(1).name("热菜").sort(1).status(1).build());
        categories.add(Category.builder().id(2L).type(1).name("凉菜").sort(2).status(1).build());

        when(categoryService.list(1)).thenReturn(categories);

        mockMvc.perform(get("/user/category/list").param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("热菜"))
                .andExpect(jsonPath("$.data[1].name").value("凉菜"));

        verify(categoryService, times(1)).list(1);
    }

    @Test
    @DisplayName("查询分类 - 无参数查询全部")
    void testList_All() throws Exception {
        when(categoryService.list(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/category/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray());

        verify(categoryService, times(1)).list(null);
    }

    @Test
    @DisplayName("查询分类 - 空结果")
    void testList_Empty() throws Exception {
        when(categoryService.list(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/category/list").param("type", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(categoryService, times(1)).list(99);
    }
}
