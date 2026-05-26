// ============================================================
// C端订单接口测试脚本
// 接口：/user/order
// ============================================================

// ==================== POST /user/order/submit ====================
// 后置脚本 - 用户下单
pm.test("用户下单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("用户下单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("用户下单 - 返回订单信息", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("orderNumber");
    pm.expect(res.data).to.have.property("orderAmount");
    pm.expect(res.data).to.have.property("orderTime");
});
// 保存订单id供后续使用
const submitRes = pm.response.json();
if (submitRes.code === 1 && submitRes.data) {
    pm.environment.set("order_id", submitRes.data.id);
    pm.environment.set("order_number", submitRes.data.orderNumber);
}

// ==================== PUT /user/order/payment ====================
// 后置脚本 - 订单支付
pm.test("订单支付 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("订单支付 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /user/order/historyOrders ====================
// 后置脚本 - 查询历史订单
pm.test("历史订单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("历史订单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("历史订单 - 返回分页结构", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("total");
    pm.expect(res.data).to.have.property("records");
    pm.expect(res.data.records).to.be.an("array");
});

// ==================== GET /user/order/orderDetail/{id} ====================
// 后置脚本 - 查询订单详情
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
    pm.expect(res.data.orderDetailList).to.be.an("array");
});

// ==================== PUT /user/order/cancel/{id} ====================
// 后置脚本 - 用户取消订单
pm.test("取消订单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("取消订单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== POST /user/order/repetition/{id} ====================
// 后置脚本 - 再来一单
pm.test("再来一单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("再来一单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /user/order/reminder/{id} ====================
// 后置脚本 - 用户催单
pm.test("用户催单 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("用户催单 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
