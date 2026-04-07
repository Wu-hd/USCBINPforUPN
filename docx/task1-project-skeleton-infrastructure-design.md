# Task 1 Spec：项目骨架与基础设施搭建（后端）

## 1. 目标与范围

### 1.1 目标
- 初始化可运行的 Spring Boot 3.x + JDK 17 后端工程。
- 建立“可维护、可排障”的分层与分模块骨架。
- 落地统一返回结构、全局异常处理、参数校验、MyBatis-Plus 分页与主键策略。
- 接入后续任务需要的基础依赖：Spring Security、JWT、Redis、MapStruct、Lombok。

### 1.2 范围（In Scope）
- Maven 多模块工程骨架与依赖管理。
- 统一 API 响应协议（`code/message/data/timestamp`）。
- 业务异常、校验异常、系统异常的统一处理策略。
- 参数校验基线（`@Valid/@Validated` + DTO 约束）。
- MyBatis-Plus 分页插件与 `ASSIGN_ID` 主键策略。
- 基础健康检查接口（用于启动与协议验证）。

### 1.3 非范围（Out of Scope）
- 登录鉴权业务接口（如 `/api/auth/login`）与菜单权限逻辑。
- 核心业务表完整建表与代码生成（归属后续任务）。
- 告警、工单、设备等业务模块实现。

---

## 2. 总体架构

## 2.1 模块划分（Maven Multi-Module）
- `uscbinp-parent`：父工程，统一版本与依赖管理（BOM/插件版本）。
- `uscbinp-common`：通用层（响应体、错误码、异常定义、全局异常处理、校验公共能力）。
- `uscbinp-domain`：领域层（Entity/DTO/VO、业务接口、领域规则）。
- `uscbinp-infra`：基础设施层（MyBatis-Plus、Mapper、Redis、JWT 工具、安全过滤器、MapStruct）。
- `uscbinp-app`：启动与应用层（Spring Boot 启动类、Controller、应用编排）。

## 2.2 依赖方向约束
- `app -> domain, infra, common`
- `infra -> domain, common`
- `domain -> common`
- 禁止反向依赖（例如 `common -> app`、`domain -> infra`）。

该约束用于中后期问题定位：接口问题优先查 `app`，业务规则查 `domain`，中间件与数据访问查 `infra`，通用协议与异常编码查 `common`。

---

## 3. 技术栈与版本基线

- JDK: 17
- Spring Boot: 3.x
- Spring Web
- Spring Security
- MyBatis-Plus
- JWT（推荐 `jjwt` 体系）
- Spring Data Redis
- MapStruct
- Lombok
- Hibernate Validator（Jakarta Validation）

版本策略：
- 由 `uscbinp-parent` 统一管理版本，子模块不重复声明版本号。
- MapStruct 与 Lombok 的 annotation processor 在父工程统一配置。

---

## 4. 接口统一返回规范

## 4.1 响应结构
所有接口均返回以下 JSON 结构：

```json
{
  "code": "00000",
  "message": "OK",
  "data": {},
  "timestamp": "2026-04-07T03:31:37Z"
}
```

字段定义：
- `code`：业务语义码（字符串，避免跨语言精度/格式歧义）。
- `message`：可读提示信息。
- `data`：业务负载，失败时可为 `null` 或错误详情结构。
- `timestamp`：UTC ISO-8601 时间。

## 4.2 成功/失败约定
- 成功：`code = 00000`。
- 失败：使用业务码（如参数错误、未授权、业务冲突、系统异常等），但结构不变。

---

## 5. 异常与错误处理规范

## 5.1 异常分类
- `BusinessException`：可预期业务异常（如状态冲突、资源不可用）。
- 参数校验异常：
  - `MethodArgumentNotValidException`
  - `ConstraintViolationException`
- 其他 `Exception`：系统兜底异常。

## 5.2 全局异常处理行为
- 统一由 `GlobalExceptionHandler` 转换为标准响应结构。
- 参数校验异常需聚合字段错误信息，返回明确可读消息。
- 系统异常统一返回“系统繁忙/内部错误”类错误码，并输出可关联日志上下文（如 traceId）到日志体系。

---

## 6. 参数校验规范

- Controller 层入参默认启用 `@Valid` / `@Validated`。
- 约束定义位于 DTO 层（如 `@NotBlank`, `@NotNull`, `@Size`, `@Pattern`）。
- 禁止在 Controller 中散落手写判空作为主要校验手段。
- 错误响应统一由全局异常处理器输出，不在 Controller 内自行拼装错误 JSON。

---

## 7. MyBatis-Plus 基础配置规范

## 7.1 分页插件
- 启用 `PaginationInnerInterceptor(DbType.MYSQL)`。

## 7.2 主键策略
- 实体主键统一使用：

```java
@TableId(type = IdType.ASSIGN_ID)
private Long id;
```

- 该策略作为默认规范；后续若个别表需特殊主键策略，需在设计中单独声明并评审。

## 7.3 审计字段扩展点
- 本任务不强制实现自动填充逻辑，但需预留 `MetaObjectHandler` 接入位置，供后续任务统一处理 `created_by/created_time/updated_by/updated_time`。

---

## 8. 安全与基础设施接入边界

- Spring Security 基础依赖接入完成，并提供后续放行登录接口的扩展点。
- JWT 工具能力（签发/解析）先完成基础组件骨架，不绑定具体业务接口。
- Redis 连接与模板能力就绪，供后续 Token、缓存、会话扩展使用。

---

## 9. 目录与命名约定（示例）

```text
uscbinp-parent
├─ uscbinp-common
│  └─ com.xxx.common
│     ├─ api
│     ├─ error
│     ├─ exception
│     └─ web
├─ uscbinp-domain
│  └─ com.xxx.domain
│     ├─ model
│     ├─ dto
│     ├─ vo
│     └─ service
├─ uscbinp-infra
│  └─ com.xxx.infra
│     ├─ mapper
│     ├─ config
│     ├─ security
│     ├─ jwt
│     └─ redis
└─ uscbinp-app
   └─ com.xxx.app
      ├─ controller
      ├─ Application.java
      └─ config
```

---

## 10. 验收标准（Definition of Done）

1. 工程成功启动，模块依赖无环。
2. 提供至少 1 个健康检查接口，返回统一结构（含 `code/message/data/timestamp`）。
3. 以下三类错误均返回统一结构：
   - 参数校验失败
   - 业务异常
   - 系统异常
4. 分页插件生效（可通过示例分页查询验证）。
5. 示例实体主键策略为 `IdType.ASSIGN_ID`。
6. Security/JWT/Redis/MapStruct/Lombok 依赖接入完成，具备后续任务扩展能力。

---

## 11. 风险与约束

- 多模块初期搭建复杂度高于单模块，但可显著降低中后期排障与维护成本。
- 若团队短期追求极快交付，可在不打破依赖约束前提下减少初期模块数量；本 spec 仍以可维护性优先。
- 错误码规范需与后续任务保持一致，否则会出现前后端语义不统一问题。

---

## 12. 后续衔接

- 下一任务可直接基于该骨架推进“核心物理表建立与实体类生成”。
- Auth 与业务模块开发应严格复用本 spec 的统一返回、异常与校验规范。

---

## 13. 交付清单映射

- 交付核对文件：`task1-delivery-checklist.md`
- 清单项与 DoD 一一对应，保持 6 项基线：
  - 多模块工程启动成功
  - 统一响应结构生效
  - 三类异常统一返回
  - 分页插件生效
  - ASSIGN_ID 主键策略生效
  - Security/JWT/Redis/MapStruct 基础接入完成
