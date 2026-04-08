# Task 6 Spec：Alert 与 WorkOrder 模块开发（预警处置闭环）

## 1. 背景与目标

当前工程已完成：
- Task 1：多模块骨架、统一响应、统一异常、安全基础接入；
- Task 2：核心表、Entity、Mapper（包含 `ops_alert_event`、`ops_work_order`、`ops_work_order_log`）；
- Task 3：权限与系统基座；
- Task 4：资产与设备档案；
- Task 5：Monitor 实时监测 + 异常落地 `ops_alert_event`。

本 spec 聚焦 Task 6，目标是交付一期可演示、可联调的“告警到处置”闭环能力：

1. 提供告警事件分页查询、确认、关闭接口；
2. 提供工单创建与流转能力，并与告警建立来源关联；
3. 保证工单状态每次变化都写入 `ops_work_order_log`；
4. 形成“告警发现 -> 处置执行 -> 闭环留痕”的最小业务链路。

---

## 2. 范围界定

### 2.1 In Scope

- `ops_alert_event`：
  - `GET /api/alerts/events` 分页查询（支持状态、等级、时间区间、区域）；
  - `PUT /api/alerts/events/{id}/confirm` 告警确认；
  - `PUT /api/alerts/events/{id}/close` 告警关闭。
- `ops_work_order`：
  - `POST /api/workorders` 工单创建（支持从告警创建）；
  - `GET /api/workorders` 工单分页查询；
  - `PUT /api/workorders/{id}/assign` 派单；
  - `PUT /api/workorders/{id}/start` 开始处理；
  - `PUT /api/workorders/{id}/finish` 完成工单。
- `ops_work_order_log`：
  - 工单状态每次变更必须插入一条日志（操作类型、前后状态、操作时间、描述）。
- 与 Task 5 衔接：
  - 使用 `ops_alert_event` 作为工单来源（`source_type=ALERT_EVENT`、`source_id=alert_event.id`）。

### 2.2 Out of Scope

- 复杂规则引擎、升级/抑制/关联分析；
- SLA 计时、催办、超时升级；
- 工单多级审批与多人协同编排；
- 前端可视化工作台细节（本期只定义后端契约）。

---

## 3. 方案比较与选择

由于你当前离线，本轮采用默认推荐策略并先行收敛方案。

### 方案 A：告警与工单解耦的最小 CRUD

- 特点：告警只负责状态管理，工单独立创建，不强制来源关联。
- 优点：实现简单、改动最小。
- 缺点：无法形成完整闭环追溯，Task 6 价值不足。

### 方案 B：告警驱动的处置闭环（推荐）

- 特点：告警可直接创建工单，工单全流程记录日志，支持回写告警状态。
- 优点：满足“预警处置闭环”目标，且可与 Task 5 自然衔接。
- 缺点：状态联动与幂等处理复杂度高于方案 A。

### 方案 C：引入通用流程引擎

- 特点：工单流转交由流程引擎编排（节点、审批、回退）。
- 优点：扩展性强。
- 缺点：一期过重，超出当前任务边界。

**结论：采用方案 B。**

---

## 4. 总体架构设计

### 4.1 分层职责

- **app（Controller）**  
  告警与工单接口、参数校验、统一响应返回。
- **domain（Service）**  
  告警状态机、工单状态机、告警-工单联动、日志写入编排。
- **infra（Mapper）**  
  复用 `OpsAlertEventMapper`、`OpsWorkOrderMapper`、`OpsWorkOrderLogMapper` 持久化。
- **common/security**  
  复用统一异常和鉴权上下文（操作人、区域）。

### 4.2 业务链路

1. 监测异常由 Task 5 写入 `ops_alert_event(alert_status=1)`；
2. 处置人员查询告警列表；
3. 对告警执行确认（`alert_status: 1 -> 2`）；
4. 从告警创建工单（建立 `source_type/source_id`）；
5. 工单在派单/开始/完成流转；
6. 每次流转写 `ops_work_order_log`；
7. 工单完成后可关闭告警（`alert_status: 2 -> 3`）。

### 4.3 状态模型（一期最小）

- 告警状态（`ops_alert_event.alert_status`）：
  - `1`：待处理
  - `2`：已确认
  - `3`：已关闭
- 工单状态（`ops_work_order.work_status`）：
  - `1`：待派单
  - `2`：处理中
  - `3`：已完成

---

## 5. 组件与接口设计

### 5.1 AlertEventController

- `GET /api/alerts/events`
  - 参数：`alertStatus`、`alertLevel`、`regionCode`、`startTime`、`endTime`、`pageNum`、`pageSize`
  - 返回：告警分页列表。
- `PUT /api/alerts/events/{id}/confirm`
  - 行为：将状态从 `1` 改为 `2`，写入 `confirm_time`。
