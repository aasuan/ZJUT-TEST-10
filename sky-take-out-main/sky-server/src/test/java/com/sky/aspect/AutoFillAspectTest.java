package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * AutoFillAspect 单元测试
 * 验证切面在 INSERT / UPDATE 时自动填充时间和用户ID
 */
@ExtendWith(MockitoExtension.class)
class AutoFillAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private AutoFillAspect autoFillAspect;

    // 模拟实体类（包含公共字段的setter和getter）
    static class TestEntity {
        private LocalDateTime createTime;
        private Long createUser;
        private LocalDateTime updateTime;
        private Long updateUser;

        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public void setCreateUser(Long createUser) { this.createUser = createUser; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
        public void setUpdateUser(Long updateUser) { this.updateUser = updateUser; }

        public LocalDateTime getCreateTime() { return createTime; }
        public Long getCreateUser() { return createUser; }
        public LocalDateTime getUpdateTime() { return updateTime; }
        public Long getUpdateUser() { return updateUser; }
    }

    // 模拟带 @AutoFill 注解的方法
    @AutoFill(OperationType.INSERT)
    public void insertMethod(TestEntity entity) {}

    @AutoFill(OperationType.UPDATE)
    public void updateMethod(TestEntity entity) {}

    private Method insertMethod;
    private Method updateMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        insertMethod = this.getClass().getDeclaredMethod("insertMethod", TestEntity.class);
        updateMethod = this.getClass().getDeclaredMethod("updateMethod", TestEntity.class);
    }

    @AfterEach
    void clearContext() {
        BaseContext.removeCurrentId(); // 假设有清理方法，避免影响其他测试
    }

    @Test
    void autoFill_shouldSetAllFields_whenOperationIsInsert() throws Exception {
        // 准备
        Long mockUserId = 123L;
        BaseContext.setCurrentId(mockUserId);

        TestEntity entity = new TestEntity();
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(insertMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{entity});

        // 执行
        autoFillAspect.autoFill(joinPoint);

        // 验证
        assertThat(entity.getCreateTime()).isNotNull();
        assertThat(entity.getCreateUser()).isEqualTo(mockUserId);
        assertThat(entity.getUpdateTime()).isNotNull();
        assertThat(entity.getUpdateUser()).isEqualTo(mockUserId);
    }

    @Test
    void autoFill_shouldSetOnlyUpdateFields_whenOperationIsUpdate() throws Exception {
        // 准备
        Long mockUserId = 456L;
        BaseContext.setCurrentId(mockUserId);

        TestEntity entity = new TestEntity();
        // 预先填充创建字段，确保更新时不会被误改（反射不会调用 setCreateTime/setCreateUser）
        entity.setCreateTime(LocalDateTime.of(2023, 1, 1, 0, 0));
        entity.setCreateUser(999L);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(updateMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{entity});

        // 执行
        autoFillAspect.autoFill(joinPoint);

        // 验证
        assertThat(entity.getUpdateTime()).isNotNull();
        assertThat(entity.getUpdateUser()).isEqualTo(mockUserId);
        // 创建字段保持不变
        assertThat(entity.getCreateTime()).isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0));
        assertThat(entity.getCreateUser()).isEqualTo(999L);
    }

    @Test
    void autoFill_shouldDoNothing_whenNoEntityParameter() throws Exception {
        // 参数为空数组的情况
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(insertMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        autoFillAspect.autoFill(joinPoint);

        // 无异常抛出即为成功，无需额外断言
    }

    @Test
    void autoFill_shouldDoNothing_whenArgsIsNull() throws Exception {
        // 模拟 getArgs() 返回 null 的极端情况（实际很少发生，但防御代码需要测试）
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(insertMethod);
        when(joinPoint.getArgs()).thenReturn(null);

        // 不应抛出异常
        autoFillAspect.autoFill(joinPoint);
    }

    @Test
    void autoFill_shouldOverwriteExistingValues_whenOperationIsInsert() throws Exception {
        // 准备：实体中已有旧值，验证 INSERT 时会全部覆盖
        Long mockUserId = 123L;
        BaseContext.setCurrentId(mockUserId);

        TestEntity entity = new TestEntity();
        LocalDateTime oldTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setCreateTime(oldTime);
        entity.setCreateUser(999L);
        entity.setUpdateTime(oldTime);
        entity.setUpdateUser(999L);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(insertMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{entity});

        // 执行
        autoFillAspect.autoFill(joinPoint);

        // 验证原有值已被覆盖
        assertThat(entity.getCreateTime()).isNotEqualTo(oldTime);
        assertThat(entity.getCreateUser()).isEqualTo(mockUserId);
        assertThat(entity.getUpdateTime()).isNotEqualTo(oldTime);
        assertThat(entity.getUpdateUser()).isEqualTo(mockUserId);
    }

    @Test
    void autoFill_shouldHandleMissingSetterGracefully() throws Exception {
        // 准备一个缺少部分setter的实体（例如没有 setCreateTime）
        class IncompleteEntity {
            // 只有 setUpdateTime
            public void setUpdateTime(LocalDateTime time) {}
        }

        Long mockUserId = 111L;
        BaseContext.setCurrentId(mockUserId);

        IncompleteEntity entity = new IncompleteEntity();
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(insertMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{entity});

        // 执行（反射会抛出异常并捕获打印堆栈，但不应导致测试失败）
        autoFillAspect.autoFill(joinPoint);

        // 验证没有抛出未捕获异常即可
    }

    @Test
    void autoFill_shouldNotFail_whenCurrentUserIsNull() throws Exception {
        // 模拟当前用户ID为空
        BaseContext.setCurrentId(null);

        TestEntity entity = new TestEntity();
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(insertMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{entity});

        autoFillAspect.autoFill(joinPoint);

        assertThat(entity.getCreateUser()).isNull();
        assertThat(entity.getUpdateUser()).isNull();
        assertThat(entity.getCreateTime()).isNotNull();   // 时间仍然会被填充
        assertThat(entity.getUpdateTime()).isNotNull();
    }
}