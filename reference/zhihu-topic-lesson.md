# 知乎专题页面前后端交互（作业说明整理）

来源：https://www.yuque.com/mqxu/zzb5xl/odvx0m320urnr1sf?singleDoc
整理自文档正文（原文大量细节在截图中，本文是可检索的文字骨架）。

## 参考效果
https://www.zhihu.com/special/all

## 数据准备
1. 打开知乎专题页 → F12 → Network → Fetch/XHR → 滚动页面，找到 `limit=10&offset=xxx` 的请求。
2. 直接访问接口看数据（改 limit / offset 翻页）：
   `https://www.zhihu.com/api/v4/news_specials/list?limit=10&offset=0`
3. 页面返回的 JSON 里取 `data` 数组（去掉外层 `{paging, data}` 包装），存成 `data.json`。
4. 用 https://data.page/json/csv 把 data.json 转成 csv 下载。
5. 用 AI 清掉空行，得到 `data_cleaned.csv`。
6. Navicat → 库 → 表 → 右键「导入向导」→ CSV → 选文件 → 日期格式选 YMD（年月日）→ 目标表名填 `t_special` → 指定主键 `id`、调整可能超长的字段长度 → 追加或更新 → 开始导入。
7. 导入后删除/修改一些字段，得到最终 `t_special` 表。

## 后端实现（模块 zhihu-api）
- 新建 `zhihu-api` 模块，添加 MySQL 驱动、MyBatis Plus 等依赖。
- 建立目录结构，编写启动主类。
- `application.yml` 配置数据源和 MyBatis Plus。
- `entity` 子包：专题实体类（与表字段对应）。
- `mapper` 包：`SpecialMapper` 接口，继承 MyBatis Plus 通用 Mapper（BaseMapper）。
- `service` 包：`SpecialService` 接口，声明「获取所有数据」方法。
- `service.impl` 子包：`SpecialServiceImpl` 实现类。
- 对 `SpecialService` 写单元测试。
- `common` 包：`ResponseResult`，统一封装返回结果。
- `controller` 包：`SpecialController`，接口 `GET http://localhost:8080/api/v1/special/all`。

## 前端（项目 zhihu-app）
- 用 Vite 创建前端项目：项目名 `zhihu-app`，框架选 Vue + TypeScript，安装依赖后 `npm run dev`（默认 http://localhost:5173/）。
- `main.ts` 注释掉引入 `style.css` 的代码；`App.vue` 去掉无用代码。
- 项目根目录安装 axios。
- `components/ZhiHu.vue`：用 axios 调用后端接口，把数据赋给响应式数组。
- `App.vue` 引入 `ZhiHu.vue`。
- 跨域问题：浏览器同源策略（协议/主机/端口任一不同即跨域）。
  - 解决：后端新建 `config` 子包，加跨域配置类 `WebConfig`，配置 `addCorsMappings`；重启后端后跨域解决。
- `ZhiHu.vue` 中布局 + 样式 + 渲染数据；通过数据表「是否关注」字段验证条件渲染。

## 扩展练习
### 练习 1：手动添加分类字段
- 加 `category` 字段，按 `title` / `introduction` 内容给每个专题分类（如「科技」「文化」）。
- 文档做法：标题和描述没规律，改为按「关注数量」简单分类。

### 练习 2：按分类查询
- 后端：`SpecialMapper.xml` 写 `selectByCategory` SQL，`SpecialMapper` 声明方法，`SpecialService#getByCategory` + 实现，`SpecialController` 加带参接口。
- 测试：`http://localhost:8080/api/v1/special/category/科技`（URL 编码 `%E7%A7%91%E6%8A%80`）。
- 前端：`ZhiHu.vue` 加下拉框 `selectedCategory`，按选择请求对应数据。

### 练习 3：分页
- 后端：`SpecialMapper.xml` 分页 SQL + 接口方法；`SpecialService` 支持分页；`SpecialController` 加 `limit` / `offset` 分页接口。
- 测试：`http://localhost:8080/api/v1/special/page?limit=3&offset=0`。
- 前端：`ZhiHu.vue` 加上一页/下一页按钮，`script setup` 里定义 `limit`、`offset`，用 `onMounted` 首次调用 `fetchByPage`。
- `onMounted`：Vue 3 组合式 API 生命周期钩子，组件挂载到 DOM 后执行（对应 Vue 2 的 `mounted`），常用于发请求、DOM 操作、订阅事件。

### 练习 4：美化与修 bug
- 美化下拉框、分页按钮。
- 第一页禁用「上一页」、最后一页禁用「下一页」；后端返回总条数。
- 其他自由发挥。

## 知乎接口数据字段（实测 2026-10 抓取，供建表参考）
请求 `https://www.zhihu.com/api/v4/news_specials/list?limit=10&offset=0`，返回：
- 外层：`paging { is_end, total, next, previous }`、`data [ ... ]`（total 约 2216）
- data 元素字段：
  - `id`：字符串，专题 id（如 "2086116797393257830"）
  - `title`：专题标题
  - `introduction`：专题简介
  - `banner`：头图 URL（`https://pic2.zhimg.com/...`）
  - `view_count`：浏览量
  - `followers_count`：关注量
  - `is_following`：是否已关注（布尔，前端条件渲染用）
  - `updated`：时间戳（秒，如 1790151017）
  - `section_list`：[{ section_id, section_title }] 子栏目数组
