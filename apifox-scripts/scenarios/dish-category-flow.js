// ============================================================
// 业务流测试场景：菜品-分类关联测试
// 场景：新增分类 → 新增菜品关联分类 → 尝试删除分类（应失败）→ 删除菜品 → 删除分类（成功）
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

// ===== 步骤2：新增测试分类 =====
// POST /admin/category
// Header: token: {{admin_token}}
// Body:
// {
//     "name": "自动化测试分类",
//     "type": 1,
//     "sort": 99
// }
// 后置脚本：
pm.test("步骤2-新增分类成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤3：查询分类获取id =====
// GET /admin/category/page?name=自动化测试分类&page=1&pageSize=10
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤3-查到测试分类", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.total).to.be.at.least(1);
});
const catPageRes = pm.response.json();
const testCat = catPageRes.data.records.find(c => c.name === "自动化测试分类");
if (testCat) {
    pm.environment.set("test_cat_id", testCat.id);
}

// ===== 步骤4：新增菜品关联该分类 =====
// POST /admin/dish
// Header: token: {{admin_token}}
// Body:
// {
//     "name": "自动化测试菜品",
//     "categoryId": {{test_cat_id}},
//     "price": 2999,
//     "description": "测试用菜品",
//     "status": 1,
//     "flavors": [
//         {"name": "辣度", "value": "[\"不辣\",\"微辣\",\"中辣\"]"}
//     ]
// }
// 后置脚本：
pm.test("步骤4-新增菜品成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤5：查询菜品获取id =====
// GET /admin/dish/page?name=自动化测试菜品&page=1&pageSize=10
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤5-查到测试菜品", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.total).to.be.at.least(1);
});
const dishPageRes = pm.response.json();
if (dishPageRes.data.records.length > 0) {
    pm.environment.set("test_dish_id", dishPageRes.data.records[0].id);
}

// ===== 步骤6：尝试删除分类（应失败，有关联菜品） =====
// DELETE /admin/category?id={{test_cat_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤6-删除分类失败（有关联）", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(0); // 业务失败
});
pm.test("步骤6-返回关联错误提示", function () {
    const res = pm.response.json();
    pm.expect(res.msg).to.be.a("string").and.not.empty;
});

// ===== 步骤7：先停售菜品 =====
// POST /admin/dish/status/0?id={{test_dish_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤7-停售菜品成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤8：删除菜品 =====
// DELETE /admin/dish?ids={{test_dish_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤8-删除菜品成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤9：再次删除分类（应成功） =====
// DELETE /admin/category?id={{test_cat_id}}
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤9-删除分类成功", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});

// ===== 步骤10：验证分类已删除 =====
// GET /admin/category/page?name=自动化测试分类&page=1&pageSize=10
// Header: token: {{admin_token}}
// 后置脚本：
pm.test("步骤10-分类已不存在", function () {
    pm.response.to.have.status(200);
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
    pm.expect(res.data.total).to.equal(0);
});
