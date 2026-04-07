[cite_start]借助 AI 编程助手（如 Copilot Pro 和 Antigravity）来落地这种规范的结构化项目是非常高效的策略。针对“一期：工程闭环版”的目标，我们需要剥离复杂的空间 GIS 和研究型算法，集中精力实现系统可上线演示并支撑基本业务流转的能力 [cite: 83][cite_start]。一期核心覆盖门户、权限、基础档案、实时监测、告警中心、工单处置和综合报表 [cite: 83]。

为了让 AI 助手能够输出高质量、不跑偏的代码，我们需要将需求拆解为职责单一、边界清晰的“微任务”。以下是为你规划的后端与前端的一期开发小任务划分。

### 一、 后端开发微任务划分 (Spring Boot 体系)


[cite_start]后端建议采用 `Controller -> Service -> Domain/Manager -> Mapper` 的分层形式 [cite: 124]，这非常适合通过 AI 批量生成样板代码。

#### 任务 1：项目骨架与基础设施搭建
* [cite_start]**目标：** 初始化 Spring Boot 3.x + JDK 17 工程 [cite: 115]。
* **AI 提示词拆解：**
    1.  [cite_start]配置 Maven/Gradle 依赖：引入 Spring Web、MyBatis-Plus、Spring Security、JWT、Redis、MapStruct、Lombok [cite: 115]。
    2.  [cite_start]配置统一返回结构：封装包含 `code`、`message`、`data`、`timestamp` 的通用响应体 [cite: 131]。
    3.  [cite_start]配置全局异常处理：捕获业务异常并返回统一格式，添加接口参数校验（Hibernate Validator）配置 [cite: 115]。
    4.  [cite_start]配置 MyBatis-Plus：开启分页插件，设置雪花算法 ID 生成策略 `@TableId(type = IdType.ASSIGN_ID)` [cite: 119]。

#### 任务 2：核心物理表建立与实体类生成
* [cite_start]**目标：** 落地一期高优先级的数据库表 [cite: 267]。
* **AI 提示词拆解：**
    1.  [cite_start]生成建表 SQL：根据文档生成 `sys_user`、`sys_role`、`asset_network`、`asset_pipe_section`、`asset_node`、`asset_facility`、`iot_device`、`iot_measure_point`、`ts_measure_current`、`ts_measure_history`、`ops_alert_rule`、`ops_alert_event`、`ops_work_order`、`ops_work_order_log` 这 14 张核心物理表的 MySQL 脚本 [cite: 267, 154][cite_start]。所有表需包含标准的审计字段（如 `created_by`, `created_time` 等） [cite: 149]。
    2.  生成实体类与 Mapper：利用 MyBatis-Plus 代码生成器或 AI 批量生成上述表的 Entity 类和基本的 Mapper 接口。


#### 任务 3：Auth 与 System 模块开发 (权限与基座)
* [cite_start]**目标：** 实现登录鉴权、角色控制、数据权限 [cite: 154]。
* **AI 提示词拆解：**
    1.  [cite_start]**JWT 认证组件：** 编写 JWT 签发与解析工具类，配置 Spring Security 过滤器链放行登录接口 [cite: 115]。
    2.  [cite_start]**Auth 接口：** 实现 `/api/auth/login` 接口，根据用户名密码校验并返回 Token；实现获取当前登录用户路由菜单的接口 [cite: 123]。
    3.  [cite_start]**用户与角色 CRUD：** 编写 `sys_user` 和 `sys_role` 的增删改查及分页接口，处理用户与角色的多对多绑定逻辑 [cite: 165]。

#### 任务 4：Asset 与 Device 模块开发 (资产底座)
* [cite_start]**目标：** 实现管网、管段等静态资产与设备的建档管理 [cite: 154]。
* **AI 提示词拆解：**
    1.  [cite_start]**业务编码服务：** 编写一个自定义编码生成服务，按照“区域-类别-层级-流水号”规则生成资产统一编码 [cite: 119]。
    2.  [cite_start]**资产 CRUD 接口：** 针对管网 (`asset_network`)、管段 (`asset_pipe_section`) 提供分页查询、新增、修改、删除接口 [cite: 123]。
    3.  [cite_start]**设备与测点 CRUD 接口：** 实现设备 (`iot_device`) 和测点 (`iot_measure_point`) 的档案管理接口，并在新增设备时生成独立的设备编码 [cite: 191]。

