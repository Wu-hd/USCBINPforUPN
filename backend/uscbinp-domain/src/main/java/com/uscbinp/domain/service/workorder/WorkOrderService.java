package com.uscbinp.domain.service.workorder;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderService {

    WorkOrderView create(CreateWorkOrderCommand command);

    WorkOrderView assign(AssignWorkOrderCommand command);

    WorkOrderView start(StartWorkOrderCommand command);

    WorkOrderView finish(FinishWorkOrderCommand command);

    PageResult pageQuery(PageQuery query);

    record CreateWorkOrderCommand(
        String sourceType,
        Long sourceId,
        String title,
        String targetType,
        Long targetId,
        String regionCode,
        Long assigneeUserId,
        LocalDateTime expectFinishTime
    ) {
    }

    record AssignWorkOrderCommand(
        Long workOrderId,
        Long assigneeUserId,
        Long operatorUserId
    ) {
    }

    record StartWorkOrderCommand(
        Long workOrderId,
        Long operatorUserId
    ) {
    }

    record FinishWorkOrderCommand(
        Long workOrderId,
        Long operatorUserId,
        String resultSummary
    ) {
    }

    record PageQuery(
        int pageNum,
        int pageSize,
        Integer workStatus,
        String sourceType,
        Long sourceId
    ) {
    }

    record WorkOrderView(
        Long id,
        String workOrderCode,
        String sourceType,
        Long sourceId,
        String title,
        String targetType,
        Long targetId,
        String regionCode,
        Long assigneeUserId,
        Integer workStatus,
        LocalDateTime expectFinishTime,
        LocalDateTime actualFinishTime,
        String resultSummary
    ) {
    }

    record PageResult(
        List<WorkOrderView> items,
        long total,
        long totalLogs,
        int pageNum,
        int pageSize
    ) {
    }
}
