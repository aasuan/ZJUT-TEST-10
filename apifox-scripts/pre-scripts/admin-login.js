// ============================================================
// 管理端公共前置脚本 - 自动登录获取 Admin Token
// 使用方法：粘贴到 Apifox 管理端文件夹的「前置操作 → 自定义脚本」
// ============================================================

// 检查 token 是否已存在且未过期（避免每个请求都登录）
const token = pm.environment.get("admin_token");
const tokenTime = pm.environment.get("admin_token_time");
const now = Date.now();

// token 有效期设为 1 小时（3600000ms），过期则重新获取
if (token && tokenTime && (now - parseInt(tokenTime)) < 3600000) {
    // token 未过期，直接使用
    console.log("使用缓存的 admin_token");
} else {
    // 重新登录获取 token
    const loginUrl = pm.environment.get("base_url") + "/admin/employee/login";
    const username = pm.environment.get("admin_username") || "admin";
    const password = pm.environment.get("admin_password") || "123456";

    const loginRequest = {
        url: loginUrl,
        method: "POST",
        header: {
            "Content-Type": "application/json"
        },
        body: {
            mode: "raw",
            raw: JSON.stringify({
                username: username,
                password: password
            })
        }
    };

    pm.sendRequest(loginRequest, function (err, res) {
        if (err) {
            console.error("管理端登录失败：", err);
            return;
        }

        const responseBody = res.json();

        if (responseBody.code === 1 && responseBody.data && responseBody.data.token) {
            pm.environment.set("admin_token", responseBody.data.token);
            pm.environment.set("admin_token_time", now.toString());
            pm.environment.set("admin_id", responseBody.data.id);
            pm.environment.set("admin_name", responseBody.data.name);
            console.log("管理端登录成功，token 已更新");
        } else {
            console.error("管理端登录失败，响应：", JSON.stringify(responseBody));
        }
    });
}
