# Task 4 Spec：Asset 与 Device 模块开发（资产底座）

## 1. 背景与目标

当前工程已完成：
- Task 1：多模块骨架、统一响应、统一异常、Security/JWT/Redis 基础设施；
- Task 2：核心物理表、Entity、Mapper（含 `asset_network`、`asset_pipe_section`、`iot_device`、`iot_measure_point`）；
- Task 3：Auth/System、用户角色管理与最小数据权限基座（管理员全量、非管理员按区域）。

本 spec 聚焦 Task 4，目标是交付一期可演示、可扩展的资产与设备档案能力：

1. 建立统一业务编码服务，支持“区域-类别-层级-流水号”生成规则；
2. 提供资产与设备核心档案 CRUD（分页查询、新增、修改、删除）；
3. 建立设备与测点关联校验，保证档案关系一致；
4. 在资产/设备分页查询中复用 Task 3 数据权限基座。

---

## 2. 范围界定

### 2.1 In Scope

- 统一编码服务（一期最小可用）：
  - 资产管网编码（`network_code`）
  - 资产管段编码（`section_code`）
  - 设备编码（`device_code`）
  - 测点编码（`point_code`）
- CRUD API：
  - `asset_network`（管网）
  - `asset_pipe_section`（管段）
  - `iot_device`（设备）
  - `iot_measure_point`（测点）
- 关键关联校验：
  - `asset_pipe_section.network_id` 必须存在；
  - `iot_measure_point.device_id` 必须存在；
  - `iot_device.facility_id`、`iot_measure_point.section_id`、`iot_measure_point.node_id` 为可选，传入时做存在性校验。
- 数据权限：分页查询复用 Task 3 规则（管理员全量，非管理员按区域）。
- 对应领域单测与 app 合同测试。

### 2.2 Out of Scope

- GIS 空间能力（空间索引、地图绘制、空间分析）；
- 节点/附属设施管理台完整 CRUD（本期仅作为关联校验对象）；
- 资产导入导出、批量变更、审批流；
- 设备在线监测实时写入（属于 Task 5 Monitor）；
- 复杂编码规则编排后台（本期先固定规则）。

---

## 3. 方案比较与选择

### 方案 A：各模块各自生成编码（不推荐）

- 特点：`NetworkService`/`DeviceService` 等各自维护编码逻辑。
- 优点：上手快，改动局部。
- 缺点：规则分散、重复实现，后续维护成本高，跨模块编码一致性弱。

### 方案 B：统一编码服务 + 模块服务复用（推荐）

- 特点：抽象 `BizCodeService`，按类别生成统一编码；资产与设备服务只负责业务校验与持久化。
- 优点：规则单点维护、可复用、可测试；便于后续扩展到告警/工单等域。
- 缺点：需要增加一层抽象与并发唯一性策略。

### 方案 C：预分配号段 + 批量缓存（暂不采用）

- 特点：预分配号段，减少实时生成开销。
- 优点：高并发性能更好。
- 缺点：一期复杂度过高，可能引入号段浪费与恢复问题。

**结论：采用方案 B。**

---

## 4. 总体架构设计

### 4.1 分层职责

- **app（Controller）**  
  提供 Asset/Device 接口，完成 DTO 校验与统一响应。
- **domain（Service）**  
  承担编码生成、唯一性校验、关联校验、数据权限过滤规则编排。
- **infra（Mapper）**  
  复用 Task 2 已生成 Mapper，负责数据库访问。
- **common / security**  
  复用 `ApiResponse`、全局异常、JWT 鉴权与 `DataPermissionEvaluator`。

### 4.2 统一业务编码规则

- 编码格式：`{regionCode}-{categoryCode}-{levelCode}-{seq6}`
- 类别编码建议：
  - `NET`：管网
  - `SEC`：管段
  - `DEV`：设备
  - `MPT`：测点
- 层级编码：
  - 资产按业务层级（如 `L1/L2`）；
  - 设备与测点默认 `L1`。
- 示例：
  - `3301-NET-L1-000001`
  - `3301-DEV-L1-000128`
- 一期并发唯一策略：使用 Redis 原子递增作为主策略；测试环境可使用内存实现替身。

### 4.3 数据权限与查询行为

- 管理员角色：可查看全部区域数据；
- 非管理员：仅可查看本区域数据（按 `region_code`）；
- 若非管理员未配置区域：拒绝分页查询并返回权限错误（`AUTH_4030`），避免跨区读取。

---

## 5. 组件与接口设计

### 5.1 Asset 模块

