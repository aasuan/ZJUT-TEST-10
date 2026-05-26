// ============================================================
// 管理端订单接口测试脚本
// 接口：/admin/order
// ============================================================

// ==================== GET /admin/order/conditionSearch ====================
// 后置脚本 - 订单条件搜索
pm.test("订单搜索 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("订单搜索 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("订单搜索 - 返回分页结构", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("total");
    pm.expect(res.data).to.have.property("records");
    pm.expect(res.data.records).to.be.an("array");
});
pm.test("订单搜索 - 订单包含必要字段", function () {
    const res = pm.response.json();
    if (res.data.records.length > 0) {
        const order = res.data.records[0];
        pm.expect(order).to.have.property("id");
        pm.expect(order).to.have.property("number");
        pm.expect(order).to.have.property("status");
        pm.expect(order).to.have.property("amount");
    }
});

// ==================== GET /admin/order/statistics ====================
// 后置脚本 - 订单状态统计
pm.test("订单统计 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("订单统计 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("订单统计 - 返回各状态数量", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("toBeConfirmed");
    pm.expect(res.data).to.have.property("confirmed");
    pm.expect(res.data).to.have.property("deliveryInProgress");
});

// ==================== GET /admin/order/details/{id} ====================
// 后置脚本 - 订单详情
pm.test("订单详情 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("订单详情 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("订单详情 - 返回订单信息", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("orderDetailList");
});

// ==================== PUT /admin/order/confirm ====================
// 后置脚本 - 接单
pm.test("接单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("接单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== PUT /admin/order/rejection ====================
// 后置脚本 - 拒单
pm.test("拒单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("拒单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== PUT /admin/order/cancel ====================
// 后置脚本 - 取消订单
pm.test("取消订单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("取消订单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== PUT /admin/order/delivery/{id} ====================
// 后置脚本 - 派送订单
pm.test("派送订单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("派送订单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== PUT /admin/order/complete/{id} ====================
// 后置脚本 - 完成订单
pm.test("完成订单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("完成订单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
