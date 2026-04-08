package com.uscbinp.domain.service.monitor.impl;

import com.uscbinp.domain.service.monitor.MonitorAlertEventService;
import com.uscbinp.domain.service.monitor.MonitorDataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class MonitorAlertEventServiceImpl implements MonitorAlertEventService {

    private static final long DEDUP_WINDOW_MINUTES = 1L;
    private final MonitorDataStore store;

    public MonitorAlertEventServiceImpl(MonitorDataStore store) {
        this.store = store;
    }

    @Override
    public String emitIfNeeded(AlertEmitCommand command) {
        if (command.alarmFlag() != 1) {
            return null;
        }
        LocalDateTime occurTime = command.occurTime() == null ? LocalDateTime.now() : command.occurTime();
        LocalDateTime windowStart = occurTime.minusMinutes(DEDUP_WINDOW_MINUTES);
        for (MonitorDataStore.AlertEventEntry event : store.listAlertEvents()) {
            if (command.pointId().equals(event.pointId())
                && event.occurTime() != null
                && !event.occurTime().isBefore(windowStart)) {
                return event.alertCode();
            }
        }

        String alertCode = "ALT-" + command.pointId() + "-" + occurTime.toEpochSecond(ZoneOffset.UTC);
        store.appendAlertEvent(new MonitorDataStore.AlertEventEntry(
            alertCode,
            null,
            "MEASURE_POINT",
            command.pointId(),
            command.pointId(),
            command.metricType() + "异常",
            2,
            1,
            occurTime,
            command.traceId()
        ));
        return alertCode;
    }
}
