package com.uscbinp.domain.service.monitor.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.monitor.MonitorDataStore;
import com.uscbinp.domain.service.monitor.MonitorQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorQueryServiceImpl implements MonitorQueryService {

    private static final int DEFAULT_HISTORY_LIMIT = 500;
    private static final int MAX_HISTORY_LIMIT = 5000;

    private final MonitorDataStore store;

    public MonitorQueryServiceImpl(MonitorDataStore store) {
        this.store = store;
    }

    @Override
    public CurrentPageResult queryCurrent(CurrentQuery query) {
        int pageNum = Math.max(query.pageNum(), 1);
        int pageSize = Math.max(query.pageSize(), 1);
        List<CurrentItem> allItems = store.listCurrent(query.pointId(), query.deviceId()).stream()
            .map(entry -> new CurrentItem(
                entry.pointId(),
                entry.deviceId(),
                entry.metricType(),
                entry.currentValue(),
                entry.qualityFlag(),
                entry.alarmFlag(),
                entry.collectTime(),
                entry.traceId()
            ))
            .toList();
        int fromIndex = Math.min((pageNum - 1) * pageSize, allItems.size());
        int toIndex = Math.min(fromIndex + pageSize, allItems.size());
        return new CurrentPageResult(allItems.subList(fromIndex, toIndex), allItems.size(), pageNum, pageSize);
    }

    @Override
    public HistoryResult queryHistory(HistoryQuery query) {
        if (query.pointId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR.getCode(), "pointId不能为空");
        }
        if (query.startTime() != null && query.endTime() != null && query.startTime().isAfter(query.endTime())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR.getCode(), "时间区间不合法");
        }
        int limit = safeHistoryLimit(query.limit());
        List<HistoryItem> items = store.listHistory(query.pointId(), query.startTime(), query.endTime(), limit).stream()
            .map(entry -> new HistoryItem(
                entry.pointId(),
                entry.metricType(),
                entry.metricValue(),
                entry.qualityFlag(),
                entry.collectTime(),
                entry.traceId()
            ))
            .toList();
        return new HistoryResult(items);
    }

    private int safeHistoryLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_HISTORY_LIMIT;
        }
        if (limit <= 0) {
            return DEFAULT_HISTORY_LIMIT;
        }
        return Math.min(limit, MAX_HISTORY_LIMIT);
    }
}