- `PUT /api/alerts/events/{id}/close`
  - 行为：将状态改为 `3`，写入 `close_time`。

### 5.2 WorkOrderController

- `POST /api/workorders`
  - 入参：`sourceType`、`sourceId`、`title`、`targetType`、`targetId`、`regionCode`、`assigneeUserId`、`expectFinishTime`。
  - 行为：创建工单并记录首条日志（`action_type=CREATE`）。
- `GET /api/workorders`
  - 参数：`workStatus`、`sourceType`、`sourceId`、`regionCode`、`assigneeUserId`、`pageNum`、`pageSize`。
- `PUT /api/workorders/{id}/assign`
  - 行为：状态更新为 `1`（若未派单则设置处理人），写日志（`ASSIGN`）。
- `PUT /api/workorders/{id}/start`
  - 行为：状态 `1 -> 2`，写日志（`START`）。
- `PUT /api/workorders/{id}/finish`
  - 行为：状态 `2 -> 3`，写日志（`FINISH`，可写 `result_summary`）。

### 5.3 领域服务

- `AlertEventService`
  - `pageQuery(AlertEventQuery)`；
  - `confirm(alertId, operatorUserId)`；
  - `close(alertId, operatorUserId)`。
- `WorkOrderService`
  - `create(CreateWorkOrderCommand)`；
  - `assign(AssignWorkOrderCommand)`；
  - `start(StartWorkOrderCommand)`；
  - `finish(FinishWorkOrderCommand)`。
- `WorkOrderLogService`
  - `appendStatusLog(LogCommand)`：封装统一日志写入，禁止控制器直写 Mapper。

---

## 6. 数据模型复用与约束

复用表：
- `ops_alert_event`
- `ops_work_order`
- `ops_work_order_log`

关键约束：
- 工单来源建议固定枚举：`ALERT_EVENT`（一期主路径）；
- 工单状态变更必须附带一条日志，日志中的 `before_status`、`after_status` 必填；
- 告警关闭前建议校验：若存在未完成工单则拒绝关闭（一期可按配置放宽，默认开启校验）。

字段映射建议：
- `ops_work_order.source_id = ops_alert_event.id`（当来源为告警时）；
- `ops_work_order_log.work_order_id = ops_work_order.id`；
- `ops_alert_event.work_order_id` 在创建告警来源工单时可回填（便于反查）。

---

## 7. 错误处理与安全策略

- 统一复用 `ApiResponse` + `GlobalExceptionHandler`；
- 业务错误建议：
  - 告警不存在/状态不允许：`BIZ_4001`；
  - 工单不存在/状态流转非法：`BIZ_4001`；
  - 未完成工单不允许关闭告警：`BIZ_4001`；
  - 参数非法（如时间区间）：`REQ_4000`。
- 安全策略：
  - 全部 Alert/WorkOrder 接口默认鉴权；
  - 角色建议：运维/管理员可写，普通只读；
  - 分页查询默认带区域过滤（管理员全量，非管理员按区域）。

---

## 8. 测试设计

### 8.1 合同测试（app）

- `AlertEventApiContractTest`
  - 告警分页查询可用；
  - 告警确认成功，状态与时间字段变化正确；
  - 未完成工单存在时关闭告警返回业务错误。
- `WorkOrderApiContractTest`
  - 告警来源工单创建成功；
  - 派单/开始/完成流转成功；
  - 非法状态流转被拒绝并返回业务错误。

### 8.2 领域与基础设施测试

- `WorkOrderFlowServiceTest`
  - 状态机约束生效（禁止跳跃流转）；
  - 每次状态变更均写入日志。
- `AlertWorkOrderLinkageTest`
  - 告警来源工单创建后可回查关联；
  - 工单完成后告警关闭链路正确。

---

## 9. 验收标准（DoD）

1. 告警分页、确认、关闭接口可用；
2. 工单创建与派单/开始/完成流转可用；
3. 所有工单状态变更均有日志记录；
4. 告警与工单可建立来源关联并可追溯；
5. 新增测试通过，且不破坏既有测试。

---

## 10. 风险与后续衔接

- 风险：状态联动较多，若缺少幂等保护易出现重复操作；
- 缓解：对关键变更接口增加状态前置校验与幂等判断；
- 与后续衔接：
  - 可在 Task 7 扩展 SLA、催办、升级规则；
  - 可在前端工作台直接消费本任务的告警/工单查询与日志接口。

---

## 11. 默认决策说明

因当前未收到额外约束反馈，本 spec 采用“告警驱动工单”的默认闭环策略：告警可直接创建工单，工单流转强制写日志，最终支持告警关闭。若后续希望改为“必须确认后才允许建单”或“支持独立无告警工单为主”，可在本 spec 上做增量调整。
