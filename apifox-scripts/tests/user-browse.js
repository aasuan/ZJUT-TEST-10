// ============================================================
// C端浏览接口测试脚本
// 接口：/user/category, /user/dish, /user/setmeal, /user/shop
// ============================================================

// ==================== GET /user/category/list ====================
// 后置脚本 - 查询分类列表
pm.test("C端分类列表 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("C端分类列表 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("C端分类列表 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
// 保存第一个分类id供后续使用
const catRes = pm.response.json();
if (catRes.code === 1 && catRes.data && catRes.data.length > 0) {
    pm.environment.set("test_category_id", catRes.data[0].id);
}

// ==================== GET /user/dish/list ====================
// 后置脚本 - 根据分类查菜品
pm.test("C端菜品列表 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("C端菜品列表 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("C端菜品列表 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
pm.test("C端菜品列表 - 菜品包含口味信息", function () {
    const res = pm.response.json();
    if (res.data.length > 0) {
        const dish = res.data[0];
        pm.expect(dish).to.have.property("id");
        pm.expect(dish).to.have.property("name");
        pm.expect(dish).to.have.property("price");
        pm.expect(dish).to.have.property("flavors");
    }
});
pm.test("C端菜品列表 - 只返回起售菜品", function () {
    const res = pm.response.json();
    res.data.forEach(function (dish) {
        pm.expect(dish.status).to.equal(1);
    });
});

// ==================== GET /user/setmeal/list ====================
// 后置脚本 - 根据分类查套餐
pm.test("C端套餐列表 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("C端套餐列表 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("C端套餐列表 - 返回数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});
pm.test("C端套餐列表 - 只返回起售套餐", function () {
    const res = pm.response.json();
    res.data.forEach(function (setmeal) {
        pm.expect(setmeal.status).to.equal(1);
    });
});

// ==================== GET /user/setmeal/dish/{id} ====================
// 后置脚本 - 查询套餐包含的菜品
pm.test("套餐菜品 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("套餐菜品 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("套餐菜品 - 返回菜品数组", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.an("array");
});

// ==================== GET /user/shop/status ====================
// 后置脚本 - C端获取店铺状态
pm.test("C端店铺状态 - 状态码200", function () {
    pm.response.to.have.status(200);
});
pm.test("C端店铺状态 - 业务码为1", function () {
    const res = pm.response.json();
    pm.expect(res.code).to.equal(1);
});
pm.test("C端店铺状态 - 返回状态值", function () {
    const res = pm.response.json();
    pm.expect(res.data).to.be.oneOf([0, 1]);
});
