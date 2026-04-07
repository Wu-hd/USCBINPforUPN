# Task 2 Spec: 核心物理表建立与实体类生成（一期稳态方案）

## 1. 背景与目标

本 spec 面向一期后端任务 2，基于既有任务 1 工程基线（Spring Boot 3.x + JDK 17 + MyBatis-Plus + ASSIGN_ID）落地数据库核心模型与代码生成规范。

目标：

1. 形成可直接执行的 MySQL 8.0 DDL 草案。
2. 覆盖任务 2 的 14 张核心表，并补充关联表（`sys_user_role`、`sys_role_menu`）。
3. 形成 Entity/Mapper 统一生成规范，保证后续模块按同一骨架扩展。

## 2. 范围界定

### 2.1 In Scope

- 建表脚本（16 张表）
- 索引、唯一约束、主键策略
- 统一审计字段与软删除字段
- Entity/Mapper 生成规范与命名约定
- 可执行验收标准（DoD）

### 2.2 Out of Scope

- Service/Controller/API 开发
- 告警规则引擎实现
- GIS 专题业务表（如 feature 几何扩展）
- 前端联调

## 3. 设计方案选择

采用 **A. 一期稳态方案**：

- 一次性定稿核心业务字段与审计字段；
- 使用“逻辑外键”（索引保证查询与关联性能，默认不强制 FK）；
- 优先保障一期稳定交付与可扩展性，避免高频写入受 FK 约束影响。

## 4. 数据域与主链路

### 4.1 数据域分组

- 权限域：`sys_user`、`sys_role`、`sys_user_role`、`sys_role_menu`
- 资产域：`asset_network`、`asset_pipe_section`、`asset_node`、`asset_facility`
- 设备测点域：`iot_device`、`iot_measure_point`
- 时序域：`ts_measure_current`、`ts_measure_history`
- 告警工单域：`ops_alert_rule`、`ops_alert_event`、`ops_work_order`、`ops_work_order_log`

### 4.2 主链关系

`asset_network -> asset_pipe_section -> (asset_node / asset_facility / iot_measure_point) -> ts_measure_current & ts_measure_history -> ops_alert_event -> ops_work_order -> ops_work_order_log`

## 5. 统一建模规范

### 5.1 主键与命名

- 主键统一 `id BIGINT`
- 代码层统一 `@TableId(type = IdType.ASSIGN_ID)`
- 表名、字段名均为 `snake_case`

### 5.2 审计与软删除字段（全表统一）

- `created_by BIGINT NULL`
- `created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `updated_by BIGINT NULL`
- `updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- `is_deleted TINYINT NOT NULL DEFAULT 0`

### 5.3 索引原则

- 业务编码字段 `*_code` 建唯一索引
- 高频筛选字段（`region_code`、`*_status`、`occur_time`、`collect_time`）建普通/组合索引
- 时序表按查询路径建立组合索引，避免全表扫描

## 6. MySQL 8.0 DDL 草案

