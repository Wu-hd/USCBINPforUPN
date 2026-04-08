package com.uscbinp.domain.service.monitor.impl;

import com.uscbinp.domain.service.monitor.MonitorDataStore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class InMemoryMonitorDataStore implements MonitorDataStore {

    private final Map<Long, MeasurePointProfile> points = new ConcurrentHashMap<>();
    private final Map<Long, CurrentEntry> currentByPoint = new ConcurrentHashMap<>();
    private final List<HistoryEntry> historyEntries = new CopyOnWriteArrayList<>();
    private final List<AlertEventEntry> alertEvents = new CopyOnWriteArrayList<>();

    public InMemoryMonitorDataStore() {
        registerPointThreshold(1001L, "PRESSURE", 2001L, new BigDecimal("0.5000"), new BigDecimal("1.5000"), 1);
        registerPointThreshold(1002L, "FLOW", 2002L, new BigDecimal("10.0000"), new BigDecimal("80.0000"), 1);
    }

    public void registerPointThreshold(Long pointId, BigDecimal thresholdMin, BigDecimal thresholdMax) {
        registerPointThreshold(pointId, "PRESSURE", pointId + 1000, thresholdMin, thresholdMax, 1);
    }

    public void registerPointThreshold(
        Long pointId,
        String metricType,
        Long deviceId,
        BigDecimal thresholdMin,
        BigDecimal thresholdMax,
        Integer pointStatus
    ) {
        points.put(pointId, new MeasurePointProfile(
            pointId,
            deviceId,
            metricType,
            thresholdMin,
            thresholdMax,
            pointStatus
        ));
    }

    @Override
    public Optional<MeasurePointProfile> findPoint(Long pointId) {
        return Optional.ofNullable(points.get(pointId));
    }

    @Override
    public void appendHistory(HistoryEntry entry) {
        historyEntries.add(entry);
    }

    @Override
    public void upsertCurrent(CurrentEntry entry) {
        currentByPoint.put(entry.pointId(), entry);
    }

    @Override
    public int historySize() {
        return historyEntries.size();
    }

    @Override
    public BigDecimal currentValueOf(Long pointId) {
        CurrentEntry current = currentByPoint.get(pointId);
        return current == null ? null : current.currentValue();
    }

    @Override
    public List<HistoryEntry> listHistory(Long pointId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return historyEntries.stream()
            .filter(entry -> pointId == null || pointId.equals(entry.pointId()))
            .filter(entry -> startTime == null || !entry.collectTime().isBefore(startTime))
            .filter(entry -> endTime == null || !entry.collectTime().isAfter(endTime))
            .sorted(Comparator.comparing(HistoryEntry::collectTime))
            .limit(Math.max(limit, 0))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<CurrentEntry> listCurrent(Long pointId, Long deviceId) {
        return currentByPoint.values().stream()
            .filter(entry -> pointId == null || pointId.equals(entry.pointId()))
            .filter(entry -> deviceId == null || deviceId.equals(entry.deviceId()))
            .sorted(Comparator.comparing(CurrentEntry::pointId))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void appendAlertEvent(AlertEventEntry entry) {
        alertEvents.add(entry);
    }

    @Override
    public int alertEventSize() {
        return alertEvents.size();
    }

    @Override
    public List<AlertEventEntry> listAlertEvents() {
        return new ArrayList<>(alertEvents);
    }
}
