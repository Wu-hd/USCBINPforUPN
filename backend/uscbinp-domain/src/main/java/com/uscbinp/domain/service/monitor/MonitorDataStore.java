package com.uscbinp.domain.service.monitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MonitorDataStore {

    Optional<MeasurePointProfile> findPoint(Long pointId);

    void appendHistory(HistoryEntry entry);

    void upsertCurrent(CurrentEntry entry);

    int historySize();

    BigDecimal currentValueOf(Long pointId);

    List<HistoryEntry> listHistory(Long pointId, LocalDateTime startTime, LocalDateTime endTime, int limit);

    List<CurrentEntry> listCurrent(Long pointId, Long deviceId);

    void appendAlertEvent(AlertEventEntry entry);

    int alertEventSize();

    List<AlertEventEntry> listAlertEvents();

    record MeasurePointProfile(
        Long pointId,
        Long deviceId,
        String metricType,
        BigDecimal thresholdMin,
        BigDecimal thresholdMax,
        Integer pointStatus
    ) {
    }

    record HistoryEntry(
        Long pointId,
        Long sectionId,
        String metricType,
        BigDecimal metricValue,
        Integer qualityFlag,
        LocalDateTime collectTime,
        LocalDateTime receiveTime,
        String calcTag,
        String traceId
    ) {
    }

    record CurrentEntry(
        Long pointId,
        Long deviceId,
        String metricType,
        BigDecimal currentValue,
        Integer qualityFlag,
        Integer alarmFlag,
        LocalDateTime collectTime,
        LocalDateTime receiveTime,
        String edgeNodeCode,
        String rawPayload,
        String traceId
    ) {
    }

    record AlertEventEntry(
        String alertCode,
        Long ruleId,
        String targetType,
        Long targetId,
        Long pointId,
        String alertTitle,
        Integer alertLevel,
        Integer alertStatus,
        LocalDateTime occurTime,
        String traceId
    ) {
    }
}
