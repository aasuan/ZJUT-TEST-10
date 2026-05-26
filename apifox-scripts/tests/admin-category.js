// ============================================================
// 分类管理接口测试脚本
// 接口：/admin/category
// ============================================================

// ==================== POST /admin/category（新增分类） ====================
// 后置脚本 - 新增分类
pm.test("新增分类 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("新增分类 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /admin/category/page ====================
// 后置脚本 - 分类分页查询
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
pm.test("分页查询 - 分类包含必要字段", function () {
    const res = pm.response.json();
    if (res.data.records.length > 0) {
        const item = res.data.records[0];
        pm.expect(item).to.have.property("id");
        pm.expect(item).to.have.property("name");
        pm.expect(item).to.have.property("type");
        pm.expect(item).to.have.property("sort");
        pm.expect(item).to.have.property("status");
    }
});

// ==================== PUT /admin/category（修改分类） ====================
// 后置脚本 - 修改分类
pm.test("修改分类 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("修改分类 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== POST /admin/category/status/{status} ====================
// 后置脚本 - 启用/禁用分类
pm.test("启用禁用分类 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("启用禁用分类 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== DELETE /admin/category ====================
// 后置脚本 - 删除分类（正常）
pm.test("删除分类 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("删除分类 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// 后置脚本 - 删除分类（有关联菜品，应失败）
pm.test("删除关联分类 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("删除关联分类 - 业务码为0（失败）", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(0);
});
pm.test("删除关联分类 - 返回错误提示", function () {
    const res = pm.response.json();
    pm.expect(res.msg).to.include("关联");
});

// ==================== GET /admin/category/list ====================
// 后置脚本 - 根据类型查询分类
pm.test("查询分类列表 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查询分类列表 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查询分类列表 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
