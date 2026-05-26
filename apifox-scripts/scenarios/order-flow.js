// ============================================================
// 业务流测试场景：完整下单流程
// 场景：C端登录 → 浏览菜品 → 加购物车 → 下单 → 管理端接单 → 派送 → 完成
// 使用方法：在 Apifox 中创建「测试场景」，按顺序添加以下步骤
// ============================================================

// ===== 步骤1：管理端登录 =====
// POST /admin/employee/login
// Body: {"username": "admin", "password": "123456"}
// 后置脚本：
pm.test("步骤1-管理端登录成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.token).to.be.a("string");
});
const step1 = pm.response.json();
pm.environment.set("admin_token", step1.data.token);

// ===== 步骤2：设置店铺营业 =====
// PUT /admin/shop/1
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤2-设置店铺营业", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤3：C端用户登录 =====
// POST /user/user/login
// Body: {"code": "test_wx_code"}
// 后置脚本：
pm.test("步骤3-C端登录成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.token).to.be.a("string");
});
const step3 = pm.response.json();
pm.environment.set("user_token", step3.data.token);

// ===== 步骤4：查询分类列表 =====
// GET /user/category/list?type=1
// Header: token: {{user_token}}
// 后置脚本：
pm.test("步骤4-获取菜品分类", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data).to.be.an("array").and.not.empty;
});
const step4 = pm.response.json();
pm.environment.set("test_category_id", step4.data[0].id);

// ===== 步骤5：查询分类下菜品 =====
// GET /user/dish/list?categoryId={{test_category_id}}
// Header: token: {{user_token}}
// 后置脚本：
pm.test("步骤5-获取菜品列表", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data).to.be.an("array").and.not.empty;
});
const step5 = pm.response.json();
pm.environment.set("test_dish_id", step5.data[0].id);
pm.environment.set("test_dish_name", step5.data[0].name);

// ===== 步骤6：添加菜品到购物车 =====
// POST /user/shoppingCart/add
// Header: token: {{user_token}}
// Body: {"dishId": {{test_dish_id}}}
// 后置脚本：
pm.test("步骤6-添加购物车成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤7：查看购物车 =====
// GET /user/shoppingCart/list
// Header: token: {{user_token}}
// 后置脚本：
pm.test("步骤7-购物车不为空", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data).to.be.an("array").and.not.empty;
});
pm.test("步骤7-购物车包含刚加的菜品", function () {
    const res = pm.response.json();
    const found = res.data.some(item => item.dishId == pm.environment.get("test_dish_id"));
    pm.expect(found).to.be.true;
});

// ===== 步骤8：新增收货地址 =====
// POST /user/addressBook
// Header: token: {{user_token}}
// Body:
// {
//     "consignee": "测试用户",
//     "phone": "13800138000",
//     "sex": "1",
//     "provinceCode": "330000",
//     "provinceName": "浙江省",
//     "cityCode": "330100",
//     "cityName": "杭州市",
//     "districtCode": "330106",
//     "districtName": "西湖区",
//     "detail": "浙大路38号",
//     "label": "1",
//     "isDefault": 1
// }
// 后置脚本：
pm.test("步骤8-新增地址成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤9：查询默认地址 =====
// GET /user/addressBook/default
// Header: token: {{user_token}}
// 后置脚本：
pm.test("步骤9-获取默认地址", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data).to.have.property("id");
});
const step9 = pm.response.json();
pm.environment.set("test_address_id", step9.data.id);

// ===== 步骤10：提交订单 =====
// POST /user/order/submit
// Header: token: {{user_token}}
// Body:
// {
//     "addressBookId": {{test_address_id}},
//     "payMethod": 1,
//     "remark": "自动化测试订单",
//     "estimatedDeliveryTime": "2026-05-26 18:00:00",
//     "deliveryStatus": 1,
//     "tablewareNumber": 1,
//     "tablewareStatus": 1,
//     "packAmount": 1,
//     "amount": 100
// }
// 后置脚本：
pm.test("步骤10-下单成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("orderNumber");
});
const step10 = pm.response.json();
pm.environment.set("test_order_id", step10.data.id);
pm.environment.set("test_order_number", step10.data.orderNumber);

// ===== 步骤11：管理端查看订单统计 =====
// GET /admin/order/statistics
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤11-有待接单订单", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.toBeConfirmed).to.be.at.least(1);
});

// ===== 步骤12：管理端接单 =====
// PUT /admin/order/confirm
// Header: token: {{admin_token}}
// Body: {"id": {{test_order_id}}}
// 后置脚本：
pm.test("步骤12-接单成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤13：管理端派送 =====
// PUT /admin/order/delivery/{{test_order_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤13-派送成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤14：管理端完成订单 =====
// PUT /admin/order/complete/{{test_order_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤14-完成订单", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤15：C端查看历史订单验证状态 =====
// GET /user/order/orderDetail/{{test_order_id}}
// Header: token: {{user_token}}
// 后置脚本：
pm.test("步骤15-订单状态为已完成", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.status).to.equal(5); // 5=已完成
});

// ===== 步骤16：清空购物车 =====
// DELETE /user/shoppingCart/clean
// Header: token: {{user_token}}
// 后置脚本：
pm.test("步骤16-清空购物车", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
