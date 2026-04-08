package com.uscbinp.domain.service.alert.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.model.entity.OpsAlertEventEntity;
import com.uscbinp.domain.service.alert.AlertEventService;
import com.uscbinp.domain.service.workorder.impl.InMemoryAlertWorkOrderStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AlertEventServiceImpl implements AlertEventService {

    private final InMemoryAlertWorkOrderStore store;

    public AlertEventServiceImpl(InMemoryAlertWorkOrderStore store) {
        this.store = store;
    }

    @Override
    public AlertView confirm(Long alertId, Long operatorUserId) {
        OpsAlertEventEntity entity = requireAlert(alertId);
        if (entity.getAlertStatus() != null && entity.getAlertStatus() == 3) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "已关闭告警不允许确认");
        }
        entity.setAlertStatus(2);
        entity.setConfirmTime(LocalDateTime.now());
        store.saveAlert(entity);
        return toView(entity);
    }

    @Override
    public AlertView close(Long alertId, Long operatorUserId) {
        OpsAlertEventEntity entity = requireAlert(alertId);
        if (store.hasUnfinishedWorkOrder(alertId)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "存在未完成工单，禁止关闭告警");
        }
        entity.setAlertStatus(3);
        entity.setCloseTime(LocalDateTime.now());
        store.saveAlert(entity);
        return toView(entity);
    }

    @Override
    public PageResult pageQuery(PageQuery query) {
        int pageNum = Math.max(query.pageNum(), 1);
        int pageSize = Math.max(query.pageSize(), 1);
        List<AlertView> all = store.listAlerts().stream()
            .filter(alert -> query.alertStatus() == null || query.alertStatus().equals(alert.getAlertStatus()))
            .filter(alert -> query.alertLevel() == null || query.alertLevel().equals(alert.getAlertLevel()))
            .filter(alert -> query.startTime() == null || (alert.getOccurTime() != null && !alert.getOccurTime().isBefore(query.startTime())))
            .filter(alert -> query.endTime() == null || (alert.getOccurTime() != null && !alert.getOccurTime().isAfter(query.endTime())))
            .sorted(Comparator.comparing(OpsAlertEventEntity::getId))
            .map(this::toView)
            .toList();
        int from = Math.min((pageNum - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new PageResult(all.subList(from, to), all.size(), pageNum, pageSize);
    }

    private OpsAlertEventEntity requireAlert(Long alertId) {
        return store.findAlert(alertId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "告警不存在"));
    }

    private AlertView toView(OpsAlertEventEntity entity) {
        return new AlertView(
            entity.getId(),
            entity.getAlertCode(),
            entity.getAlertStatus(),
            entity.getAlertLevel(),
            entity.getOccurTime(),
            entity.getConfirmTime(),
            entity.getCloseTime(),
            entity.getWorkOrderId()
        );
    }
}
