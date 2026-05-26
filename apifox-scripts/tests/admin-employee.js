// ============================================================
// 员工管理接口测试脚本
// 接口：/admin/employee
// ============================================================

// ==================== POST /admin/employee/login ====================
// 后置脚本 - 员工登录
pm.test("登录成功 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("登录成功 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("登录成功 - 返回token", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("token");
    pm.expect(res.data.token).to.be.a("string").and.not.empty;
});
pm.test("登录成功 - 返回用户信息", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("userName");
    pm.expect(res.data).to.have.property("name");
});
// 保存 token 到环境变量
const loginRes = pm.response.json();
if (loginRes.code === 1 && loginRes.data) {
    pm.environment.set("admin_token", loginRes.data.token);
}

// ==================== POST /admin/employee/login（异常） ====================
// 后置脚本 - 登录失败（错误密码）
pm.test("登录失败 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("登录失败 - 业务码为0", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(0);
});
pm.test("登录失败 - 返回错误信息", function () {
    const res = pm.response.json();
    pm.expect(res.msg).to.be.a("string").and.not.empty;
});

// ==================== POST /admin/employee（新增员工） ====================
// 后置脚本 - 新增员工
pm.test("新增员工 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("新增员工 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== GET /admin/employee/page ====================
// 后置脚本 - 员工分页查询
pm.test("分页查询 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("分页查询 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("分页查询 - 返回分页数据", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("total");
    pm.expect(res.data).to.have.property("records");
    pm.expect(res.data.records).to.be.an("array");
});
pm.test("分页查询 - total为数字", function () {
    const res = pm.response.json();
    pm.expect(res.data.total).to.be.a("number");
});

// ==================== GET /admin/employee/{id} ====================
// 后置脚本 - 根据id查询员工
pm.test("查询员工 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("查询员工 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("查询员工 - 返回员工信息", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.have.property("id");
    pm.expect(res.data).to.have.property("username");
    pm.expect(res.data).to.have.property("name");
});

// ==================== PUT /admin/employee（修改员工） ====================
// 后置脚本 - 修改员工
pm.test("修改员工 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("修改员工 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== POST /admin/employee/status/{status} ====================
// 后置脚本 - 启用/禁用员工
pm.test("启用禁用 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("启用禁用 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ==================== POST /admin/employee/logout ====================
// 后置脚本 - 退出登录
pm.test("退出登录 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("退出登录 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
