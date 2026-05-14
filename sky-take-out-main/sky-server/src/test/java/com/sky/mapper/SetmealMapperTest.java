package com.sky.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;

@MybatisTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("套餐Mapper测试")
class SetmealMapperTest {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    private Setmeal setmeal;
    private SetmealDish setmealDish;

    @BeforeEach
    void setUp() {
        setmeal = new Setmeal();
        setmeal.setName("测试套餐");
        setmeal.setCategoryId(1L);
        setmeal.setPrice(new BigDecimal("100.00"));
        setmeal.setImage("http://localhost/media/test.jpg");
        setmeal.setDescription("测试套餐描述");
        setmeal.setStatus(StatusConstant.DISABLE);
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setCreateUser(1L);
        setmeal.setUpdateUser(1L);

        setmealDish = new SetmealDish();
        setmealDish.setSetmealId(1L);
        setmealDish.setDishId(1L);
        setmealDish.setName("测试菜品");
        setmealDish.setPrice(new BigDecimal("50.00"));
        setmealDish.setCopies(2);
    }

    @Test
    @DisplayName("插入套餐")
    void testInsert() {
        setmealMapper.insert(setmeal);
        assertNotNull(setmeal.getId());
    }

    @Test
    @DisplayName("根据id查询套餐")
    void testGetById() {
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();

        Setmeal result = setmealMapper.getById(id);
        assertNotNull(result);
        assertEquals("测试套餐", result.getName());
    }

    @Test
    @DisplayName("修改套餐")
    void testUpdate() {
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();

        Setmeal updateSetmeal = new Setmeal();
        updateSetmeal.setId(id);
        updateSetmeal.setName("修改后的套餐");
        updateSetmeal.setPrice(new BigDecimal("150.00"));
        updateSetmeal.setStatus(StatusConstant.ENABLE);

        setmealMapper.update(updateSetmeal);

        Setmeal result = setmealMapper.getById(id);
        assertEquals("修改后的套餐", result.getName());
        assertEquals(new BigDecimal("150.00"), result.getPrice());
    }

    @Test
    @DisplayName("删除套餐")
    void testDeleteById() {
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();

        setmealMapper.deleteById(id);

        Setmeal result = setmealMapper.getById(id);
        assertEquals(null, result);
    }

    @Test
    @DisplayName("批量插入套餐菜品关联")
    void testInsertBatchSetmealDish() {
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();

        SetmealDish dish1 = new SetmealDish();
        dish1.setSetmealId(setmealId);
        dish1.setDishId(1L);
        dish1.setName("菜品1");
        dish1.setPrice(new BigDecimal("30.00"));
        dish1.setCopies(2);

        SetmealDish dish2 = new SetmealDish();
        dish2.setSetmealId(setmealId);
        dish2.setDishId(2L);
        dish2.setName("菜品2");
        dish2.setPrice(new BigDecimal("50.00"));
        dish2.setCopies(3);

        setmealDishMapper.insertBatch(Arrays.asList(dish1, dish2));

        List<SetmealDish> result = setmealDishMapper.getDishBySetmealId(setmealId);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据套餐id查询套餐菜品关联")
    void testGetDishBySetmealId() {
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();

        SetmealDish dish1 = new SetmealDish();
        dish1.setSetmealId(setmealId);
        dish1.setDishId(1L);
        dish1.setName("菜品1");
        dish1.setPrice(new BigDecimal("30.00"));
        dish1.setCopies(2);

        SetmealDish dish2 = new SetmealDish();
        dish2.setSetmealId(setmealId);
        dish2.setDishId(2L);
        dish2.setName("菜品2");
        dish2.setPrice(new BigDecimal("50.00"));
        dish2.setCopies(3);

        setmealDishMapper.insertBatch(Arrays.asList(dish1, dish2));

        List<SetmealDish> result = setmealDishMapper.getDishBySetmealId(setmealId);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据套餐id删除套餐菜品关联")
    void testDeleteBySetmealId() {
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();

        SetmealDish dish1 = new SetmealDish();
        dish1.setSetmealId(setmealId);
        dish1.setDishId(1L);
        dish1.setName("菜品1");
        dish1.setPrice(new BigDecimal("30.00"));
        dish1.setCopies(2);

        SetmealDish dish2 = new SetmealDish();
        dish2.setSetmealId(setmealId);
        dish2.setDishId(2L);
        dish2.setName("菜品2");
        dish2.setPrice(new BigDecimal("50.00"));
        dish2.setCopies(3);

        setmealDishMapper.insertBatch(Arrays.asList(dish1, dish2));

        List<SetmealDish> beforeDelete = setmealDishMapper.getDishBySetmealId(setmealId);
        assertEquals(2, beforeDelete.size());

        setmealDishMapper.deleteBySetmealId(setmealId);

        List<SetmealDish> afterDelete = setmealDishMapper.getDishBySetmealId(setmealId);
        assertEquals(0, afterDelete.size());
    }
}