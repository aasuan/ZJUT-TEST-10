// ============================================================
// 业务流测试场景：员工 CRUD 全流程
// 场景：登录 → 新增员工 → 查询 → 修改 → 禁用 → 启用 → 退出
// ============================================================

// ===== 步骤1：管理端登录 =====
// POST /admin/employee/login
// Body: {"username": "admin", "password": "123456"}
// 后置脚本：
pm.test("步骤1-登录成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
const loginRes = pm.response.json();
pm.environment.set("admin_token", loginRes.data.token);

// ===== 步骤2：新增员工 =====
// POST /admin/employee
// Header: token: {{admin_token}}
// Body:
// {
//     "name": "测试员工",
//     "username": "test_emp_001",
//     "phone": "13900139001",
//     "sex": "1",
//     "idNumber": "330106199001011234"
// }
// 后置脚本：
pm.test("步骤2-新增员工成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤3：分页查询找到新员工 =====
// GET /admin/employee/page?name=测试员工&page=1&pageSize=10
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤3-查询到新员工", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.total).to.be.at.least(1);
});
pm.test("步骤3-员工信息正确", function () {
    const res = pm.response.json();
    const emp = res.data.records.find(e => e.username === "test_emp_001");
    pm.expect(emp).to.not.be.undefined;
    pm.expect(emp.name).to.equal("测试员工");
});
// 保存员工id
const pageRes = pm.response.json();
const newEmp = pageRes.data.records.find(e => e.username === "test_emp_001");
if (newEmp) {
    pm.environment.set("test_emp_id", newEmp.id);
}

// ===== 步骤4：根据id查询员工详情 =====
// GET /admin/employee/{{test_emp_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤4-查询员工详情", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.name).to.equal("测试员工");
    pm.expect(res.data.phone).to.equal("13900139001");
});

// ===== 步骤5：修改员工信息 =====
// PUT /admin/employee
// Header: token: {{admin_token}}
// Body:
// {
//     "id": {{test_emp_id}},
//     "name": "测试员工-已修改",
//     "phone": "13900139002"
// }
// 后置脚本：
pm.test("步骤5-修改员工成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤6：验证修改结果 =====
// GET /admin/employee/{{test_emp_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤6-修改生效", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.data.name).to.equal("测试员工-已修改");
    pm.expect(res.data.phone).to.equal("13900139002");
});

// ===== 步骤7：禁用员工 =====
// POST /admin/employee/status/0?id={{test_emp_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤7-禁用员工成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤8：验证禁用状态 =====
// GET /admin/employee/{{test_emp_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤8-员工已禁用", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.data.status).to.equal(0);
});

// ===== 步骤9：启用员工 =====
// POST /admin/employee/status/1?id={{test_emp_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤9-启用员工成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤10：退出登录 =====
// POST /admin/employee/logout
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤10-退出成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
