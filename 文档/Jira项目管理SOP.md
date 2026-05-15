# 苍穹外卖测试 — Jira 项目管理

---

## 一、Jira 项目配置

### 1.1 Epic 结构

| Epic Key | 名称 | 对应测试阶段 | 颜色 |
|---|---|---|---|
| `TEST-UNIT` | 单元测试 | W1-W2 | 蓝 |
| `TEST-INTEG` | 集成测试 | W3 | 绿 |
| `TEST-SYS` | 系统测试 | W4 | 橙 |
| `TEST-REPORT` | 测试报告 | W4-W5 | 紫 |

### 1.2 看板列定义

```
Backlog  →  To Do  →  In Progress  →  Review  →  Done
  ↑                                               │
  └────────── Reopen（审查不通过退回）←─────────────┘
```

| 列名 | 含义 | 谁操作 |
|---|---|---|
| Backlog | 未分配任务池 | M1 创建时放入 |
| To Do | 已分配，等待开始 | 成员自己拖入 |
| In Progress | 正在执行（同时只能有 1 个） | 成员开始工作时拖入 |
| Review | 代码写完，等待 M1 审查 | 成员完成后拖入 |
| Done | 审查通过 | M1 审查通过后拖入 |
| Reopen | 审查不通过，打回修改 | M1 拖回 To Do |

---

## 二、Issue 类型与命名规范

### 2.1 只用两种 Issue 类型

| 类型 | 用途 | 谁建 |
|---|---|---|
| **Task** | 测试用例编写、测试执行 | M1 初始化，成员可补充 |
| **Bug** | 测试中发现的缺陷 | 发现者立即建 |

### 2.2 Task 命名规范

```
[模块缩写]-[编号] [测试层级]: [具体描述]

模块缩写对照：
  EMP = 员工     CAT = 分类     DISH = 菜品
  SET = 套餐     ORD = 订单     CART = 购物车
  USER = C端用户  ADDR = 地址簿  RPT = 报表
  INFRA = 基础设施  PERF = 性能  SEC = 安全
```

**正确示例**：
```
EMP-001 单元: EmployeeController.login 正常登录返回Token
EMP-002 单元: EmployeeController.login 用户名不存在抛异常
EMP-003 单元: EmployeeServiceImpl.login 密码MD5加密比对
EMP-010 集成: 登录→拿Token→调分页接口 全链路
ORD-007 单元: OrderController.confirm 已完成订单无法再次接单
PERF-001 系统: /admin/employee/page 并发200压测
```

**错误示例**：
```
❌ 测登录               （太模糊，不知道测什么）
❌ 员工模块测试1         （没有编号、没有层级）
❌ EMP-TEST-001         （没有用中文描述，别人看不懂）
```

### 2.3 Bug 命名规范

```
[Bug] [模块]: 简短描述

正确示例：
[Bug] [员工]: 登录接口密码为空时返回500而非提示错误
[Bug] [订单]: 已完成订单仍可通过接单接口再次确认
[Bug] [安全]: 用户A可查看用户B的订单详情
```

---

## 三、Task 字段填写规范

| 字段 | 必填 | 填写规则 |
|---|---|---|
| Summary | ✅ | 按命名规范写 |
| Epic Link | ✅ | 选择对应 Epic |
| Assignee | ✅ | 分配给执行者 |
| Priority | ✅ | 按下方优先级规则 |
| Description | ✅ | 写测试步骤（Given-When-Then 格式） |
| Labels | 推荐 | 加上模块标签（如 `employee`、`order`） |

### Priority 规则

| 级别 | 适用场景 |
|---|---|
| Highest | 阻塞其他成员测试的 Bug |
| High | 核心功能缺陷（登录、下单、支付） |
| Medium | 一般功能缺陷、边界问题 |
| Low | 文案错误、UI 小问题、非关键接口 |

---

## 四、组长 SOP（M1）

### 4.1 W1 初始化（项目启动）

1. 创建 Jira 项目，类型选 Scrum
2. 建 4 个 Epic：`TEST-UNIT`、`TEST-INTEG`、`TEST-SYS`、`TEST-REPORT`
3. 配置看板，添加 Review 和 Reopen 列
4. 按分工表给每人批量创建 Task（M2~M8 各 20~50 个），放入 Backlog
5. 发群公告：Jira 链接 + 账户 + 命名规范文档

### 4.2 每日操作（W1-W4 每天）

| 时间 | 操作 |
|---|---|
| 早 9:00 | 打开看板，截图当前状态发群（相当于站会） |
| 全天 | 监控 Review 列，有卡就审查 |
| 晚 18:00 | 检查是否有人 In Progress 超过 2 天没动，私聊问情况 |

### 4.3 审查流程（Review 列有卡时）

1. 点开 Task，看 Description 里的 Given-When-Then
2. 打开对应测试代码，检查三点：
   - 覆盖了 Description 里写的全部场景？
   - 断言有意义？（不是 `assertTrue(true)` 这种废断言）
   - 命名清晰？（看方法名就知道测什么）
3. 通过 → 拖到 Done
4. 不通过 → 在 Comment 里写清楚哪里不行，拖到 Reopen（自动回到 To Do）

### 4.4 每周五操作

1. 导出 Burndown Chart，截图发群
2. 导出 Bug 分布（按模块、按严重级别），截图发群
3. 对比计划进度 vs 实际进度，差太多就调整下周分配

### 4.5 W4 收尾操作

