// ============================================================
// 购物车接口测试脚本
// 接口：/user/shoppingCart
// ============================================================

// ==================== POST /user/shoppingCart/add ====================
// 后置脚本 - 添加购物车
pm.test("添加购物车 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("添加购物车 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /user/shoppingCart/list ====================
// 后置脚本 - 查看购物车
pm.test("查看购物车 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查看购物车 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查看购物车 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
pm.test("查看购物车 - 商品包含必要字段", function () {
    const res = pm.response.json();
    if (res.data.length > 0) {
        const item = res.data[0];
        pm.expect(item).to.have.property("name");
        pm.expect(item).to.have.property("number");
        pm.expect(item).to.have.property("amount");
    }
});

// ==================== POST /user/shoppingCart/sub ====================
// 后置脚本 - 减少购物车商品
pm.test("减少商品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("减少商品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== DELETE /user/shoppingCart/clean ====================
// 后置脚本 - 清空购物车
pm.test("清空购物车 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("清空购物车 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
