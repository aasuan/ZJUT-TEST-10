// ============================================================
// 地址簿接口测试脚本
// 接口：/user/addressBook
// ============================================================

// ==================== POST /user/addressBook（新增地址） ====================
// 后置脚本 - 新增地址
pm.test("新增地址 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("新增地址 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /user/addressBook/list ====================
// 后置脚本 - 查询地址列表
pm.test("地址列表 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("地址列表 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("地址列表 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
pm.test("地址列表 - 地址包含必要字段", function () {
    const res = pm.response.json();
    if (res.data.length > 0) {
        const addr = res.data[0];
        pm.expect(addr).to.have.property("id");
        pm.expect(addr).to.have.property("consignee");
        pm.expect(addr).to.have.property("phone");
    }
});

// ==================== GET /user/addressBook/{id} ====================
// 后置脚本 - 根据id查询地址
pm.test("查询地址 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查询地址 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查询地址 - 返回地址详情", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("consignee");
    pm.expect(res.data).to.have.property("phone");
    pm.expect(res.data).to.have.property("detail");
});

// ==================== PUT /user/addressBook（修改地址） ====================
// 后置脚本 - 修改地址
pm.test("修改地址 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("修改地址 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== PUT /user/addressBook/default ====================
// 后置脚本 - 设置默认地址
pm.test("设默认地址 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("设默认地址 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /user/addressBook/default ====================
// 后置脚本 - 查询默认地址
pm.test("查默认地址 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查默认地址 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查默认地址 - 返回地址信息", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("isDefault", 1);
});

// ==================== DELETE /user/addressBook ====================
// 后置脚本 - 删除地址
pm.test("删除地址 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("删除地址 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