1. 从 Jira 导出以下数据用于报告第 7 章：
   - Task 完成率（Done / 总 Task）
   - Bug 总数 / 已修复数 / 未修复数
   - 各模块 Bug 分布表
   - 各严重级别 Bug 统计
2. 关闭所有已修复 Bug
3. 未修复 Bug 标记为 Won't Fix 或 Known Issue

---

## 五、组员 SOP（M2~M8）

### 5.1 每日操作流程

```
早 9:00 看群 → 从 To Do 拖 1 张卡到 In Progress → 开始干活
                                                ↓
                                          写完 + 跑通
                                                ↓
                                    拖到 Review，在 Comment 写：
                                    "已完成，测试代码路径: xxxTest.java"
                                                ↓
                              等 M1 Review → 通过则 Done → 拖下一张
                                            → 不通过 → 回到 To Do → 修改 → 再拖 Review
```

### 5.2 开始一个 Task 前

1. 确认自己 In Progress 列是空的（同时只做一件事）
2. 把要做的 Task 从 To Do 拖到 In Progress
3. 看 Description，确认自己理解了要测的场景

### 5.3 完成一个 Task 后

1. 本地 `mvn test` 确保全部通过
2. 把 Task 拖到 Review
3. 在 Comment 里写：

```
✅ 已完成
测试类: src/test/java/com/sky/EmployeeControllerTest.java
测试方法: testLoginSuccess()
跑通截图: [贴终端输出或 IDEA 截图]
```

4. 如果等 Review 期间想做下一件事，**不要等**，直接拖下一张 To Do 开始

### 5.4 发现 Bug 时

🚨 **立即建 Bug Issue，不要等到 W4**：

1. 点 Create → 类型选 Bug
2. Summary 按命名规范写
3. Description 写：

```
复现步骤：
1. 调用 POST /admin/employee/login
2. 传参 {"username":"admin","password":""}
3. 预期返回 "密码不能为空"
4. 实际返回 HTTP 500 Internal Server Error

环境：本地开发环境
严重级别：High
```

4. 关联到当前 Task（Linked Issues）
5. **先建 Bug，再继续干活**

### 5.5 被 Reopen 后

1. 看 M1 在 Comment 里写的原因
2. 修改代码
3. 重新拖到 Review
4. 在 Comment 里回复："已修改，见 xxx 行"

---

## 六、Task Description 模板

### 单元测试 Task（Given-When-Then）

```
【测试目标】验证 EmployeeController.login 在用户名不存在时抛出异常

【Given】数据库中没有用户名为 "nonexist" 的员工
【When】调用 POST /admin/employee/login，传参 {"username":"nonexist","password":"123456"}
【Then】
  1. HTTP 状态码 200
  2. 响应体 code = 0（业务失败）
  3. 响应体 msg = "账号不存在"

【测试方法】等价类划分 - 无效等价类
【所属模块】员工管理
```

### 集成测试 Task

```
【测试目标】验证登录→鉴权→调用业务接口的全链路

【Given】数据库有员工 admin/123456，状态为启用
【When】
  1. 调用 POST /admin/employee/login 登录
  2. 从响应中提取 token
  3. 带 token 调用 GET /admin/employee/page?page=1&pageSize=10
【Then】
  1. 登录返回 200 + token
  2. 分页接口返回 200 + total > 0

【涉及组件】EmployeeController → EmployeeService → EmployeeMapper → MySQL
```

### 系统测试 Task（性能/安全）

```
【测试目标】压测 GET /admin/employee/page 在高并发下的表现

【测试工具】JMeter
【并发级别】50 / 100 / 200 / 500
【持续时间】每级别持续 60 秒
【成功标准】
  - 50并发：TPS > 1000，响应时间 < 500ms
  - 200并发：TPS > 800，响应时间 < 1000ms
  - 500并发：TPS > 500，错误率 < 1%
```

---

## 七、常见问题

### Q1：Task 太多拖不过来怎么办？

找 M1，把部分 Task 降优先级或关闭。Task 是计划不是军令状。

### Q2：我发现了别人模块的 Bug 怎么办？

直接建 Bug，Assign 给那个模块的负责人。不是只有自己模块才管。

### Q3：Review 等太久怎么办？

群里 @ M1。或者先拖下一张做，Review 通过了再回来关。

### Q4：一个 Bug 导致我多个 Task 跑不了怎么办？

建 Bug → 把受影响的 Task 拖回 To Do → Comment 写"阻塞：等待 Bug-XXX 修复"→ 去做其他不依赖这个 Bug 的 Task。

### Q5：不确定一个场景该建 Task 还是 Bug？

先建 Task，测完发现行为不符合预期 → 再建 Bug。

---

## 八、检查清单

### 组长 W1 检查清单

- [ ] Jira 项目已创建，4 个 Epic 就绪
- [ ] 看板有 Backlog / To Do / In Progress / Review / Done 五列
- [ ] 所有成员有 Jira 账号并能登录
- [ ] M2~M8 每人至少创建了 15 个 Task
- [ ] 命名规范文档已发群
- [ ] 群公告有 Jira 链接

### 组长每周检查清单

- [ ] Burndown Chart 已截图发群
- [ ] Bug 统计已截图发群
- [ ] Review 列每周清零（没有积压超过 3 天的）
- [ ] Done 列数量符合计划进度

### 组员每日检查清单

- [ ] In Progress 只有 1 张卡
- [ ] 完成的卡已拖到 Review 并写了 Comment
- [ ] 发现的 Bug 已建 Issue
- [ ] 没有超过 2 天不动的 In Progress 卡
