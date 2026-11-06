# 14-toolkit · Hutool 工具库实战

用真实项目里反复出现的十二个需求，把 Hutool 常用工具类串起来练手。所有数据放进程内存，不需要数据库和 Redis，重启即清空。端口 **8016**（15-zhihu 已占用 8015）。

## 十二个案例与使用场合

| 序号 | 案例 | 主要 API | 什么时候会用到 |
| --- | --- | --- | --- |
| 1 | 图形验证码 | CaptchaUtil、MathGenerator、TimedCache | 登录/注册/发短信前的防刷，挡机器人 |
| 2 | 扫码登录 | QrCodeUtil、QrConfig、JWTUtil | PC 网页显示二维码、手机扫码确认的登录方式 |
| 3 | 商品海报 | Img、ImgUtil、QrCodeUtil | 分享海报、活动图，运营直接发朋友圈的合成图 |
| 4 | 短链服务 | RandomUtil、TimedCache、Validator | 短信里的短链接（长链被拆行/拦截）、渠道点击统计 |
| 5 | Excel 导入导出 | ExcelWriter、ExcelReader、@Alias | 后台报表导出给财务、批量导入用户填的表格 |
| 6 | 分片上传 | FileUtil、DigestUtil | 几百 MB 大文件上传、断点续传、MD5 完整性校验 |
| 7 | 上传安全 | FileTypeUtil、ImgUtil、ZipUtil | 按文件头挡住改扩展名的脚本、图片压缩、多文件打包下载 |
| 8 | 开放接口签名 | SecureUtil.hmac、MessageDigest | 给第三方调用的 API：防篡改、防重放（HMAC+时间窗+nonce） |
| 9 | 敏感数据 | AES、BCrypt、DesensitizedUtil、IdcardUtil | 手机号/身份证加密存储、密码哈希、按角色脱敏展示 |
| 10 | 系统面板 | OshiUtil、ManagementFactory | 运维监控页：CPU/内存/磁盘/JVM 一屏看完 |
| 11 | 接口巡检 | HttpRequest、ThreadUtil | 定时并发探测一批地址通不通、慢不慢 |
| 12 | 文本清洗 | ReUtil、SensitiveUtil(DFA) | 内容审核：提取并打码手机号/邮箱/IP/身份证，敏感词过滤 |

## 骨架

- `common/`：Result 统一响应、ErrorCode 分段错误码（41001 起）、BusinessException、全局异常处理（业务码映射真实 HTTP 状态码，不骗监控）
- `config/ToolkitProperties`：所有密钥/有效期/目录外置到 `application.yml` 的 `toolkit` 前缀
- 启动类设了 `java.awt.headless=true`（画图依赖 AWT）

## 依赖注意

Hutool 有三块功能依赖的是可选包，不引不会编译报错，而是运行时抛 `NoClassDefFoundError`：

- 二维码 → zxing（core + javase）
- Excel → poi-ooxml
- 系统信息 → oshi-core

## 启动

```bash
mvn -pl 14-toolkit spring-boot:run
```

看到 `Tomcat started on port 8016` 即可。验证全局异常处理：

```bash
curl --noproxy '*' -i http://127.0.0.1:8016/toolkit/nothing
# 404 + {"code":40400,...,"message":"路径不存在：/toolkit/nothing"}
```

## 快速体验（每条都已实测）

```bash
# 1 验证码：响应里带答案（debug-code 开关，仅练习用）
curl --noproxy '*' "http://127.0.0.1:8016/toolkit/captcha?type=math"

# 2 扫码登录：create -> status -> scan -> confirm（confirm 后返回 JWT）
curl --noproxy '*' -X POST http://127.0.0.1:8016/toolkit/qr-login/create

# 3 海报：直接返回 600x800 PNG
curl --noproxy '*' -X POST -F "title=秋季新品" -F "price=2999.00" \
  http://127.0.0.1:8016/toolkit/poster -o poster.png

# 4 短链：创建后 GET /toolkit/s/{code} 返回 302
curl --noproxy '*' -X POST http://127.0.0.1:8016/toolkit/short-link \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/a/very/long/path?x=1&y=2"}'

# 5 Excel：导出/模板/导入（错误行会生成标红明细供下载）
curl --noproxy '*' -o orders.xlsx http://127.0.0.1:8016/toolkit/excel/export
curl --noproxy '*' -X POST http://127.0.0.1:8016/toolkit/excel/import -F "file=@template.xlsx"

# 6 分片：init -> upload(0..n-1) -> status(断点续传) -> merge(MD5 校验) -> download
# 7 上传安全：/file/check 识破 PDF 改名 jpg；/file/compress 压图；/file/zip 打包

# 8 签名：/sign/demo 生成一组参数，复制到 /sign/order 的四个请求头即可

# 9 敏感数据：创建客户后 GET /{id}?role=agent 看脱敏，role=admin 看明文，/{id}/raw 看密文

# 10 面板页：http://127.0.0.1:8016/system.html（注意不带 /toolkit 前缀）

# 11 巡检
curl --noproxy '*' -X POST http://127.0.0.1:8016/toolkit/inspect \
  -H 'Content-Type: application/json' \
  -d '{"urls":["http://127.0.0.1:8016/toolkit/system","not-a-url"]}'

# 12 文本清洗：/text/extract 提取+脱敏，/text/sensitive 敏感词（DFA）
```

## 生产边界（教程第 15 章）

这十二个案例为了单机可跑：缓存全用 TimedCache、文件放本地临时目录、nonce/会话在内存。上生产的替换方向：缓存换 Redis（多实例共享）、文件进对象存储、密钥进配置中心/KMS、系统面板和巡检加鉴权与白名单（防信息泄露和 SSRF）、验证码 `debug-code` 必须关闭。

## 已知留白（教程练习题）

文本提取里 `phones` 会多出身份证中段的 11 位数字（如 `19491231002`），脱敏文本是对的，提取列表不准——教程第 17 章第 4 题，用前后断言 `(?<!\d)...(?!\d)` 修复。

## Apifox 图片可视化（老师要求的后置脚本）

两个返回 Base64 图片的接口已通过 Apifox 开放 API 装好后置操作（项目 backend-learning，文件夹 14-toolkit）：

- `GET /toolkit/captcha`（验证码）
- `POST /toolkit/qr-login/create`（扫码登录二维码）

脚本即老师给的 `pm.visualizer.set` 模板。使用：Apifox 里打开接口 → 发送 → 响应区切到「**可视化**」Tab，图片直接显示。`/toolkit/poster` 返回的是 PNG 二进制，Apifox 会自动预览，无需脚本。

手动给其他接口加同款脚本的方法：接口编辑页 → 「后置操作」→ 添加 → 「自定义脚本」→ 粘贴：

```js
var resp = {
    response: pm.response.json()
}
var template = `
<html>
    <img src="{{response.data.image}}" />
</html>
`;
pm.visualizer.set(template, resp);
```
