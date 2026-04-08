# Task 4 Asset-Device Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于 `docs/superpowers/specs/2026-04-08-task4-asset-device-design.md` 落地统一编码、资产/设备四类档案 CRUD、关联校验与分页数据权限。

**Architecture:** 采用 `app -> domain` 轻量分层，app 暴露合同接口，domain 以内存实现承载业务规则与状态，确保一期可快速闭环并可测试。统一编码通过 `BizCodeService` 抽象收口，资产与设备服务复用同一编码与权限策略。通过 TDD 逐步落地：先写失败测试，再写最小实现，再回归全量测试。

**Tech Stack:** Java 17, Spring Boot 3.3.x, Spring MVC, Spring Security Test, JUnit5, MockMvc

---

## 文件结构与职责映射

- `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/code/`：统一业务编码服务。
- `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/asset/`：管网/管段服务。
- `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/device/`：设备/测点服务。
- `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/asset/`：资产接口控制器。
- `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/device/`：设备接口控制器。
- `backend/uscbinp-app/src/test/java/com/uscbinp/app/`：Asset/Device 合同测试。
- `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/`：编码与领域规则单测。

---

### Task 1: 统一编码服务

**Files:**
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/code/BizCodeService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/code/impl/InMemoryBizCodeService.java`
- Test: `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/code/BizCodeServiceTest.java`

- [x] **Step 1: 写失败测试并验证失败**
- [x] **Step 2: 实现最小编码服务（`{region}-{category}-{level}-{seq6}`）**
- [x] **Step 3: 回归测试并提交**

---

### Task 2: Asset 模块（network + pipe-section）

**Files:**
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/asset/AssetNetworkService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/asset/AssetPipeSectionService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/asset/impl/AssetNetworkServiceImpl.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/asset/impl/AssetPipeSectionServiceImpl.java`
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/asset/AssetNetworkController.java`
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/asset/AssetPipeSectionController.java`
- Test: `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/asset/AssetPipeSectionServiceTest.java`
- Test: `backend/uscbinp-app/src/test/java/com/uscbinp/app/AssetApiContractTest.java`

- [x] **Step 1: 写失败测试（`network_id` 存在性）**
- [x] **Step 2: 实现资产服务与接口**
- [x] **Step 3: 通过合同测试并提交**

---

### Task 3: Device 模块（device + measure-point）

**Files:**
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/device/IotDeviceService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/device/IotMeasurePointService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/device/impl/IotDeviceServiceImpl.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/device/impl/IotMeasurePointServiceImpl.java`
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/device/IotDeviceController.java`
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/device/IotMeasurePointController.java`
- Test: `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/device/IotMeasurePointServiceTest.java`
- Test: `backend/uscbinp-app/src/test/java/com/uscbinp/app/DeviceApiContractTest.java`

- [x] **Step 1: 写失败测试（`device_id` 存在性）**
- [x] **Step 2: 实现设备与测点服务/接口**
- [x] **Step 3: 通过合同测试并提交**

---

### Task 4: 数据权限收口与回归

**Files:**
- Modify: `backend/uscbinp-common/src/main/java/com/uscbinp/common/error/ErrorCode.java`
- Modify: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/asset/AssetNetworkController.java`
- Modify: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/asset/AssetPipeSectionController.java`
- Modify: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/device/IotDeviceController.java`
- Modify: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/device/IotMeasurePointController.java`
- Modify: `backend/uscbinp-app/src/test/java/com/uscbinp/app/AssetApiContractTest.java`
- Modify: `backend/uscbinp-app/src/test/java/com/uscbinp/app/DeviceApiContractTest.java`

- [x] **Step 1: 写失败测试（非管理员无区域时返回 `AUTH_4030`）**
- [x] **Step 2: 落地权限守卫与错误码**
- [x] **Step 3: 全量回归通过**

---

## 自检结果

1. **Spec 覆盖性**：已覆盖统一编码、四类档案 CRUD、关联校验、分页数据权限、合同测试与回归。  
2. **占位符扫描**：无 TBD/TODO/后续补充占位。  
3. **签名一致性**：Controller -> Service 命令对象与分页响应结构保持一致。
