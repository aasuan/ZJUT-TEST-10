package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.*;
import com.sky.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GlobalExceptionHandler 单元测试
 *
 * 职责：
 * 逐一验证所有 BaseException 子类的处理逻辑。
 *
 * 注意：不使用 @SpringBootTest，直接实例化 Handler 以避免 WebSocket 启动冲突。
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ==================== 核心测试：有参构造，消息非空 ====================

    @Test
    @DisplayName("测试 AccountLockedException - 账号被锁定")
    public void testAccountLockedException() {
        // 使用常量（假设 MessageConstant.ACCOUNT_LOCKED 存在）
        BaseException ex = new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(MessageConstant.ACCOUNT_LOCKED, result.getMsg());
    }

    @Test
    @DisplayName("测试 AccountNotFoundException - 账号不存在")
    public void testAccountNotFoundException() {
        BaseException ex = new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, result.getMsg());
    }

    @Test
    @DisplayName("测试 AddressBookBusinessException - 地址簿业务异常")
    public void testAddressBookBusinessException() {
        // 业务代码中使用的实际消息（硬编码）
        String expectedMsg = "地址簿不能为空";
        BaseException ex = new AddressBookBusinessException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 DeletionNotAllowedException - 删除不允许")
    public void testDeletionNotAllowedException() {
        String expectedMsg = "该分类下关联了菜品，无法删除";
        BaseException ex = new DeletionNotAllowedException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 LoginFailedException - 登录失败")
    public void testLoginFailedException() {
        String expectedMsg = "登录失败";
        BaseException ex = new LoginFailedException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 OrderBusinessException - 订单业务异常")
    public void testOrderBusinessException() {
        String expectedMsg = "订单状态不正确";
        BaseException ex = new OrderBusinessException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 PasswordEditFailedException - 密码修改失败")
    public void testPasswordEditFailedException() {
        String expectedMsg = "密码修改失败";
        BaseException ex = new PasswordEditFailedException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 PasswordErrorException - 密码错误")
    public void testPasswordErrorException() {
        // 使用常量（假设 MessageConstant.PASSWORD_ERROR 存在）
        BaseException ex = new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(MessageConstant.PASSWORD_ERROR, result.getMsg());
    }

    @Test
    @DisplayName("测试 SetmealEnableFailedException - 套餐启用失败")
    public void testSetmealEnableFailedException() {
        String expectedMsg = "套餐包含停售菜品，无法启用";
        BaseException ex = new SetmealEnableFailedException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 ShoppingCartBusinessException - 购物车业务异常")
    public void testShoppingCartBusinessException() {
        String expectedMsg = "购物车数据异常";
        BaseException ex = new ShoppingCartBusinessException(expectedMsg);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(expectedMsg, result.getMsg());
    }

    @Test
    @DisplayName("测试 UserNotLoginException - 用户未登录")
    public void testUserNotLoginException() {
        // 使用常量（假设 MessageConstant.USER_NOT_LOGIN 存在）
        BaseException ex = new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        Result<String> result = handler.exceptionHandler(ex);
        assertNotNull(result);
        assertEquals(MessageConstant.USER_NOT_LOGIN, result.getMsg());
    }
    
}