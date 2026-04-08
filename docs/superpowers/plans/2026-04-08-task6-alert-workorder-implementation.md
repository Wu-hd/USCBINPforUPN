# Task 6 Alert & WorkOrder Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 落地 Task6 告警处置闭环：告警分页/确认/关闭、工单创建与流转、状态变更强制写日志。

**Architecture:** 复用现有 `app -> domain -> infra` 分层，在 domain 新增 Alert/WorkOrder 领域服务与状态机编排，在 app 提供告警与工单接口，在 infra 复用现有 Mapper。当前工程无真实数据源运行链路，因此先采用内存态实现保证一期合同测试和业务闭环，再保持接口与状态语义与物理表一致，为后续持久化替换留出边界。

**Tech Stack:** Java 17, Spring Boot 3.3.x, Spring MVC, Spring Security, JUnit5, MockMvc

---

## 文件结构与职责映射

- `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/alert/`
  - 告警查询、确认、关闭服务接口。
- `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/`
  - 工单创建/派单/开始/完成服务接口。
- `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/`
  - 内存态状态机实现、日志强约束实现。
- `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/alert/`
  - 告警接口控制器。
- `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/workorder/`
  - 工单接口控制器。
- `backend/uscbinp-app/src/test/java/com/uscbinp/app/`
  - Task6 合同测试。
- `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/`
  - Task6 领域流转测试与联动测试。

---

### Task 1: 领域状态机基座（告警、工单、日志）

**Files:**
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/WorkOrderService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/WorkOrderLogService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/alert/AlertEventService.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/InMemoryAlertWorkOrderStore.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/WorkOrderLogServiceImpl.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/WorkOrderServiceImpl.java`
- Create: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/alert/impl/AlertEventServiceImpl.java`
- Test: `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/workorder/WorkOrderFlowServiceTest.java`

- [ ] **Step 1: 写失败测试（流转 + 日志必写）**

