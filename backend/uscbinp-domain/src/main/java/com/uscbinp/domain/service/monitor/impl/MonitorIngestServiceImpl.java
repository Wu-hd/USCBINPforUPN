package com.uscbinp.domain.service.monitor.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.monitor.MonitorAlertEventService;
import com.uscbinp.domain.service.monitor.MonitorDataStore;
import com.uscbinp.domain.service.monitor.MonitorIngestService;
import com.uscbinp.domain.service.monitor.MonitorThresholdEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MonitorIngestServiceImpl implements MonitorIngestService {

    private final MonitorDataStore store;
    private final MonitorThresholdEvaluator thresholdEvaluator;
    private final MonitorAlertEventService alertEventService;

    public MonitorIngestServiceImpl(MonitorDataStore store) {
        this(store, new MonitorThresholdEvaluatorImpl(), new MonitorAlertEventServiceImpl(store));
    }

    public MonitorIngestServiceImpl(MonitorDataStore store, MonitorAlertEventService alertEventService) {
        this(store, new MonitorThresholdEvaluatorImpl(), alertEventService);
    }

    @Autowired
    public MonitorIngestServiceImpl(
        MonitorDataStore store,
        MonitorThresholdEvaluator thresholdEvaluator,
        MonitorAlertEventService alertEventService
    ) {
        this.store = store;
        this.thresholdEvaluator = thresholdEvaluator;
        this.alertEventService = alertEventService;
    }

    @Override
    public IngestResult ingest(MeasureIngestCommand command) {
        MonitorDataStore.MeasurePointProfile point = store.findPoint(command.pointId())
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "点位不存在"));
        if (point.pointStatus() == null || point.pointStatus() != 1) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "点位已停用");
        }
        if (!point.metricType().equalsIgnoreCase(command.metricType())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "指标类型不匹配");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime collectTime = command.collectTime() == null ? now : command.collectTime();
        Integer qualityFlag = command.qualityFlag() == null ? 1 : command.qualityFlag();
        int alarmFlag = thresholdEvaluator.evaluate(command.metricValue(), point.thresholdMin(), point.thresholdMax());
        store.appendHistory(new MonitorDataStore.HistoryEntry(
            command.pointId(),
            null,
            command.metricType(),
            command.metricValue(),
            qualityFlag,
            collectTime,
            now,
            null,
            command.traceId()
        ));
        store.upsertCurrent(new MonitorDataStore.CurrentEntry(
            command.pointId(),
            point.deviceId(),
            command.metricType(),
            command.metricValue(),
            qualityFlag,
            alarmFlag,
            collectTime,
            now,
            command.edgeNodeCode(),
            null,
            command.traceId()
        ));

        String alertCode = alertEventService.emitIfNeeded(new MonitorAlertEventService.AlertEmitCommand(
            command.pointId(),
            command.metricType(),
            command.metricValue(),
            alarmFlag,
            collectTime,
            command.traceId()
        ));
        return new IngestResult(true, alarmFlag, command.metricValue(), alertCode);
    }
}
