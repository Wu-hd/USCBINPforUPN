# Task 3 Spec：Auth 与 System 模块开发（权限与基座）

## 1. 背景与目标

当前工程已完成：
- Task 1：多模块骨架、统一响应、统一异常、Security/JWT/Redis 基础接入；
- Task 2：核心表与 Entity/Mapper（包含 `sys_user`、`sys_role`、`sys_user_role`、`sys_role_menu`）。

本 spec 聚焦 Task 3，目标是交付一期可演示、可扩展的权限基座能力：

1. 提供可用的登录鉴权能力（JWT）。
2. 提供当前用户与菜单能力，支撑前端动态路由。
3. 提供用户与角色管理能力（CRUD + 绑定）。
4. 建立“数据权限”扩展点，并落地最小可用规则。

---

## 2. 范围界定

### 2.1 In Scope

- `/api/auth/login` 登录鉴权。
- `/api/auth/me` 当前登录用户信息。
- `/api/auth/menus` 当前用户菜单。
- `sys_user`、`sys_role` 的分页查询与增删改查。
- 用户与角色多对多绑定（`sys_user_role`）。
- 基础数据权限策略（按角色/区域的最小规则）。
- 对应单元测试与接口合同测试。

### 2.2 Out of Scope

- 复杂菜单树可视化编排后台（仅提供菜单读取，不做完整菜单管理台）。
- 细粒度按钮级权限引擎（仅预留扩展点）。
- SSO/OAuth2、刷新令牌体系、踢人下线等高级认证能力。
- 业务域（资产、监测、告警、工单）内的完整数据权限落地（本任务仅打基座）。

---

## 3. 方案比较与选择

### 方案 A：轻量 RBAC + JWT + 领域服务分层（推荐）

- 特点：沿用现有 `Controller -> Service -> Domain/Manager -> Mapper` 分层，新增 JWT 认证过滤器与 RBAC 领域服务。
- 优点：与当前工程一致、改造风险低、交付快、便于后续任务叠加。
- 缺点：权限模型表达能力有限，需要后续迭代补齐按钮级能力。

### 方案 B：深度 Spring Security 化（UserDetailsService + GrantedAuthority）

- 特点：把权限加载、认证、授权深度绑定到 Security 体系。
- 优点：安全体系标准化更强。
- 缺点：初期样板复杂度高，调试成本高，不利于一期快速闭环。

### 方案 C：会话化鉴权（Redis Session + Token 黑名单）

- 特点：侧重在线会话管理与强制失效控制。
- 优点：运维控制能力强。
- 缺点：超出 Task 3 必需范围，增加系统复杂度。

**结论：采用方案 A。**

---

## 4. 总体架构设计

### 4.1 分层职责

- **app（Controller）**：鉴权接口、系统管理接口、DTO 校验、统一响应返回。
- **domain（Service/Manager）**：认证逻辑、用户角色管理、菜单聚合、数据权限规则判断。
- **infra（Mapper/Security/JWT）**：SQL 访问、JWT 过滤与上下文构建。
- **common**：错误码、异常、统一响应、全局异常。

### 4.2 安全链路

1. `POST /api/auth/login` 放行，用户名密码校验成功后签发 JWT。
2. 其他 `/api/**` 请求由 JWT 过滤器解析 token，构建认证上下文。
3. 认证上下文至少包含：`userId`、`username`、`roleCodes`、`regionCode`、`roleScope`。
4. 无 token 或 token 非法返回统一未授权错误码。

### 4.3 数据权限基座（最小可用）

- 规则优先级：
  1. 管理员角色（如 `ADMIN`） -> 全量可见；
  2. 非管理员 -> 限制为 `region_code = currentUser.regionCode`（若 region 为空则拒绝跨区查询）。
- 本任务先在 `sys_user` 分页查询中落地该规则。
- 抽象 `DataPermissionEvaluator` 接口，后续业务域复用。

---

## 5. 组件与接口设计

### 5.1 Auth 组件

### AuthService
- `login(username, password)`：校验账号状态、密码（`password_hash`），返回 token 与用户摘要。
- `getCurrentUser(userId)`：返回当前用户基础信息与角色。
- `getCurrentMenus(userId)`：基于用户角色聚合菜单（来源 `sys_role_menu.menu_code`）。

