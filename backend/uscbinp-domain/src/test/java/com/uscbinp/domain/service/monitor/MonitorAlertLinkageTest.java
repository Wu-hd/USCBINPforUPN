package com.uscbinp.domain.service.monitor;

import com.uscbinp.domain.service.monitor.impl.InMemoryMonitorDataStore;
import com.uscbinp.domain.service.monitor.impl.MonitorAlertEventServiceImpl;
import com.uscbinp.domain.service.monitor.impl.MonitorIngestServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonitorAlertLinkageTest {

    @Test
    void ingestOverThresholdShouldCreateAlertEvent() {
        InMemoryMonitorDataStore store = new InMemoryMonitorDataStore();
        store.registerPointThreshold(1001L, new BigDecimal("0.5000"), new BigDecimal("1.5000"));
        MonitorAlertEventService alertService = new MonitorAlertEventServiceImpl(store);
        MonitorIngestService service = new MonitorIngestServiceImpl(store, alertService);

        MonitorIngestService.IngestResult result = service.ingest(new MonitorIngestService.MeasureIngestCommand(
            1001L, "PRESSURE", new BigDecimal("2.2000"), LocalDateTime.now(), 1, "edge-a", "trace-x"));

        assertEquals(1, result.alarmFlag());
        assertNotNull(result.alertCode());
        assertEquals(1, store.alertEventSize());
    }
}
