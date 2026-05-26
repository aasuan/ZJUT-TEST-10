// ============================================================
// 工作台接口测试脚本
// 接口：/admin/workspace
// ============================================================

// ==================== GET /admin/workspace/businessData ====================
// 后置脚本 - 今日运营数据
pm.test("今日数据 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("今日数据 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("今日数据 - 返回运营指标", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("turnover");
    pm.expect(res.data).to.have.property("validOrderCount");
    pm.expect(res.data).to.have.property("orderCompletionRate");
    pm.expect(res.data).to.have.property("unitPrice");
    pm.expect(res.data).to.have.property("newUsers");
});

// ==================== GET /admin/workspace/overviewOrders ====================
// 后置脚本 - 订单管理数据
pm.test("订单总览 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("订单总览 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("订单总览 - 返回各状态订单数", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("waitingOrders");
    pm.expect(res.data).to.have.property("deliveredOrders");
    pm.expect(res.data).to.have.property("completedOrders");
    pm.expect(res.data).to.have.property("cancelledOrders");
    pm.expect(res.data).to.have.property("allOrders");
});

// ==================== GET /admin/workspace/overviewDishes ====================
// 后置脚本 - 菜品总览
pm.test("菜品总览 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("菜品总览 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("菜品总览 - 返回菜品统计", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("sold");
    pm.expect(res.data).to.have.property("discontinued");
});

// ==================== GET /admin/workspace/overviewSetmeals ====================
// 后置脚本 - 套餐总览
pm.test("套餐总览 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("套餐总览 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("套餐总览 - 返回套餐统计", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("sold");
    pm.expect(res.data).to.have.property("discontinued");
});
