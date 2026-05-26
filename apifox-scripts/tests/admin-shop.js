// ============================================================
// 店铺状态接口测试脚本
// 接口：/admin/shop
// ============================================================

// ==================== PUT /admin/shop/{status}（设置店铺状态） ====================
// 后置脚本 - 设置营业
pm.test("设置营业 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("设置营业 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /admin/shop/status ====================
// 后置脚本 - 获取店铺状态
pm.test("获取状态 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("获取状态 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("获取状态 - 返回状态值", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.oneOf([0, 1]);
});
