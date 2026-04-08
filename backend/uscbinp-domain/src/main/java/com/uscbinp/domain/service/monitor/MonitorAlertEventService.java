package com.uscbinp.domain.service.monitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MonitorAlertEventService {

    String emitIfNeeded(AlertEmitCommand command);

    record AlertEmitCommand(
        Long pointId,
        String metricType,
        BigDecimal metricValue,
        int alarmFlag,
        LocalDateTime occurTime,
        String traceId
    ) {
    }
}