#### AssetNetworkController
- `GET /api/asset/networks`：分页查询（支持 `networkName`、`networkType`、`regionCode` 过滤）。
- `POST /api/asset/networks`：新增管网（可自动生成 `networkCode`）。
- `PUT /api/asset/networks/{id}`：更新管网。
- `DELETE /api/asset/networks/{id}`：逻辑删除。

#### AssetPipeSectionController
- `GET /api/asset/pipe-sections`：分页查询（支持 `sectionName`、`networkId`、`pipeMaterial` 过滤）。
- `POST /api/asset/pipe-sections`：新增管段（自动生成 `sectionCode`）。
- `PUT /api/asset/pipe-sections/{id}`：更新管段。
- `DELETE /api/asset/pipe-sections/{id}`：逻辑删除。

### 5.2 Device 模块

#### IotDeviceController
- `GET /api/device/devices`：分页查询（支持 `deviceName`、`deviceType`、`onlineStatus` 过滤）。
- `POST /api/device/devices`：新增设备（自动生成 `deviceCode`）。
- `PUT /api/device/devices/{id}`：更新设备。
- `DELETE /api/device/devices/{id}`：逻辑删除。

#### IotMeasurePointController
- `GET /api/device/measure-points`：分页查询（支持 `deviceId`、`metricType`、`pointStatus` 过滤）。
- `POST /api/device/measure-points`：新增测点（自动生成 `pointCode`）。
- `PUT /api/device/measure-points/{id}`：更新测点。
- `DELETE /api/device/measure-points/{id}`：逻辑删除。

### 5.3 领域服务

- `BizCodeService`
  - `generate(BizCategory category, String regionCode, String levelCode)` -> `String`
- `AssetNetworkService` / `AssetPipeSectionService`
- `IotDeviceService` / `IotMeasurePointService`

核心规则：
- 业务编码唯一冲突返回业务异常；
- 管段新增/更新必须校验 `network_id` 存在；
- 测点新增/更新必须校验 `device_id` 存在；
- 逻辑删除默认 `is_deleted = 1`，分页只查未删除数据。

---

## 6. 数据模型复用说明

本任务直接复用 Task 2 已落地表：
- `asset_network`
- `asset_pipe_section`
- `iot_device`
- `iot_measure_point`
- （只读校验）`asset_node`、`asset_facility`

关键约束：
- `network_code`、`section_code`、`device_code`、`point_code` 均唯一；
- `is_deleted` 为逻辑删除标识；
- 区域字段统一使用 `region_code` 参与数据权限过滤。

---

## 7. 错误处理与安全策略

- 继续复用 `ApiResponse` + `GlobalExceptionHandler`；
- 推荐补充/复用业务错误语义：
  - 编码冲突：`BIZ_4001`（消息明确到具体编码字段）；
  - 关联对象不存在：`BIZ_4001`（消息明确到具体外键）；
  - 越权读取：`AUTH_4030`。
- 所有接口默认 JWT 鉴权；仅健康检查等公共接口放行。

---

## 8. 测试设计

### 8.1 合同测试（app）

- `AssetApiContractTest`
  - 管网/管段分页、创建、更新、删除链路可用；
  - 自动编码返回且符合规则；
  - 非管理员分页只看到本区域数据。
- `DeviceApiContractTest`
  - 设备/测点 CRUD 可用；
  - 测点关联不存在设备时报业务错误。

### 8.2 领域与基础设施测试

- `BizCodeServiceTest`
  - 编码格式正确；
  - 并发下编码唯一。
- `AssetDeviceServiceTest`
  - 外键关联校验、唯一冲突、逻辑删除行为。
- 复用 Mapper/Schema 合同测试，确保字段映射与 SQL 一致。

---

## 9. 验收标准（DoD）

1. 四类档案（管网/管段/设备/测点）CRUD 与分页接口可用；
2. 四类业务编码可自动生成，格式正确且唯一；
3. 关键关联校验生效（`network_id`、`device_id`）；
4. 分页查询落地区域数据权限规则；
5. 新增测试通过，且不破坏既有测试。

---

## 10. 风险与后续衔接

- 风险：编码服务一期采用固定规则，若后续规则变化可能涉及兼容；
- 缓解：通过 `BizCodeService` 隔离规则实现，避免散落到各模块；
- 与后续任务衔接：
  - Task 5 Monitor 直接复用 `iot_device` / `iot_measure_point` 档案；
  - Task 6 Alert/WorkOrder 可复用资产编码与区域权限上下文。

---

## 11. 默认决策说明

本轮用户离线，按默认“最小可交付”策略制定：优先四类核心档案与统一编码基座，不扩大到 GIS 与批处理管理台。若后续需要纳入 `asset_node`/`asset_facility` 完整管理，可在 Task 4.1 增量扩展，不破坏现有 API 形态。
