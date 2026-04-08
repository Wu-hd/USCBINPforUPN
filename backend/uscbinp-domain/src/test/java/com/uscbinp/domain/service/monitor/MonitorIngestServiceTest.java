package com.uscbinp.domain.service.monitor;

import com.uscbinp.domain.service.monitor.impl.InMemoryMonitorDataStore;
import com.uscbinp.domain.service.monitor.impl.MonitorIngestServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonitorIngestServiceTest {

    @Test
    void ingestShouldAppendHistoryAndUpsertCurrent() {
        InMemoryMonitorDataStore store = new InMemoryMonitorDataStore();
        MonitorIngestService service = new MonitorIngestServiceImpl(store);
        LocalDateTime now = LocalDateTime.now();
        service.ingest(new MonitorIngestService.MeasureIngestCommand(
            1001L, "PRESSURE", new BigDecimal("1.23"), now, 1, "edge-a", "trace-1"));
        service.ingest(new MonitorIngestService.MeasureIngestCommand(
            1001L, "PRESSURE", new BigDecimal("2.34"), now.plusSeconds(5), 1, "edge-a", "trace-2"));

        assertEquals(2, store.historySize());
        assertEquals(new BigDecimal("2.34"), store.currentValueOf(1001L));
    }
}
