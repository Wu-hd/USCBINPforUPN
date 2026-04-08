# 后端服务运行与 API 说明

## 1. 运行环境

- JDK 17
- Maven 3.8+
- 默认端口：`8080`
- 可选环境变量：`USCBINP_JWT_SECRET`（未设置时使用随机值）

## 2. 如何启动后端服务

在仓库根目录执行：

```powershell
Set-Location backend
mvn -q -DskipTests install
Set-Location uscbinp-app
mvn -q spring-boot:run
```

## 3. 启动检查结果

已检查：服务可正常启动，且健康检查接口可访问。

```json
{"code":"00000","message":"OK","data":"UP","timestamp":"2026-04-08T07:51:24.217669400Z"}
```

健康检查地址：

- `GET /api/health`

## 4. 认证与访问规则

- 放行（无需 JWT）：
  - `GET /api/health`
  - `POST /api/auth/login`
  - WebSocket 握手路径：`/ws/**`（当前为 `/ws/monitor`）
- 其余 HTTP API 默认都需要 Bearer Token（`Authorization: Bearer <token>`）

## 5. 通用响应结构

所有 HTTP API 都使用统一结构（`ApiResponse<T>`）：

- `code`：状态码（成功为 `00000`）
- `message`：描述（成功为 `OK`）
- `data`：业务数据
- `timestamp`：响应时间戳

---

## 6. API 清单（全部）

> 说明：`鉴权` 列中，`否` 表示无需登录；`是` 表示需要 JWT。

### 6.1 健康与示例

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| GET | `/api/health` | 否 | 服务健康状态 | 无 |
| POST | `/api/demo/validate` | 是 | 参数校验示例接口 | Body: `name`（必填） |
| GET | `/api/demo/business-error` | 是 | 触发业务异常示例 | 无 |

### 6.2 认证

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/auth/login` | 否 | 用户登录并返回 JWT | Body: `username`、`password`（均必填） |
| GET | `/api/auth/me` | 是 | 获取当前登录用户名 | 无 |
| GET | `/api/auth/menus` | 是 | 获取当前用户菜单权限列表 | 无 |

### 6.3 系统管理（用户/角色）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| GET | `/api/system/users` | 是 | 用户分页查询 | Query: `pageNum`(默认1), `pageSize`(默认10) |
| GET | `/api/system/users/{id}` | 是 | 获取用户详情 | Path: `id` |
| POST | `/api/system/users` | 是 | 新增用户 | Body: `username`(必填), `realName`, `mobile`, `email`, `accountStatus`(必填) |
| PUT | `/api/system/users/{id}` | 是 | 更新用户 | Path: `id`; Body 同新增 |
| DELETE | `/api/system/users/{id}` | 是 | 删除用户 | Path: `id` |
| PUT | `/api/system/users/{id}/roles` | 是 | 绑定用户角色 | Path: `id`; Body: `roleIds`(Long数组，必填) |
| GET | `/api/system/roles` | 是 | 角色分页查询 | Query: `pageNum`(默认1), `pageSize`(默认10) |
| GET | `/api/system/roles/{id}` | 是 | 获取角色详情 | Path: `id` |
| POST | `/api/system/roles` | 是 | 新增角色 | Body: `roleCode`(必填), `roleName`(必填), `roleStatus`(必填) |
| PUT | `/api/system/roles/{id}` | 是 | 更新角色 | Path: `id`; Body 同新增 |
| DELETE | `/api/system/roles/{id}` | 是 | 删除角色 | Path: `id` |

### 6.4 资产网络（Asset Network）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/asset/networks` | 是 | 新增管网 | Body: `networkName`(必填), `networkCode`, `networkType`, `regionCode`, `serviceStatus`, `levelCode` |
| PUT | `/api/asset/networks/{id}` | 是 | 更新管网 | Path: `id`; Body 同新增 |
| GET | `/api/asset/networks/{id}` | 是 | 管网详情 | Path: `id` |
| DELETE | `/api/asset/networks/{id}` | 是 | 删除管网 | Path: `id` |
| GET | `/api/asset/networks` | 是 | 管网分页查询 | Query: `pageNum`(默认1), `pageSize`(默认10), `regionCode` |

