# 苍穹外卖 Apifox 测试脚本

## 使用方法

1. 在 Apifox 中新建项目「苍穹外卖」
2. 新建环境「测试环境」，设置 Base URL = `http://localhost:8080`
3. 将下面的脚本按说明粘贴到对应位置

## 文件说明

| 文件 | 用途 | 放置位置 |
|---|---|---|
| `pre-scripts/admin-login.js` | 管理端自动登录获取token | 管理端文件夹 → 前置脚本 |
| `pre-scripts/user-login.js` | C端用户自动登录获取token | C端文件夹 → 前置脚本 |
| `tests/admin-employee.js` | 员工管理接口测试 | 各接口 → 后置脚本 |
| `tests/admin-category.js` | 分类管理接口测试 | 各接口 → 后置脚本 |
| `tests/admin-dish.js` | 菜品管理接口测试 | 各接口 → 后置脚本 |
| `tests/admin-setmeal.js` | 套餐管理接口测试 | 各接口 → 后置脚本 |
| `tests/admin-order.js` | 管理端订单接口测试 | 各接口 → 后置脚本 |
| `tests/admin-shop.js` | 店铺状态接口测试 | 各接口 → 后置脚本 |
| `tests/admin-report.js` | 报表接口测试 | 各接口 → 后置脚本 |
| `tests/admin-workspace.js` | 工作台接口测试 | 各接口 → 后置脚本 |
| `tests/user-addressbook.js` | 地址簿接口测试 | 各接口 → 后置脚本 |
| `tests/user-shoppingcart.js` | 购物车接口测试 | 各接口 → 后置脚本 |
| `tests/user-order.js` | C端订单接口测试 | 各接口 → 后置脚本 |
| `tests/user-browse.js` | C端浏览接口测试 | 各接口 → 后置脚本 |
| `scenarios/order-flow.js` | 完整下单业务流测试场景 | 测试场景 |
| `scenarios/employee-crud.js` | 员工CRUD业务流测试场景 | 测试场景 |
| `scenarios/dish-category-flow.js` | 菜品分类关联业务流测试场景 | 测试场景 |

## 环境变量

在 Apifox 环境中需要配置以下变量：

| 变量名 | 说明 | 示例值 |
|---|---|---|
| `base_url` | 服务地址 | `http://localhost:8080` |
| `admin_token` | 管理端token（自动获取） | - |
| `user_token` | C端token（自动获取） | - |
| `admin_username` | 管理员账号 | `admin` |
| `admin_password` | 管理员密码 | `123456` |
