# Task 5 Spec：Monitor 模块开发（实时监测模拟）

## 1. 背景与目标

当前工程已完成：
- Task 1：多模块骨架、统一响应、异常处理、安全基础接入；
- Task 2：核心物理表、实体、Mapper（包含 `ts_measure_current`、`ts_measure_history`）；
- Task 3：权限与系统基座；
- Task 4：资产与设备档案能力，已沉淀 `iot_device`/`iot_measure_point` 基础数据。

本 spec 聚焦 Task 5，目标是交付一期“可演示、可联调”的实时监测与告警联动闭环：

1. 提供模拟网关数据上报接口；
2. 上报数据写入历史表，并对当前值表执行 UPSERT；
3. 提供当前值与历史曲线查询接口；
4. 通过 WebSocket + STOMP 广播实时状态更新消息（含异常标记）；
5. 异常值触发 `ops_alert_event` 事件落库，为 Task 6 提供告警输入。

---

## 2. 范围界定

### 2.1 In Scope

- `POST /api/monitor/ingest`：接收监测上报数据；
- 历史写入：`ts_measure_history` 追加插入；
- 当前值刷新：`ts_measure_current` 按 `point_id` 执行 UPSERT；
- 异常联动：当值越过测点阈值时写入 `ops_alert_event`（最小事件模型）；
- 查询接口：
  - `GET /api/monitor/current`（按点位/设备查询当前值）
  - `GET /api/monitor/history`（按点位 + 时间区间查询历史曲线）
- WebSocket/STOMP 推送：
  - 广播点位实时更新消息；
  - 当值越过测点阈值时广播异常状态与事件摘要。

### 2.2 Out of Scope

- MQTT/Kafka 等真实网关协议接入（本期仅 HTTP 模拟）；
- 复杂流式计算、窗口聚合、规则编排后台；
- 告警确认、关闭与工单联动处置（由 Task 6 完成）；
- 前端大屏实现细节（本期仅提供后端接口与推送契约）。

---

## 3. 方案比较与选择

### 方案 A：轻量闭环

- 路径：HTTP 上报 -> history 插入 -> current UPSERT -> STOMP 广播。
- 优点：实现成本低、联调快，满足一期实时演示目标。
- 缺点：告警仅做状态标记，不形成正式事件流。

### 方案 B：监测 + 告警联动（推荐）

- 在方案 A 基础上直接写入 `ops_alert_event`。
- 优点：更接近业务闭环。
- 缺点：提前耦合 Task 6，边界变重，测试复杂度显著增加。

### 方案 C：纯数据层

- 只做接收和落库，不推送 WebSocket。
- 优点：实现最简单。
- 缺点：无法支撑“实时监测”核心体验，不满足任务目标。

**结论：采用方案 B。**

---

## 4. 总体架构设计

### 4.1 分层职责

- **app（Controller）**
  - 数据接收、查询接口；
  - 参数校验、统一响应封装。
- **domain（Service）**
  - 上报处理编排（历史写入 + 当前值刷新 + 阈值判断 + 告警事件生成）；
  - 查询聚合（当前值、历史曲线）。
- **infra（Mapper + WebSocket）**
  - MyBatis-Plus 持久化；
  - 告警事件 Mapper 写入；
  - STOMP 消息广播。

### 4.2 数据处理链路

1. 校验上报 `pointId`、`metricType`、`metricValue`；
2. 读取测点配置（阈值、单位、状态）；
3. 写 `ts_measure_history`（append-only）；
4. 对 `ts_measure_current` 执行 UPSERT（同 `point_id` 覆盖）；
5. 计算 `alarmFlag`（超阈值为 1，否则 0）；
6. `alarmFlag=1` 时生成 `ops_alert_event`（最小字段集）；
7. 推送 STOMP 消息给订阅端（携带事件摘要）。

### 4.3 WebSocket 广播约定

- Broker 前缀：`/topic`；
- 广播主题：
  - `/topic/monitor/points/{pointId}`（单点位）
  - `/topic/monitor/stream`（全量流）
- 消息结构：
  - `pointId`、`metricType`、`currentValue`、`collectTime`、`qualityFlag`、`alarmFlag`、`traceId`、`alertCode`（可空）。

---

## 5. 组件与接口设计

### 5.1 MonitorController

- `POST /api/monitor/ingest`
  - 入参：`pointId`、`metricType`、`metricValue`、`collectTime`、`qualityFlag`、`edgeNodeCode`、`traceId`
  - 出参：`accepted`、`alarmFlag`、`currentValue`
- `GET /api/monitor/current`
  - 参数：`pointId`（可选）、`deviceId`（可选）、`pageNum`、`pageSize`
  - 返回当前值分页列表。