### 6.5 资产管段（Asset Pipe Section）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/asset/pipe-sections` | 是 | 新增管段 | Body: `networkId`(必填), `sectionCode`, `sectionName`, `pipeMaterial`, `diameterMm`, `buryDepthM`, `regionCode`, `renovationStatus`, `levelCode` |
| PUT | `/api/asset/pipe-sections/{id}` | 是 | 更新管段 | Path: `id`; Body 同新增 |
| GET | `/api/asset/pipe-sections/{id}` | 是 | 管段详情 | Path: `id` |
| DELETE | `/api/asset/pipe-sections/{id}` | 是 | 删除管段 | Path: `id` |
| GET | `/api/asset/pipe-sections` | 是 | 管段分页查询 | Query: `pageNum`(默认1), `pageSize`(默认10), `networkId`, `regionCode` |

### 6.6 设备管理（IoT Device）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/device/devices` | 是 | 新增设备 | Body: `deviceName`(必填), `deviceCode`, `deviceType`, `protocolType`, `gatewayCode`, `facilityId`, `regionCode`, `onlineStatus`, `lastOnlineTime`, `firmwareVersion`, `levelCode` |
| PUT | `/api/device/devices/{id}` | 是 | 更新设备 | Path: `id`; Body 同新增 |
| GET | `/api/device/devices/{id}` | 是 | 设备详情 | Path: `id` |
| DELETE | `/api/device/devices/{id}` | 是 | 删除设备 | Path: `id` |
| GET | `/api/device/devices` | 是 | 设备分页查询 | Query: `pageNum`(默认1), `pageSize`(默认10), `regionCode` |

### 6.7 测点管理（IoT Measure Point）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/device/measure-points` | 是 | 新增测点 | Body: `pointName`(必填), `deviceId`(必填), `metricType`(必填), `pointCode`, `unitName`, `sampleCycleSec`, `sectionId`, `pointStatus`, `regionCode`, `levelCode` |
| PUT | `/api/device/measure-points/{id}` | 是 | 更新测点 | Path: `id`; Body 同新增 |
| GET | `/api/device/measure-points/{id}` | 是 | 测点详情 | Path: `id` |
| DELETE | `/api/device/measure-points/{id}` | 是 | 删除测点 | Path: `id` |
| GET | `/api/device/measure-points` | 是 | 测点分页查询 | Query: `pageNum`(默认1), `pageSize`(默认10), `deviceId`, `regionCode` |

### 6.8 监测（Monitor）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/monitor/ingest` | 是 | 上报监测值并触发广播/告警判定 | Body: `pointId`(必填), `metricType`(必填), `metricValue`(必填), `collectTime`, `qualityFlag`, `edgeNodeCode`, `traceId` |
| GET | `/api/monitor/current` | 是 | 查询当前值分页 | Query: `pointId`, `deviceId`, `pageNum`(默认1), `pageSize`(默认20) |
| GET | `/api/monitor/history` | 是 | 查询历史值 | Query: `pointId`(必填), `startTime`, `endTime`, `limit` |

### 6.9 告警（Alert）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| GET | `/api/alerts/events` | 是 | 告警事件分页查询 | Query: `alertStatus`, `alertLevel`, `regionCode`, `startTime`, `endTime`, `pageNum`(默认1), `pageSize`(默认20) |
| PUT | `/api/alerts/events/{id}/confirm` | 是 | 确认告警 | Path: `id` |
| PUT | `/api/alerts/events/{id}/close` | 是 | 关闭告警 | Path: `id` |

### 6.10 工单（WorkOrder）

| 方法 | 路径 | 鉴权 | 描述 | 请求参数/Body |
|---|---|---:|---|---|
| POST | `/api/workorders` | 是 | 创建工单 | Body: `sourceType`(必填), `sourceId`(必填), `title`(必填), `targetType`, `targetId`, `regionCode`, `assigneeUserId`, `expectFinishTime` |
| GET | `/api/workorders` | 是 | 工单分页查询 | Query: `pageNum`(默认1), `pageSize`(默认20), `workStatus`, `sourceType`, `sourceId` |
| PUT | `/api/workorders/{id}/assign` | 是 | 分派工单 | Path: `id`; Body: `assigneeUserId`(必填) |
| PUT | `/api/workorders/{id}/start` | 是 | 开始处理工单 | Path: `id` |
| PUT | `/api/workorders/{id}/finish` | 是 | 完成工单 | Path: `id`; Body(可选): `resultSummary` |

---

## 7. WebSocket 接口（实时推送）

- 握手端点：`/ws/monitor`（无需 JWT）
- 订阅主题：
  - `/topic/monitor/stream`：监测实时总线
  - `/topic/monitor/points/{pointId}`：按测点订阅
- 应用前缀：`/app`（STOMP 应用目标前缀）

