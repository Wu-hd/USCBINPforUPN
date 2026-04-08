package com.uscbinp.domain.service.workorder;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderLogService {

    void appendStatusLog(LogCommand command);

    List<LogView> listByWorkOrder(Long workOrderId);

    record LogCommand(
        Long workOrderId,
        String actionType,
        Long operatorUserId,
        Integer beforeStatus,
        Integer afterStatus,
        String actionDesc
    ) {
    }

    record LogView(
        Long id,
        Long workOrderId,
        String actionType,
        Long operatorUserId,
        Integer beforeStatus,
        Integer afterStatus,
        String actionDesc,
        LocalDateTime actionTime
    ) {
    }
}
