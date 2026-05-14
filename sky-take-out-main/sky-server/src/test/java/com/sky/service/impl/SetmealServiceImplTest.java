package com.sky.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

@ExtendWith(MockitoExtension.class)
@DisplayName("套餐服务实现类测试")
class SetmealServiceImplTest {

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private SetmealDishMapper setmealDishMapper;

    @Mock
    private DishMapper dishMapper;

    @InjectMocks
    private SetmealServiceImpl setmealService;

    private SetmealDTO setmealDTO;
    private Setmeal setmeal;
    private SetmealVO setmealVO;

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

        setmeal = new Setmeal();
        setmeal.setId(1L);
        setmeal.setName("测试套餐");
        setmeal.setCategoryId(1L);
        setmeal.setPrice(new BigDecimal("100.00"));
        setmeal.setStatus(StatusConstant.DISABLE);

        setmealVO = new SetmealVO();
        setmealVO.setId(1L);
        setmealVO.setName("测试套餐");
        setmealVO.setCategoryId(1L);
        setmealVO.setCategoryName("测试分类");
        setmealVO.setPrice(new BigDecimal("100.00"));
        setmealVO.setStatus(StatusConstant.DISABLE);
        setmealVO.setSetmealDishes(dishes);
    }

    @Test
    @DisplayName("起售套餐 - 所有菜品都已启售")
    void testStartOrStop_AllDishesEnabled() {
        // 准备数据：所有菜品都是启用状态
        List<Dish> dishList = Arrays.asList(
                Dish.builder().id(1L).status(StatusConstant.ENABLE).build(),
                Dish.builder().id(2L).status(StatusConstant.ENABLE).build()
        );

        when(dishMapper.getBySetmealId(1L)).thenReturn(dishList);
        doNothing().when(setmealMapper).update(any(Setmeal.class));

        // 执行测试：应该正常执行，不抛出异常
        assertDoesNotThrow(() -> setmealService.startOrStop(StatusConstant.ENABLE, 1L));
    }

    @Test
    @DisplayName("起售套餐 - 包含未启售菜品")
    void testStartOrStop_HasDisabledDish() {
        // 准备数据：包含停用状态的菜品
        List<Dish> dishList = Arrays.asList(
                Dish.builder().id(1L).status(StatusConstant.ENABLE).build(),
                Dish.builder().id(2L).status(StatusConstant.DISABLE).build()  // 停用状态
        );

        when(dishMapper.getBySetmealId(1L)).thenReturn(dishList);

        // 执行测试：应该抛出 SetmealEnableFailedException
        assertThrows(SetmealEnableFailedException.class, 
                () -> setmealService.startOrStop(StatusConstant.ENABLE, 1L));
    }

    @Test
    @DisplayName("起售套餐 - 所有菜品都未启售")
    void testStartOrStop_AllDishesDisabled() {
        // 准备数据：所有菜品都是停用状态
        List<Dish> dishList = Arrays.asList(
                Dish.builder().id(1L).status(StatusConstant.DISABLE).build(),
                Dish.builder().id(2L).status(StatusConstant.DISABLE).build()
        );

        when(dishMapper.getBySetmealId(1L)).thenReturn(dishList);

        // 执行测试：应该抛出 SetmealEnableFailedException
        assertThrows(SetmealEnableFailedException.class, 
                () -> setmealService.startOrStop(StatusConstant.ENABLE, 1L));
    }

    @Test
    @DisplayName("起售套餐 - 套餐无菜品")
    void testStartOrStop_NoDishes() {
        // 准备数据：套餐没有关联菜品
        when(dishMapper.getBySetmealId(1L)).thenReturn(new ArrayList<>());
        doNothing().when(setmealMapper).update(any(Setmeal.class));

        // 执行测试：应该正常执行，不抛出异常
        assertDoesNotThrow(() -> setmealService.startOrStop(StatusConstant.ENABLE, 1L));
    }

    @Test
    @DisplayName("起售套餐 - 菜品列表为null")
    void testStartOrStop_DishListNull() {
        // 准备数据：菜品列表为null
        when(dishMapper.getBySetmealId(1L)).thenReturn(null);
        doNothing().when(setmealMapper).update(any(Setmeal.class));

        // 执行测试：应该正常执行，不抛出异常
        assertDoesNotThrow(() -> setmealService.startOrStop(StatusConstant.ENABLE, 1L));
    }

    @Test
    @DisplayName("停用套餐 - 正常停用")
    void testStartOrStop_DisableSuccess() {
        doNothing().when(setmealMapper).update(any(Setmeal.class));

        // 执行测试：停用不需要检查菜品状态，应该正常执行
        assertDoesNotThrow(() -> setmealService.startOrStop(StatusConstant.DISABLE, 1L));
    }

    @Test
    @DisplayName("停用套餐 - 包含未启售菜品")
    void testStartOrStop_DisableWithDisabledDish() {
        assertDoesNotThrow(() -> setmealService.startOrStop(StatusConstant.DISABLE, 1L));
    }

    @Test
    @DisplayName("新增套餐 - 正常新增")
    void testSaveWithDish_Success() {
        doNothing().when(setmealMapper).insert(any(Setmeal.class));
        doNothing().when(setmealDishMapper).insertBatch(anyList());

        assertDoesNotThrow(() -> setmealService.saveWithDish(setmealDTO));
    }

    @Test
    @DisplayName("新增套餐 - 无菜品")
    void testSaveWithDish_NoDishes() {
        setmealDTO.setSetmealDishes(new ArrayList<>());
        doNothing().when(setmealMapper).insert(any(Setmeal.class));
        doNothing().when(setmealDishMapper).insertBatch(anyList());

        assertDoesNotThrow(() -> setmealService.saveWithDish(setmealDTO));
    }

    @Test
    @DisplayName("删除套餐 - 正常删除")
    void testDeleteBatch_Success() {
        when(setmealMapper.getById(1L)).thenReturn(setmeal);
        doNothing().when(setmealDishMapper).deleteBySetmealId(anyLong());
        doNothing().when(setmealMapper).deleteById(anyLong());

        assertDoesNotThrow(() -> setmealService.deleteBatch(Arrays.asList(1L)));
    }

    @Test
    @DisplayName("删除套餐 - 删除在售套餐")
    void testDeleteBatch_OnSale() {
        setmeal.setStatus(StatusConstant.ENABLE);
        when(setmealMapper.getById(1L)).thenReturn(setmeal);

        assertThrows(DeletionNotAllowedException.class, 
                () -> setmealService.deleteBatch(Arrays.asList(1L)));
    }

    @Test
    @DisplayName("修改套餐 - 正常修改")
    void testUpdate_Success() {
        doNothing().when(setmealMapper).update(any(Setmeal.class));
        doNothing().when(setmealDishMapper).deleteBySetmealId(anyLong());
        doNothing().when(setmealDishMapper).insertBatch(anyList());

        assertDoesNotThrow(() -> setmealService.update(setmealDTO));
    }

    @Test
    @DisplayName("根据id查询套餐 - 正常查询")
    void testGetById_Success() {
        when(setmealMapper.getById(1L)).thenReturn(setmeal);
        when(setmealDishMapper.getDishBySetmealId(1L)).thenReturn(new ArrayList<>());

        SetmealVO result = setmealService.getById(1L);
        
        assert result != null;
        assert result.getId().equals(1L);
    }

    @Test
    @DisplayName("分页查询 - 正常查询")
    void testPageQuery_Success() {
        Page<SetmealVO> page = new Page<>();
        page.add(setmealVO);
        page.setTotal(10);

        PageHelper.startPage(1, 10);
        when(setmealMapper.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(page);

        PageResult<SetmealVO> result = setmealService.pageQuery(new SetmealPageQueryDTO());
        
        assert result != null;
        assert result.getTotal() == 10;
        assert result.getRecords().size() == 1;
    }
}