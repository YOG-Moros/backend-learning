# 15-zhihu · 知乎专题页面前后端交互

对应作业《知乎专题页面前后端交互》，参考效果 <https://www.zhihu.com/special/all>。
后端接口在本模块（端口 **8015**），前端在 `D:\first_one\zhihu-app`（Vite + Vue3 + TypeScript）。

## 1. 数据来源与入库

知乎接口（免登录，直接 GET）：

```
https://www.zhihu.com/api/v4/news_specials/list?limit=50&offset=0
```

外层是 `paging` + `data[]`，用到的字段：

| 接口字段 | 表字段 | 说明 |
| --- | --- | --- |
| `id` | `id` VARCHAR(32) | 雪花 ID 字符串，超过 JS 安全整数，前端必须当字符串处理 |
| `title` | `title` | 标题 |
| `introduction` | `introduction` | 简介 |
| `banner` | `banner` | 头图 URL |
| `view_count` | `view_count` BIGINT | 浏览量 |
| `followers_count` | `followers_count` INT | 关注量 |
| `is_following` | `is_following` TINYINT(1) | 是否关注，1/0 |
| —（新加） | `category` | 分类：热门 / 关注中 / 小众 |

`updated`（秒级时间戳）和 `section_list`（栏目数组）没有入库价值，按作业要求删掉。

**分类规则（练习 1）**：专题标题没有规律，改成按关注量三分位分类。
97 条数据算出 q1 = 39、q2 = 127：

- `followers_count >= 127` → `热门`（33 条）
- `followers_count >= 39` → `关注中`（32 条）
- 其余 → `小众`（32 条）

**导入**：`sql/zhihu.sql` 里是建库 + 建表 + 97 条 `INSERT`，直接执行即可：

```bash
mysql -uroot -p${ZHIHU_DB_PASSWORD} --default-character-set=utf8mb4 < sql/zhihu.sql
```

> 用 Navicat 导入向导也可以：日期列选 `YMD`、id 勾主键、导入模式选「追加或更新」。
> 命令行导入时注意 `--default-character-set=utf8mb4`，否则中文标题会变问号。

## 2. 模块结构

```
15-zhihu
├── pom.xml
├── sql/zhihu.sql                       # 建库建表 + 数据
└── src
    ├── main
    │   ├── java/top/cch/zhihu
    │   │   ├── ZhihuApplication.java    # @SpringBootApplication + @MapperScan
    │   │   ├── common
    │   │   │   ├── ResponseResult.java  # { code, message, data }
    │   │   │   └── PageResult.java      # list/total/limit/offset/hasNext/hasPrev
    │   │   ├── config/WebConfig.java    # CORS
    │   │   ├── controller/SpecialController.java
    │   │   ├── entity/Special.java      # @TableName("t_special")
    │   │   ├── mapper/SpecialMapper.java
    │   │   └── service
    │   │       ├── SpecialService.java
    │   │       └── impl/SpecialServiceImpl.java
    │   └── resources
    │       ├── application.yml
    │       └── mapper/SpecialMapper.xml # 分类 / 分页手写 SQL
    └── test/java/top/cch/zhihu/service/SpecialServiceImplTest.java
```

`SpecialMapper extends BaseMapper<Special>`，通用单表操作直接调（`selectList` / `selectById` …），
只有「按分类」「分页」「分类列表」三条写进 XML。

## 3. 接口

统一响应：`{ "code": 200, "message": "ok", "data": ... }`，前端记得取 `res.data.data`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/special/all` | 全部专题，按关注量倒序 |
| GET | `/api/v1/special/category/{category}` | 按分类查询（练习 2），如 `/api/v1/special/category/热门` |
| GET | `/api/v1/special/page?limit=3&offset=0` | 分页（练习 3） |
| GET | `/api/v1/special/categories` | 分类列表，给下拉框用 |

分页返回示例：

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "list": [ { "id": "2086116797393257830", "title": "…" } ],
    "total": 97,
    "limit": 3,
    "offset": 0,
    "hasNext": true,
    "hasPrev": false
  }
}
```

`hasNext` / `hasPrev` 后端算好，前端直接拿来禁用按钮；`total` 也能自己算 `offset + limit < total`。

分类名是中文，但不用手工转码：Spring Boot 3+ 默认按 UTF-8 解析请求行，
`@PathVariable String category` 拿到的直接就是「热门」这样的正确字符
（早期版本才需要 `new String(bytes, ISO_8859_1)` 那种写法）。
前端拼路径时用 `encodeURIComponent` 编码一下更稳妥。

## 4. 启动与验证

```bash
# 用 JDK 21（PATH 里的 java 是 1.8，不能用）
set JAVA_HOME=C:\Program Files\Java\jdk-21
mvn -q -pl 15-zhihu -am spring-boot:run
```

```bash
curl http://localhost:8015/api/v1/special/all
curl "http://localhost:8015/api/v1/special/page?limit=3&offset=0"
curl http://localhost:8015/api/v1/special/categories
```

- 端口用 **8015**：本机 8080 已被别的服务占用（作业文档里的 8080 只是示例）。
- 数据库连接：`jdbc:mysql://localhost:3306/zhihu`，`root / ${ZHIHU_DB_PASSWORD}`（来自 `05-mybatis` 的配置）。
- CORS：`WebConfig#addCorsMappings` 放开了 `/api/**`，用 `allowedOriginPatterns("*")`；
  注意 `allowCredentials(true)` 不能和 `allowedOrigins("*")` 同时用。

## 5. 单测

```bash
mvn -pl 15-zhihu test
```

`SpecialServiceImplTest` 用 Mockito 给 `SpecialMapper` 打桩，覆盖：
首页 / 末页的 `hasNext`、`hasPrev`、`limit` 夹紧到 100、`offset` 负数归零、分类查询透传。