### AuthController
- `POST /api/auth/login`
  - 请求：`username`、`password`
  - 响应：`token`、`tokenType`、`expiresIn`、`userInfo`
- `GET /api/auth/me`
  - 响应：当前用户信息（含角色、区域、数据范围）
- `GET /api/auth/menus`
  - 响应：菜单编码列表（一期先返回平铺结构）

### 5.2 System 组件

### SysUserController
- `GET /api/system/users`（分页 + 条件查询）
- `POST /api/system/users`
- `PUT /api/system/users/{id}`
- `DELETE /api/system/users/{id}`（逻辑删除）
- `PUT /api/system/users/{id}/roles`（覆盖式绑定）

### SysRoleController
- `GET /api/system/roles`（分页/列表）
- `POST /api/system/roles`
- `PUT /api/system/roles/{id}`
- `DELETE /api/system/roles/{id}`（逻辑删除）

### 领域规则
- 用户名、角色编码唯一性冲突需返回业务异常。
- 删除角色前需校验是否仍被用户绑定。
- 角色绑定采用“先查差异，后批量更新”策略，保持幂等。

---

## 6. 数据模型使用说明

直接复用 Task 2 已落地表：

- `sys_user`：账号主体（含 `region_code`、`role_scope`、`account_status`）。
- `sys_role`：角色主体（含 `role_code`、`role_status`、`data_scope`）。
- `sys_user_role`：用户角色关系。
- `sys_role_menu`：角色菜单编码关系。

关键约束：
- `sys_user.username` 唯一；
- `sys_role.role_code` 唯一；
- `sys_user_role(user_id, role_id)` 复合唯一。

---

## 7. 错误处理与安全策略

- 继续复用 `ApiResponse` 与 `GlobalExceptionHandler`。
- 认证/授权建议补充错误码：
  - `AUTH_4010`：未登录或 token 无效；
  - `AUTH_4030`：无权限访问；
  - `AUTH_4001`：用户名或密码错误；
  - `AUTH_4002`：账号禁用。
- 密码不明文存储，统一哈希比对（推荐 BCrypt）。
- 登录接口需预留防暴力破解能力（本期先留扩展点，如失败计数器接口）。

---

## 8. 测试设计

### 8.1 合同测试（app）

- `AuthApiContractTest`
  - 登录成功返回 token；
  - 错误密码返回业务错误码；
  - 未携带 token 访问受保护接口返回未授权码。
- `SystemUserRoleApiContractTest`
  - 用户分页可用；
  - 用户角色绑定可读可写；
  - 唯一约束冲突返回业务异常。

### 8.2 领域与基础设施测试

- `AuthServiceTest`：账号状态、密码校验、token 负载。
- `DataPermissionEvaluatorTest`：管理员全量、非管理员按区域过滤。
- `JwtSecurityFilterTest`：token 解析与认证上下文构建。

---

## 9. 验收标准（DoD）

1. 登录接口可用，成功返回 JWT，失败返回统一错误结构。
2. 非登录接口默认鉴权，未登录访问被拒绝。
3. `/api/auth/me`、`/api/auth/menus` 可返回当前用户信息与菜单编码。
4. `sys_user`、`sys_role` CRUD 与分页接口可用。
5. 用户角色绑定接口可用，关系表数据一致。
6. `sys_user` 分页落地最小数据权限规则（管理员全量，其他按区域）。
7. 新增测试通过，且不破坏既有测试。

---

## 10. 风险与后续衔接

- 风险：一期只做“菜单编码级”权限，无法覆盖按钮级与字段级授权。
- 缓解：通过 `DataPermissionEvaluator` 与角色菜单聚合接口保持扩展点稳定。
- 与后续任务衔接：
  - Task 4/5/6 的业务接口统一复用 JWT 上下文与数据权限接口；
  - 后续可新增菜单管理台与细粒度权限策略，不破坏当前 API 形态。

---

## 11. 本 spec 的默认决策说明

由于本轮未收到额外范围反馈，本 spec 采用默认“最小可交付”范围（登录 + 当前用户/菜单 + 用户角色管理 + 最小数据权限基座）。如需升级到“菜单管理/权限点管理”或“复杂数据权限策略”，可在本 spec 基础上增量扩展。
