// ============================================================
// 菜品管理接口测试脚本
// 接口：/admin/dish
// ============================================================

// ==================== POST /admin/dish（新增菜品） ====================
// 后置脚本 - 新增菜品
pm.test("新增菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("新增菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /admin/dish/page ====================
// 后置脚本 - 菜品分页查询
pm.test("分页查询 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("分页查询 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("分页查询 - 返回分页结构", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("total");
    pm.expect(res.data).to.have.property("records");
    pm.expect(res.data.records).to.be.an("array");
});
pm.test("分页查询 - 菜品包含必要字段", function () {
    const res = pm.response.json();
    if (res.data.records.length > 0) {
        const item = res.data.records[0];
        pm.expect(item).to.have.property("id");
        pm.expect(item).to.have.property("name");
        pm.expect(item).to.have.property("categoryId");
        pm.expect(item).to.have.property("price");
        pm.expect(item).to.have.property("status");
    }
});

// ==================== GET /admin/dish/{id} ====================
// 后置脚本 - 根据id查询菜品
pm.test("查询菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查询菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查询菜品 - 返回菜品详情含口味", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("name");
    pm.expect(res.data).to.have.property("flavors");
});

// ==================== PUT /admin/dish（修改菜品） ====================
// 后置脚本 - 修改菜品
pm.test("修改菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("修改菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== POST /admin/dish/status/{status} ====================
// 后置脚本 - 启用/停用菜品
pm.test("启停菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("启停菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== DELETE /admin/dish ====================
// 后置脚本 - 删除菜品
pm.test("删除菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("删除菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// 后置脚本 - 删除起售中菜品（应失败）
pm.test("删除起售菜品 - 业务码为0", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(0);
});
pm.test("删除起售菜品 - 返回错误提示", function () {
    const res = pm.response.json();
    pm.expect(res.msg).to.be.a("string").and.not.empty;
});

// ==================== GET /admin/dish/list ====================
// 后置脚本 - 根据分类id查询菜品
pm.test("按分类查菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("按分类查菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("按分类查菜品 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
