package com.uscbinp.domain.service.workorder.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.model.entity.OpsWorkOrderLogEntity;
import com.uscbinp.domain.service.workorder.WorkOrderLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderLogServiceImpl implements WorkOrderLogService {

    private final InMemoryAlertWorkOrderStore store;

    public WorkOrderLogServiceImpl(InMemoryAlertWorkOrderStore store) {
        this.store = store;
    }

    @Override
    public void appendStatusLog(LogCommand command) {
        if (command.workOrderId() == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "工单日志缺少workOrderId");
        }
        OpsWorkOrderLogEntity entity = new OpsWorkOrderLogEntity();
        entity.setWorkOrderId(command.workOrderId());
        entity.setActionType(command.actionType());
        entity.setOperatorUserId(command.operatorUserId());
        entity.setBeforeStatus(command.beforeStatus());
        entity.setAfterStatus(command.afterStatus());
        entity.setActionDesc(command.actionDesc());
        entity.setActionTime(LocalDateTime.now());
        store.appendLog(entity);
    }

    @Override
    public List<LogView> listByWorkOrder(Long workOrderId) {
        return store.listLogs(workOrderId).stream()
            .map(log -> new LogView(
                log.getId(),
                log.getWorkOrderId(),
                log.getActionType(),
                log.getOperatorUserId(),
                log.getBeforeStatus(),
                log.getAfterStatus(),
                log.getActionDesc(),
                log.getActionTime()
            ))
            .toList();
    }
}
