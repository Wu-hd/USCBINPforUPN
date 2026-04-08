package com.uscbinp.domain.service.workorder.impl;

import com.uscbinp.domain.model.entity.OpsAlertEventEntity;
import com.uscbinp.domain.model.entity.OpsWorkOrderEntity;
import com.uscbinp.domain.model.entity.OpsWorkOrderLogEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryAlertWorkOrderStore {

    private final Map<Long, OpsAlertEventEntity> alertMap = new ConcurrentHashMap<>();
    private final Map<Long, OpsWorkOrderEntity> workOrderMap = new ConcurrentHashMap<>();
    private final Map<Long, List<OpsWorkOrderLogEntity>> logsByOrder = new ConcurrentHashMap<>();
    private final AtomicLong workOrderIdSeq = new AtomicLong(10000);
    private final AtomicLong workOrderCodeSeq = new AtomicLong(1);
    private final AtomicLong logIdSeq = new AtomicLong(1);

    public InMemoryAlertWorkOrderStore() {
        OpsAlertEventEntity seed = new OpsAlertEventEntity();
        seed.setId(9001L);
        seed.setAlertCode("ALT-9001");
        seed.setAlertTitle("seed-alert");
        seed.setAlertLevel(2);
        seed.setAlertStatus(1);
        seed.setOccurTime(LocalDateTime.now().minusMinutes(5));
        alertMap.put(seed.getId(), seed);
    }

    public Optional<OpsAlertEventEntity> findAlert(Long alertId) {
        return Optional.ofNullable(alertMap.get(alertId));
    }

    public void saveAlert(OpsAlertEventEntity entity) {
        alertMap.put(entity.getId(), entity);
    }

    public List<OpsAlertEventEntity> listAlerts() {
        return new ArrayList<>(alertMap.values());
    }

    public OpsWorkOrderEntity saveWorkOrder(OpsWorkOrderEntity entity) {
        if (entity.getId() == null) {
            entity.setId(workOrderIdSeq.incrementAndGet());
        }
        workOrderMap.put(entity.getId(), entity);
        return entity;
    }

    public Optional<OpsWorkOrderEntity> findWorkOrder(Long workOrderId) {
        return Optional.ofNullable(workOrderMap.get(workOrderId));
    }

    public List<OpsWorkOrderEntity> listWorkOrders() {
        return new ArrayList<>(workOrderMap.values());
    }

    public void appendLog(OpsWorkOrderLogEntity logEntity) {
        if (logEntity.getId() == null) {
            logEntity.setId(logIdSeq.incrementAndGet());
        }
        logsByOrder.computeIfAbsent(logEntity.getWorkOrderId(), key -> new CopyOnWriteArrayList<>()).add(logEntity);
    }

    public List<OpsWorkOrderLogEntity> listLogs(Long workOrderId) {
        return new ArrayList<>(logsByOrder.getOrDefault(workOrderId, List.of()));
    }

    public boolean hasUnfinishedWorkOrder(Long alertId) {
        return workOrderMap.values().stream()
            .anyMatch(order -> "ALERT_EVENT".equals(order.getSourceType())
                && alertId.equals(order.getSourceId())
                && (order.getWorkStatus() == null || order.getWorkStatus() != 3));
    }

    public void bindAlertToWorkOrder(Long alertId, Long workOrderId) {
        OpsAlertEventEntity alert = alertMap.get(alertId);
        if (alert != null) {
            alert.setWorkOrderId(workOrderId);
            saveAlert(alert);
        }
    }

    public String nextWorkOrderCode(String regionCode) {
        String region = (regionCode == null || regionCode.isBlank()) ? "0000" : regionCode;
        return "WO-" + region + "-" + String.format("%06d", workOrderCodeSeq.getAndIncrement());
    }
}
