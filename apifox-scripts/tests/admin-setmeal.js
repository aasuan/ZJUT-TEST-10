// ============================================================
// 套餐管理接口测试脚本
// 接口：/admin/setmeal
// ============================================================

// ==================== POST /admin/setmeal（新增套餐） ====================
// 后置脚本 - 新增套餐
pm.test("新增套餐 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("新增套餐 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /admin/setmeal/page ====================
// 后置脚本 - 套餐分页查询
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
pm.test("分页查询 - 套餐包含必要字段", function () {
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

// ==================== GET /admin/setmeal/{id} ====================
// 后置脚本 - 根据id查询套餐
pm.test("查询套餐 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查询套餐 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查询套餐 - 返回套餐详情", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("name");
    pm.expect(res.data).to.have.property("setmealDishes");
});

// ==================== PUT /admin/setmeal（修改套餐） ====================
// 后置脚本 - 修改套餐
pm.test("修改套餐 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("修改套餐 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== POST /admin/setmeal/status/{status} ====================
// 后置脚本 - 启用/停用套餐
pm.test("启停套餐 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("启停套餐 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// 后置脚本 - 起售套餐但菜品停售（应失败）
pm.test("起售套餐菜品停售 - 业务码为0", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(0);
});
pm.test("起售套餐菜品停售 - 返回错误提示", function () {
    const res = pm.response.json();
    pm.expect(res.msg).to.be.a("string").and.not.empty;
});

// ==================== DELETE /admin/setmeal ====================
// 后置脚本 - 删除套餐
pm.test("删除套餐 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("删除套餐 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// 后置脚本 - 删除起售中套餐（应失败）
pm.test("删除起售套餐 - 业务码为0", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(0);
});
