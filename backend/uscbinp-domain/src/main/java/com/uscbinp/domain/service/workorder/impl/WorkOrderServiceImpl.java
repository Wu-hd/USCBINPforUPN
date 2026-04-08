package com.uscbinp.domain.service.workorder.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.model.entity.OpsWorkOrderEntity;
import com.uscbinp.domain.service.workorder.WorkOrderLogService;
import com.uscbinp.domain.service.workorder.WorkOrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final InMemoryAlertWorkOrderStore store;
    private final WorkOrderLogService logService;

    public WorkOrderServiceImpl(InMemoryAlertWorkOrderStore store, WorkOrderLogService logService) {
        this.store = store;
        this.logService = logService;
    }

    @Override
    public WorkOrderView create(CreateWorkOrderCommand command) {
        if (command.title() == null || command.title().isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "工单标题不能为空");
        }
        OpsWorkOrderEntity entity = new OpsWorkOrderEntity();
        entity.setWorkOrderCode(store.nextWorkOrderCode(command.regionCode()));
        entity.setSourceType(command.sourceType());
        entity.setSourceId(command.sourceId());
        entity.setTitle(command.title());
        entity.setTargetType(command.targetType());
        entity.setTargetId(command.targetId());
        entity.setRegionCode(command.regionCode());
        entity.setAssigneeUserId(command.assigneeUserId());
        entity.setWorkStatus(1);
        entity.setExpectFinishTime(command.expectFinishTime());
        store.saveWorkOrder(entity);
        if ("ALERT_EVENT".equals(command.sourceType()) && command.sourceId() != null) {
            store.bindAlertToWorkOrder(command.sourceId(), entity.getId());
        }
        logService.appendStatusLog(new WorkOrderLogService.LogCommand(
            entity.getId(),
            "CREATE",
            defaultOperator(command.assigneeUserId()),
            null,
            entity.getWorkStatus(),
            "工单创建"
        ));
        return toView(entity);
    }

    @Override
    public WorkOrderView assign(AssignWorkOrderCommand command) {
        OpsWorkOrderEntity entity = requireWorkOrder(command.workOrderId());
        Integer before = entity.getWorkStatus();
        entity.setAssigneeUserId(command.assigneeUserId());
        if (entity.getWorkStatus() == null) {
            entity.setWorkStatus(1);
        }
        store.saveWorkOrder(entity);
        logService.appendStatusLog(new WorkOrderLogService.LogCommand(
            entity.getId(),
            "ASSIGN",
            defaultOperator(command.operatorUserId()),
            before,
            entity.getWorkStatus(),
            "工单派单"
        ));
        return toView(entity);
    }

    @Override
    public WorkOrderView start(StartWorkOrderCommand command) {
        OpsWorkOrderEntity entity = requireWorkOrder(command.workOrderId());
        if (entity.getWorkStatus() == null || entity.getWorkStatus() != 1) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "工单状态不允许开始");
        }
        Integer before = entity.getWorkStatus();
        entity.setWorkStatus(2);
        store.saveWorkOrder(entity);
        logService.appendStatusLog(new WorkOrderLogService.LogCommand(
            entity.getId(),
            "START",
            defaultOperator(command.operatorUserId()),
            before,
            entity.getWorkStatus(),
            "工单开始处理"
        ));
        return toView(entity);
    }

    @Override
    public WorkOrderView finish(FinishWorkOrderCommand command) {
        OpsWorkOrderEntity entity = requireWorkOrder(command.workOrderId());
        if (entity.getWorkStatus() == null || entity.getWorkStatus() != 2) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "工单状态不允许完成");
        }
        Integer before = entity.getWorkStatus();
        entity.setWorkStatus(3);
        entity.setActualFinishTime(LocalDateTime.now());
        entity.setResultSummary(command.resultSummary());
        store.saveWorkOrder(entity);
        logService.appendStatusLog(new WorkOrderLogService.LogCommand(
            entity.getId(),
            "FINISH",
            defaultOperator(command.operatorUserId()),
            before,
            entity.getWorkStatus(),
            "工单完成"
        ));
        return toView(entity);
    }

    @Override
    public PageResult pageQuery(PageQuery query) {
        int pageNum = Math.max(query.pageNum(), 1);
        int pageSize = Math.max(query.pageSize(), 1);
        List<WorkOrderView> all = store.listWorkOrders().stream()
            .filter(order -> query.workStatus() == null || query.workStatus().equals(order.getWorkStatus()))
            .filter(order -> query.sourceType() == null || query.sourceType().equals(order.getSourceType()))
            .filter(order -> query.sourceId() == null || query.sourceId().equals(order.getSourceId()))
            .sorted(Comparator.comparing(OpsWorkOrderEntity::getId))
            .map(this::toView)
            .toList();
        int from = Math.min((pageNum - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        long totalLogs = store.listWorkOrders().stream().mapToLong(order -> store.listLogs(order.getId()).size()).sum();
        return new PageResult(all.subList(from, to), all.size(), totalLogs, pageNum, pageSize);
    }

    private OpsWorkOrderEntity requireWorkOrder(Long workOrderId) {
        return store.findWorkOrder(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "工单不存在"));
    }

    private Long defaultOperator(Long operatorUserId) {
        return operatorUserId == null ? 0L : operatorUserId;
    }

    private WorkOrderView toView(OpsWorkOrderEntity entity) {
        return new WorkOrderView(
            entity.getId(),
            entity.getWorkOrderCode(),
            entity.getSourceType(),
            entity.getSourceId(),
            entity.getTitle(),
            entity.getTargetType(),
            entity.getTargetId(),
            entity.getRegionCode(),
            entity.getAssigneeUserId(),
            entity.getWorkStatus(),
            entity.getExpectFinishTime(),
            entity.getActualFinishTime(),
            entity.getResultSummary()
        );
    }
}