```sql
-- =========================
-- 权限域
-- =========================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    mobile VARCHAR(32) NULL,
    email VARCHAR(128) NULL,
    org_name VARCHAR(128) NULL,
    role_scope VARCHAR(128) NULL,
    region_code VARCHAR(32) NULL,
    account_status TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_mobile (mobile),
    KEY idx_sys_user_org_name (org_name),
    KEY idx_sys_user_region_code (region_code),
    KEY idx_sys_user_account_status (account_status),
    KEY idx_sys_user_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    data_scope VARCHAR(64) NULL,
    subsystem_scope VARCHAR(255) NULL,
    menu_scope VARCHAR(1000) NULL,
    sort_no INT NULL,
    role_status TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_role_code (role_code),
    KEY idx_sys_role_role_status (role_status),
    KEY idx_sys_role_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id),
    KEY idx_sys_user_role_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_code VARCHAR(64) NOT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_role_menu (role_id, menu_code),
    KEY idx_sys_role_menu_menu_code (menu_code),
    KEY idx_sys_role_menu_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 资产域
-- =========================
CREATE TABLE IF NOT EXISTS asset_network (
    id BIGINT NOT NULL,
    network_code VARCHAR(64) NOT NULL,
    network_name VARCHAR(128) NOT NULL,
    network_type VARCHAR(32) NULL,
    region_code VARCHAR(32) NULL,
    owner_unit VARCHAR(128) NULL,
    operation_unit VARCHAR(128) NULL,
    service_status TINYINT NOT NULL DEFAULT 1,
    health_level TINYINT NULL,
    risk_level TINYINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_network_code (network_code),
    KEY idx_asset_network_type (network_type),
    KEY idx_asset_network_region (region_code),
    KEY idx_asset_network_status (service_status),
    KEY idx_asset_network_health (health_level),
    KEY idx_asset_network_risk (risk_level),
    KEY idx_asset_network_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS asset_pipe_section (
    id BIGINT NOT NULL,
    section_code VARCHAR(64) NOT NULL,
    network_id BIGINT NOT NULL,
    section_name VARCHAR(128) NULL,
    pipe_material VARCHAR(64) NULL,
    diameter_mm DECIMAL(10,2) NULL,
    bury_depth_m DECIMAL(10,2) NULL,
    pipe_age_year INT NULL,
    old_flag TINYINT NULL,
    renovation_status TINYINT NULL,
    start_node_id BIGINT NULL,
    end_node_id BIGINT NULL,
    latest_health_score DECIMAL(8,2) NULL,
    latest_risk_level TINYINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_pipe_section_code (section_code),
    KEY idx_asset_pipe_section_network_id (network_id),
    KEY idx_asset_pipe_section_name (section_name),
    KEY idx_asset_pipe_section_material (pipe_material),
    KEY idx_asset_pipe_section_diameter (diameter_mm),
    KEY idx_asset_pipe_section_age (pipe_age_year),
    KEY idx_asset_pipe_section_renovation (renovation_status),
    KEY idx_asset_pipe_section_health (latest_health_score),
    KEY idx_asset_pipe_section_risk (latest_risk_level),
    KEY idx_asset_pipe_section_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS asset_node (
    id BIGINT NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    network_id BIGINT NULL,
    node_type VARCHAR(32) NULL,
    node_name VARCHAR(128) NULL,
    longitude DECIMAL(12,7) NULL,
    latitude DECIMAL(12,7) NULL,
    elevation_m DECIMAL(10,2) NULL,
    access_status TINYINT NULL,
    online_flag TINYINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_node_code (node_code),
    KEY idx_asset_node_network_id (network_id),
    KEY idx_asset_node_type (node_type),
    KEY idx_asset_node_name (node_name),
    KEY idx_asset_node_longitude (longitude),
    KEY idx_asset_node_latitude (latitude),
    KEY idx_asset_node_online_flag (online_flag),
    KEY idx_asset_node_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS asset_facility (
    id BIGINT NOT NULL,
    facility_code VARCHAR(64) NOT NULL,
    section_id BIGINT NULL,
    node_id BIGINT NULL,
    facility_type VARCHAR(32) NULL,
    facility_name VARCHAR(128) NULL,
    manufacturer VARCHAR(128) NULL,
    install_date DATE NULL,
    maintain_cycle_day INT NULL,
    facility_status TINYINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_facility_code (facility_code),
    KEY idx_asset_facility_section_id (section_id),
    KEY idx_asset_facility_node_id (node_id),
    KEY idx_asset_facility_type (facility_type),
    KEY idx_asset_facility_name (facility_name),
    KEY idx_asset_facility_status (facility_status),
    KEY idx_asset_facility_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 设备测点域
-- =========================
CREATE TABLE IF NOT EXISTS iot_device (
    id BIGINT NOT NULL,
    device_code VARCHAR(64) NOT NULL,
    device_name VARCHAR(128) NULL,
    device_type VARCHAR(32) NULL,
    protocol_type VARCHAR(32) NULL,
    gateway_code VARCHAR(64) NULL,
    facility_id BIGINT NULL,
    region_code VARCHAR(32) NULL,
    online_status TINYINT NULL,
    last_online_time DATETIME NULL,
    firmware_version VARCHAR(64) NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_iot_device_code (device_code),
    KEY idx_iot_device_name (device_name),
    KEY idx_iot_device_type (device_type),
    KEY idx_iot_device_gateway_code (gateway_code),
    KEY idx_iot_device_facility_id (facility_id),
    KEY idx_iot_device_region_code (region_code),
    KEY idx_iot_device_online_status (online_status),
    KEY idx_iot_device_last_online_time (last_online_time),
    KEY idx_iot_device_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS iot_measure_point (
    id BIGINT NOT NULL,
    point_code VARCHAR(64) NOT NULL,
    point_name VARCHAR(128) NULL,
    device_id BIGINT NOT NULL,
    section_id BIGINT NULL,
    node_id BIGINT NULL,
    metric_type VARCHAR(32) NOT NULL,
    unit_name VARCHAR(16) NULL,
    sample_cycle_sec INT NULL,
    threshold_min DECIMAL(16,4) NULL,
    threshold_max DECIMAL(16,4) NULL,
    point_status TINYINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_iot_measure_point_code (point_code),
    KEY idx_iot_measure_point_name (point_name),
    KEY idx_iot_measure_point_device_id (device_id),
    KEY idx_iot_measure_point_section_id (section_id),
    KEY idx_iot_measure_point_node_id (node_id),
    KEY idx_iot_measure_point_metric_type (metric_type),
    KEY idx_iot_measure_point_status (point_status),
    KEY idx_iot_measure_point_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 时序域
-- =========================
CREATE TABLE IF NOT EXISTS ts_measure_current (
    id BIGINT NOT NULL,
    point_id BIGINT NOT NULL,
    metric_type VARCHAR(32) NULL,
    current_value DECIMAL(18,6) NOT NULL,
    quality_flag TINYINT NULL,
    alarm_flag TINYINT NULL,
    collect_time DATETIME NOT NULL,
    receive_time DATETIME NULL,
    edge_node_code VARCHAR(64) NULL,
    raw_payload JSON NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ts_measure_current_point_id (point_id),
    KEY idx_ts_measure_current_metric_type (metric_type),
    KEY idx_ts_measure_current_quality_flag (quality_flag),
    KEY idx_ts_measure_current_alarm_flag (alarm_flag),
    KEY idx_ts_measure_current_collect_time (collect_time),
    KEY idx_ts_measure_current_receive_time (receive_time),
    KEY idx_ts_measure_current_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ts_measure_history (
    id BIGINT NOT NULL,
    point_id BIGINT NOT NULL,
    section_id BIGINT NULL,
    metric_type VARCHAR(32) NULL,
    metric_value DECIMAL(18,6) NOT NULL,
    quality_flag TINYINT NULL,
    collect_time TIMESTAMP NOT NULL,
    receive_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    calc_tag VARCHAR(32) NULL,
    trace_id VARCHAR(64) NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ts_measure_history_point_collect (point_id, collect_time DESC),
    KEY idx_ts_measure_history_section_collect (section_id, collect_time DESC),
    KEY idx_ts_measure_history_metric_type (metric_type),
    KEY idx_ts_measure_history_quality_flag (quality_flag),
    KEY idx_ts_measure_history_trace_id (trace_id),
    KEY idx_ts_measure_history_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 告警工单域
-- =========================
CREATE TABLE IF NOT EXISTS ops_alert_rule (
    id BIGINT NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    metric_type VARCHAR(32) NULL,
    target_scope VARCHAR(255) NULL,
    rule_expr VARCHAR(1000) NOT NULL,
    alert_level TINYINT NULL,
    merge_window_min INT NULL,
    notify_channel VARCHAR(255) NULL,
    rule_status TINYINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ops_alert_rule_code (rule_code),
    KEY idx_ops_alert_rule_metric_type (metric_type),
    KEY idx_ops_alert_rule_alert_level (alert_level),
    KEY idx_ops_alert_rule_rule_status (rule_status),
    KEY idx_ops_alert_rule_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ops_alert_event (
    id BIGINT NOT NULL,
    alert_code VARCHAR(64) NOT NULL,
    rule_id BIGINT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    point_id BIGINT NULL,
    alert_title VARCHAR(255) NOT NULL,
    alert_level TINYINT NULL,
    alert_status TINYINT NULL,
    occur_time DATETIME NOT NULL,
    confirm_time DATETIME NULL,
    close_time DATETIME NULL,
    work_order_id BIGINT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ops_alert_event_code (alert_code),
    KEY idx_ops_alert_event_rule_id (rule_id),
    KEY idx_ops_alert_event_target (target_type, target_id),
    KEY idx_ops_alert_event_point_id (point_id),
    KEY idx_ops_alert_event_level (alert_level),
    KEY idx_ops_alert_event_status (alert_status),
    KEY idx_ops_alert_event_occur_time (occur_time),
    KEY idx_ops_alert_event_work_order_id (work_order_id),
    KEY idx_ops_alert_event_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ops_work_order (
    id BIGINT NOT NULL,
    work_order_code VARCHAR(64) NOT NULL,
    source_type VARCHAR(32) NULL,
    source_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    target_type VARCHAR(32) NULL,
    target_id BIGINT NULL,
    region_code VARCHAR(32) NULL,
    assignee_user_id BIGINT NULL,
    work_status TINYINT NULL,
    expect_finish_time DATETIME NULL,
    actual_finish_time DATETIME NULL,
    result_summary VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ops_work_order_code (work_order_code),
    KEY idx_ops_work_order_source (source_type, source_id),
    KEY idx_ops_work_order_target (target_type, target_id),
    KEY idx_ops_work_order_region_code (region_code),
    KEY idx_ops_work_order_assignee (assignee_user_id),
    KEY idx_ops_work_order_status (work_status),
    KEY idx_ops_work_order_expect_finish_time (expect_finish_time),
    KEY idx_ops_work_order_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ops_work_order_log (
    id BIGINT NOT NULL,
    work_order_id BIGINT NOT NULL,
    action_type VARCHAR(32) NULL,
    operator_user_id BIGINT NULL,
    action_time DATETIME NOT NULL,
    before_status TINYINT NULL,
    after_status TINYINT NULL,
    action_desc VARCHAR(500) NULL,
    attachment_group_id BIGINT NULL,
    location_text VARCHAR(255) NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ops_work_order_log_work_order (work_order_id),
    KEY idx_ops_work_order_log_action_type (action_type),
    KEY idx_ops_work_order_log_operator (operator_user_id),
    KEY idx_ops_work_order_log_action_time (action_time),
    KEY idx_ops_work_order_log_after_status (after_status),
    KEY idx_ops_work_order_log_work_order_action_time (work_order_id, action_time DESC),
    KEY idx_ops_work_order_log_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

> 注：本期采用逻辑外键策略。若后续需要启用物理 FK，可在迁移脚本中分域按需增加 `ALTER TABLE ... ADD CONSTRAINT`。

## 7. Entity/Mapper 生成规范

### 7.1 包路径

- Entity：`uscbinp-domain/src/main/java/com/uscbinp/domain/model/entity`
- Mapper：`uscbinp-infra/src/main/java/com/uscbinp/infra/mapper`
- Mapper XML（如需要）：`uscbinp-infra/src/main/resources/mapper`

### 7.2 基类规范

创建 `BaseAuditEntity`（domain 模块）：

- `createdBy`
- `createdTime`
- `updatedBy`
- `updatedTime`
- `isDeleted`（标注 `@TableLogic`）

所有业务实体继承该基类，仅保留业务字段。

### 7.3 命名与注解

- 表 `asset_pipe_section` -> `AssetPipeSectionEntity`
- Mapper -> `AssetPipeSectionMapper extends BaseMapper<AssetPipeSectionEntity>`
- 必备注解：
  - `@TableName("asset_pipe_section")`
  - `@TableId(type = IdType.ASSIGN_ID)`

### 7.4 生成边界

- 本任务仅生成 Entity + Mapper
- 不生成 Service/Controller，避免跨越任务 2 边界

## 8. 验收标准（DoD）

1. 16 张表（14 核心 + 2 关联）均可成功建表。
2. 所有表均包含统一审计与软删除字段。
3. 关键唯一约束与索引生效（`*_code`、`point_id`、核心组合索引）。
4. `ts_measure_current` 支持按 `point_id` UPSERT。
5. `ops_work_order_log` 可按 `(work_order_id, action_time)` 高效分页。
6. 生成的 Entity/Mapper 在多模块工程中可编译通过，命名与路径符合规范。

## 9. 迁移与回滚建议

- 按域拆分 migration：
  - `V2_1__task2_auth_schema.sql`
  - `V2_2__task2_asset_schema.sql`
  - `V2_3__task2_iot_schema.sql`
  - `V2_4__task2_ts_schema.sql`
  - `V2_5__task2_ops_schema.sql`
- 回滚以域为单位执行 drop（或软回滚：重命名 + 备份），避免一次性全量回滚风险。

## 10. 与后续任务衔接

- 任务 3（Auth/System）直接复用 `sys_user/sys_role/sys_user_role`。
- 任务 4（Asset/Device）直接复用资产与设备测点域表。
- 任务 5（Monitor）围绕 `ts_measure_current/ts_measure_history` 实现接收与趋势。
- 任务 6（Alert/WorkOrder）基于 `ops_alert_*` 与 `ops_work_order*` 实现闭环。
