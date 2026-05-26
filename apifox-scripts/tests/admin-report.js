// ============================================================
// 报表接口测试脚本
// 接口：/admin/report
// ============================================================

// ==================== GET /admin/report/turnoverStatistics ====================
// 后置脚本 - 营业额统计
pm.test("营业额统计 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("营业额统计 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("营业额统计 - 返回日期和金额列表", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("dateList");
    pm.expect(res.data).to.have.property("turnoverList");
});

// ==================== GET /admin/report/userStatistics ====================
// 后置脚本 - 用户统计
pm.test("用户统计 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("用户统计 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("用户统计 - 返回用户数据", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("dateList");
    pm.expect(res.data).to.have.property("totalUserList");
    pm.expect(res.data).to.have.property("newUserList");
});

// ==================== GET /admin/report/ordersStatistics ====================
// 后置脚本 - 订单统计
pm.test("订单统计 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("订单统计 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("订单统计 - 返回订单数据", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("dateList");
    pm.expect(res.data).to.have.property("orderCountList");
    pm.expect(res.data).to.have.property("validOrderCountList");
    pm.expect(res.data).to.have.property("totalOrderCount");
    pm.expect(res.data).to.have.property("validOrderCount");
    pm.expect(res.data).to.have.property("orderCompletionRate");
});

// ==================== GET /admin/report/top10 ====================
// 后置脚本 - 销量Top10
pm.test("销量Top10 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("销量Top10 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("销量Top10 - 返回排名数据", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("nameList");
    pm.expect(res.data).to.have.property("numberList");
});

// ==================== GET /admin/report/export ====================
// 后置脚本 - 导出Excel
pm.test("导出Excel - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("导出Excel - Content-Type为Excel格式", function () {
    const contentType = pm.response.headers.get("Content-Type");
    // Excel 文件的 MIME 类型
    pm.expect(contentType).to.include("application/vnd.openxmlformats");
});
