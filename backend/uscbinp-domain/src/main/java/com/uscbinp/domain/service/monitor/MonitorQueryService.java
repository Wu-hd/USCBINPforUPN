package com.uscbinp.domain.service.monitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MonitorQueryService {

    CurrentPageResult queryCurrent(CurrentQuery query);

    HistoryResult queryHistory(HistoryQuery query);

    record CurrentQuery(
        Long pointId,
        Long deviceId,
        int pageNum,
        int pageSize
    ) {
    }

    record HistoryQuery(
        Long pointId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer limit
    ) {
    }

    record CurrentItem(
        Long pointId,
        Long deviceId,
        String metricType,
        BigDecimal currentValue,
        Integer qualityFlag,
        Integer alarmFlag,
        LocalDateTime collectTime,
        String traceId
    ) {
    }

    record CurrentPageResult(
        List<CurrentItem> items,
        long total,
        int pageNum,
        int pageSize
    ) {
    }

    record HistoryItem(
        Long pointId,
        String metricType,
        BigDecimal metricValue,
        Integer qualityFlag,
        LocalDateTime collectTime,
        String traceId
    ) {
    }

    record HistoryResult(
        List<HistoryItem> items
    ) {
    }
}