```java
class WorkOrderFlowServiceTest {
    @Test
    void finishShouldWriteStatusLog() {
        InMemoryAlertWorkOrderStore store = new InMemoryAlertWorkOrderStore();
        WorkOrderLogService logService = new WorkOrderLogServiceImpl(store);
        WorkOrderService service = new WorkOrderServiceImpl(store, logService);

        WorkOrderService.WorkOrderView created = service.create(new WorkOrderService.CreateWorkOrderCommand(
            "ALERT_EVENT", 9001L, "pressure high", "PIPE_SECTION", 3001L, "3301", 101L, null));
        service.start(new WorkOrderService.StartWorkOrderCommand(created.id(), 101L));
        service.finish(new WorkOrderService.FinishWorkOrderCommand(created.id(), 101L, "done"));

        assertEquals(3, store.listLogs(created.id()).size());
        assertEquals("FINISH", store.listLogs(created.id()).get(2).actionType());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl backend/uscbinp-domain -am -Dtest=WorkOrderFlowServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: FAIL（Task6 领域服务尚未实现）

- [ ] **Step 3: 实现最小可通过代码**

```java
public interface WorkOrderService {
    WorkOrderView create(CreateWorkOrderCommand command);
    WorkOrderView assign(AssignWorkOrderCommand command);
    WorkOrderView start(StartWorkOrderCommand command);
    WorkOrderView finish(FinishWorkOrderCommand command);
    PageResult pageQuery(PageQuery query);
}
```

```java
// WorkOrderServiceImpl.finish 关键约束
if (entity.getWorkStatus() == null || entity.getWorkStatus() != 2) {
    throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "工单状态不允许完成");
}
Integer before = entity.getWorkStatus();
entity.setWorkStatus(3);
entity.setActualFinishTime(LocalDateTime.now());
entity.setResultSummary(command.resultSummary());
store.saveWorkOrder(entity);
logService.appendStatusLog(new WorkOrderLogService.LogCommand(
    entity.getId(), "FINISH", command.operatorUserId(), before, entity.getWorkStatus(), "工单完成"));
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -q -pl backend/uscbinp-domain -am -Dtest=WorkOrderFlowServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/alert backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/workorder/WorkOrderFlowServiceTest.java
git commit -m "feat(task6): add alert and workorder domain state machine"
```

---

### Task 2: 告警接口与关闭前置校验

**Files:**
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/alert/AlertEventController.java`
- Test: `backend/uscbinp-app/src/test/java/com/uscbinp/app/AlertEventApiContractTest.java`
- Modify: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/alert/impl/AlertEventServiceImpl.java`

- [ ] **Step 1: 写失败合同测试（分页、确认、关闭校验）**

```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class AlertEventApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void confirmShouldUpdateAlertStatus() throws Exception {
        mockMvc.perform(put("/api/alerts/events/9001/confirm"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.alertStatus").value(2));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl backend/uscbinp-app -am -Dtest=AlertEventApiContractTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: FAIL（AlertEventController 不存在）

- [ ] **Step 3: 实现接口与关闭前置校验**

```java
@RestController
@RequestMapping("/api/alerts/events")
public class AlertEventController {
    @GetMapping
    public ApiResponse<AlertEventService.PageResult> page(...){}
    @PutMapping("/{id}/confirm")
    public ApiResponse<AlertEventService.AlertView> confirm(@PathVariable Long id) {}
    @PutMapping("/{id}/close")
    public ApiResponse<AlertEventService.AlertView> close(@PathVariable Long id) {}
}
```

```java
// AlertEventServiceImpl.close 关键约束
if (store.hasUnfinishedWorkOrder(alertId)) {
    throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "存在未完成工单，禁止关闭告警");
}
entity.setAlertStatus(3);
entity.setCloseTime(LocalDateTime.now());
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -q -pl backend/uscbinp-app -am -Dtest=AlertEventApiContractTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/alert/AlertEventController.java backend/uscbinp-app/src/test/java/com/uscbinp/app/AlertEventApiContractTest.java backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/alert/impl/AlertEventServiceImpl.java
git commit -m "feat(task6): add alert event APIs with close guard"
```

---

### Task 3: 工单接口与告警来源建单

**Files:**
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/workorder/WorkOrderController.java`
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/workorder/dto/CreateWorkOrderRequest.java`
- Create: `backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/workorder/dto/FinishWorkOrderRequest.java`
- Test: `backend/uscbinp-app/src/test/java/com/uscbinp/app/WorkOrderApiContractTest.java`
- Modify: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/WorkOrderServiceImpl.java`

- [ ] **Step 1: 写失败合同测试（创建、开始、完成）**

```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class WorkOrderApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void createFromAlertShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/workorders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceType\":\"ALERT_EVENT\",\"sourceId\":9001,\"title\":\"repair\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.sourceType").value("ALERT_EVENT"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl backend/uscbinp-app -am -Dtest=WorkOrderApiContractTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: FAIL（WorkOrderController 不存在）

- [ ] **Step 3: 实现控制器与领域联动**

```java
@PostMapping
public ApiResponse<WorkOrderService.WorkOrderView> create(@Valid @RequestBody CreateWorkOrderRequest request) {
    return ApiResponse.ok(workOrderService.create(new WorkOrderService.CreateWorkOrderCommand(
        request.sourceType(), request.sourceId(), request.title(), request.targetType(),
        request.targetId(), request.regionCode(), request.assigneeUserId(), request.expectFinishTime()
    )));
}
```

```java
// WorkOrderServiceImpl.create 关键逻辑
if ("ALERT_EVENT".equals(command.sourceType())) {
    store.bindAlertToWorkOrder(command.sourceId(), entity.getId());
}
logService.appendStatusLog(new WorkOrderLogService.LogCommand(
    entity.getId(), "CREATE", defaultOperator(), null, entity.getWorkStatus(), "工单创建"));
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -q -pl backend/uscbinp-app -am -Dtest=WorkOrderApiContractTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/uscbinp-app/src/main/java/com/uscbinp/app/controller/workorder backend/uscbinp-app/src/test/java/com/uscbinp/app/WorkOrderApiContractTest.java backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/WorkOrderServiceImpl.java
git commit -m "feat(task6): add workorder APIs and alert linkage"
```

---

### Task 4: 联动测试与全量回归

**Files:**
- Test: `backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/workorder/AlertWorkOrderLinkageTest.java`
- Modify: `backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/InMemoryAlertWorkOrderStore.java`

- [ ] **Step 1: 写失败测试（告警来源建单 + 完成后关闭）**

```java
class AlertWorkOrderLinkageTest {
    @Test
    void shouldCloseAlertAfterWorkOrderFinished() {
        InMemoryAlertWorkOrderStore store = new InMemoryAlertWorkOrderStore();
        WorkOrderService workOrderService = ...
        AlertEventService alertEventService = ...
        WorkOrderService.WorkOrderView order = workOrderService.create(...);
        workOrderService.start(...);
        workOrderService.finish(...);
        AlertEventService.AlertView closed = alertEventService.close(order.sourceId(), 101L);
        assertEquals(3, closed.alertStatus());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl backend/uscbinp-domain -am -Dtest=AlertWorkOrderLinkageTest -Dsurefire.failIfNoSpecifiedTests=false test`  
Expected: FAIL（联动细节未补齐）

- [ ] **Step 3: 实现最小联动补齐**

```java
// InMemoryAlertWorkOrderStore 需支持
boolean hasUnfinishedWorkOrder(Long alertId);
void bindAlertToWorkOrder(Long alertId, Long workOrderId);
List<OpsWorkOrderLogEntity> listLogs(Long workOrderId);
```

- [ ] **Step 4: 运行回归**

Run:
- `mvn -q -pl backend/uscbinp-domain -am test`
- `mvn -q -pl backend/uscbinp-app -am test`
- `mvn -q test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/uscbinp-domain/src/test/java/com/uscbinp/domain/service/workorder/AlertWorkOrderLinkageTest.java backend/uscbinp-domain/src/main/java/com/uscbinp/domain/service/workorder/impl/InMemoryAlertWorkOrderStore.java
git commit -m "test(task6): complete alert-workorder linkage verification"
```

---

## 自检结论（对照 spec）

1. **Spec 覆盖性**：告警分页/确认/关闭、工单建单与流转、日志强约束、告警工单关联均有对应任务。  
2. **占位符扫描**：无 TBD/TODO/“后续补充”类占位语句。  
3. **类型一致性**：`WorkOrderView`、`AlertView`、`LogCommand`、`CreateWorkOrderCommand` 在任务间命名保持一致。

---

Plan complete and saved to `docs/superpowers/plans/2026-04-08-task6-alert-workorder-implementation.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