#### 任务 5：Monitor 模块开发 (实时监测模拟)
* [cite_start]**目标：** 支撑实时监测和历史曲线 [cite: 154]。
* **AI 提示词拆解：**
    1.  **数据接收接口：** 编写一个模拟网关上传数据的 HTTP 接口，接收设备 ID、指标类型和当前值。
    2.  [cite_start]**数据落库逻辑：** 将接收到的值写入历史表 `ts_measure_history`，并执行 `UPSERT`（更新或插入）操作刷新当前实时值表 `ts_measure_current` [cite: 152]。
    3.  [cite_start]**WebSocket 推送：** 集成 Spring WebSocket + STOMP [cite: 115][cite_start]，当实时值表更新且发生异常时，向前端广播状态更新消息 [cite: 132]。

#### 任务 6：Alert 与 WorkOrder 模块开发 (预警处置闭环)
* [cite_start]**目标：** 支撑规则触发、预警分级、建议生成和工单闭环 [cite: 154]。
* **AI 提示词拆解：**
    1.  [cite_start]**告警事件管理：** 实现 `ops_alert_event` 的分页查询接口，以及告警确认、告警关闭的状态变更接口 [cite: 230]。
    2.  [cite_start]**工单流转逻辑：** 实现工单 (`ops_work_order`) 的新增派单接口。重点是：状态每发生一次变更，必须向 `ops_work_order_log` 表中插入一条操作日志 [cite: 242]。

---

### 二、 前端开发微任务划分 (Vue3 体系)


前端的任务需要围绕组件复用展开，避免每个页面从零手写。

#### 任务 1：工程脚手架与基础 UI
* [cite_start]**目标：** 搭建具备路由与状态管理的工程化框架 [cite: 100]。
* **AI 提示词拆解：**
    1.  [cite_start]使用 Vite 初始化 Vue3 + TypeScript 项目，引入 Element Plus、Vue Router、Pinia、Axios、ECharts [cite: 102]。
    2.  [cite_start]封装 Axios 请求实例，统一处理 Token 携带、响应拦截（解析后端统一的 `code`/`data` 结构）以及异常提示 [cite: 102]。
    3.  [cite_start]配置 SCSS 全局变量，统一定义不同告警等级（如红、橙、黄、蓝）的颜色标量 [cite: 102]。

#### 任务 2：通用组件沉淀
* [cite_start]**目标：** 封装高频复用的基础组件，为后续拼装页面做准备 [cite: 104]。
* **AI 提示词拆解：**
    1.  封装 `BasicTable` 组件：结合 `ElTable` 和 `ElPagination`，传入接口 API 即可自动渲染分页列表。
    2.  封装 `SearchPanel` 组件：结合 `ElForm`，提供统一的查询条件输入和重置按钮。
    3.  [cite_start]封装 `StatusTag` 组件：传入状态字典值，自动渲染对应颜色和文字的 `ElTag` [cite: 106, 108]。

#### 任务 3：后台管理页面组装
* **目标：** 完成资产、设备、权限等标准化表单与列表页。
* **AI 提示词拆解：**
    1.  [cite_start]使用沉淀的组件，快速组装“管段档案管理”和“设备档案管理”页面，实现上方查询、中央列表、右侧抽屉查看详情（包含基础属性和挂载的测点）的布局 [cite: 106]。
    2.  开发登录页与动态菜单栏，根据后端的菜单权限接口渲染左侧路由。

#### 任务 4：监测分析与预警处置中心 (核心业务视图)
* [cite_start]**目标：** 实现实时监测大盘与工单闭环操作界面 [cite: 33]。
* **AI 提示词拆解：**
    1.  [cite_start]**趋势图表页：** 引入 ECharts 封装 `TrendChart` 组件，提供顶部日期筛选，展示历史时序数据的曲线波动 [cite: 106, 108]。
    2.  [cite_start]**WebSocket 客户端：** 集成 `@stomp/stompjs` [cite: 102]，监听后端的告警广播频道，实现前端右下角的实时告警弹窗提示。
    3.  [cite_start]**告警与工单工作台：** 使用 `ElTabs` 分割“未处理/处理中/已闭环”的告警列表 [cite: 106][cite_start]。使用弹窗 (`ElDialog`) 结合时间轴组件 (`ElTimeline`) 渲染工单的处理流转日志 (`ops_work_order_log`) [cite: 106, 242]。

**下一步建议：** 你可以先把“后端任务 1”和“后端任务 2”发给 AI 助手，让它先把基础环境和核心建表 SQL 跑出来，确认主键 ID 策略和统一结构无误后，再推进后续的增删改查与业务逻辑。