- `GET /api/monitor/history`
  - 参数：`pointId`（必填）、`startTime`、`endTime`、`limit`
  - 返回按时间升序的历史点列。

### 5.2 领域服务

- `MonitorIngestService`
  - `ingest(MeasureIngestCommand)`：执行完整写链路并返回结果。
- `MonitorQueryService`
  - `queryCurrent(CurrentQuery)`：当前值查询；
  - `queryHistory(HistoryQuery)`：历史曲线查询。
- `MonitorThresholdEvaluator`
  - 根据 `thresholdMin/thresholdMax` 计算 `alarmFlag`。
- `MonitorAlertEventService`
  - `emitIfNeeded(AlertEmitCommand)`：在异常时写入 `ops_alert_event`。

### 5.3 关键规则

- 点位不存在或停用时拒绝上报并返回业务错误；
- `metricType` 与测点配置不一致时拒绝上报；
- `collectTime` 允许客户端传入，未传则使用服务端当前时间；
- `ts_measure_current` 以 `point_id` 为唯一键执行“存在更新、不存在插入”；
- `alarmFlag=1` 时写 `ops_alert_event`，最小落库字段：`alert_code`、`target_type`、`target_id`、`point_id`、`alert_level`、`alert_status`、`occur_time`；
- 同点位短时间重复异常可按最小去重窗口合并（默认 1 分钟）；
- `history` 默认返回最近 `N` 条（建议默认 500，上限 5000）。

---

## 6. 数据模型复用说明

复用表：
- `iot_measure_point`（阈值、点位状态、指标类型）；
- `ts_measure_history`（历史序列）；
- `ts_measure_current`（实时快照）。
- `ops_alert_event`（异常联动事件）。

关键字段映射：
- `metricValue -> ts_measure_history.metric_value`；
- `metricValue -> ts_measure_current.current_value`；
- 阈值判断结果 -> `alarm_flag`；
- 异常联动 -> `ops_alert_event.alert_status=1(待处理)`；
- 原始负载摘要 -> `raw_payload`（JSON 字符串）。

---

## 7. 错误处理与安全策略

- 统一复用 `ApiResponse` + `GlobalExceptionHandler`；
- 业务错误建议：
  - 点位不存在/停用：`BIZ_4001`；
  - 指标不匹配：`BIZ_4001`；
  - 时间区间不合法：`REQ_4000`。
- 安全策略：
  - Monitor 接口默认鉴权；
  - 网关模拟接口可通过系统角色控制（管理员/运维可写，其他只读）。

---

## 8. 测试设计

### 8.1 合同测试（app）

- `MonitorIngestApiContractTest`
  - 合法上报返回成功；
  - 非法点位返回业务错误；
  - 上报后可在 current 接口查询到最新值；
  - 异常值上报后存在告警事件记录。
- `MonitorQueryApiContractTest`
  - 历史曲线按时间排序；
  - 时间区间与 limit 生效。
- `MonitorWebSocketContractTest`
  - 上报后能收到 STOMP 推送；
  - 异常值推送 `alarmFlag=1` 且带 `alertCode`。

### 8.2 领域与基础设施测试

- `MonitorIngestServiceTest`
  - history 追加写入；
  - current UPSERT 生效；
  - 阈值判断正确；
  - 异常时告警事件写入正确。
- `MonitorQueryServiceTest`
  - 当前值分页与历史区间过滤正确。

---

## 9. 验收标准（DoD）

1. 上报接口可用，能写入 history 并刷新 current；
2. 相同 `point_id` 连续上报时，current 保持最新值；
3. current/history 查询接口可支撑实时卡片与历史曲线；
4. 异常上报可写入 `ops_alert_event`，并输出最小事件摘要；
5. STOMP 能广播实时更新，异常值携带 `alarmFlag=1` 与 `alertCode`；
6. 新增测试通过，且不破坏既有测试。

---

## 10. 风险与后续衔接

- 风险：HTTP 模拟上报与真实网关时序特性可能存在偏差；
- 缓解：通过 `MeasureIngestCommand` 保持协议字段稳定，后续适配器扩展；
- 与后续任务衔接：
  - Task 6 直接复用 Task 5 生成的 `ops_alert_event` 做确认/关闭/工单流转；
  - 前端趋势图与实时卡片可直接消费 current/history + STOMP 契约。

---

## 11. 默认决策说明

根据用户评审反馈，本轮改为方案 B：在实时监测链路中前置落地“异常即事件”的最小告警联动，保证 Task 5 产出可直接驱动 Task 6 的处置闭环。
