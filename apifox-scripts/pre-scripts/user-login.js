// ============================================================
// C端公共前置脚本 - 自动登录获取 User Token
// 使用方法：粘贴到 Apifox C端文件夹的「前置操作 → 自定义脚本」
// ============================================================
// 注意：C端使用微信登录，测试环境需要 Mock 微信接口
// 方案1：直接在数据库插入测试用户，手动设置 token
// 方案2：修改后端代码，测试环境跳过微信验证

const token = pm.environment.get("user_token");
const tokenTime = pm.environment.get("user_token_time");
const now = Date.now();

if (token && tokenTime && (now - parseInt(tokenTime)) < 3600000) {
    console.log("使用缓存的 user_token");
} else {
    // 微信登录（测试环境使用 mock code）
    const loginUrl = pm.environment.get("base_url") + "/user/user/login";

    const loginRequest = {
        url: loginUrl,
        method: "POST",
        header: {
            "Content-Type": "application/json"
        },
        body: {
            mode: "raw",
            raw: JSON.stringify({
                code: "test_wx_code"  // 测试环境的 mock 微信授权码
            })
        }
    };

    pm.sendRequest(loginRequest, function (err, res) {
        if (err) {
            console.error("C端登录失败：", err);
            return;
        }

        const responseBody = res.json();

        if (responseBody.code === 1 && responseBody.data && responseBody.data.token) {
            pm.environment.set("user_token", responseBody.data.token);
            pm.environment.set("user_token_time", now.toString());
            pm.environment.set("user_id", responseBody.data.id);
            pm.environment.set("user_openid", responseBody.data.openid);
            console.log("C端登录成功，token 已更新");
        } else {
            console.error("C端登录失败，响应：", JSON.stringify(responseBody));
        }
    });
}
