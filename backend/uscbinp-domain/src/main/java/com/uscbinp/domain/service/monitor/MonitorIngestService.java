package com.uscbinp.domain.service.monitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MonitorIngestService {

    IngestResult ingest(MeasureIngestCommand command);

    record MeasureIngestCommand(
        Long pointId,
        String metricType,
        BigDecimal metricValue,
        LocalDateTime collectTime,
        Integer qualityFlag,
        String edgeNodeCode,
        String traceId
    ) {
    }

    record IngestResult(
        boolean accepted,
        int alarmFlag,
        BigDecimal currentValue,
        String alertCode
    ) {
    }
}
