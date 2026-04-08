package com.uscbinp.domain.service.monitor;

import com.uscbinp.domain.service.monitor.impl.InMemoryMonitorDataStore;
import com.uscbinp.domain.service.monitor.impl.MonitorIngestServiceImpl;
import com.uscbinp.domain.service.monitor.impl.MonitorQueryServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonitorQueryServiceTest {

    @Test
    void queryHistoryShouldFilterByRangeAndSortAsc() {
        InMemoryMonitorDataStore store = new InMemoryMonitorDataStore();
        MonitorIngestService ingestService = new MonitorIngestServiceImpl(store);
        MonitorQueryService queryService = new MonitorQueryServiceImpl(store);
        LocalDateTime base = LocalDateTime.of(2026, 4, 8, 10, 0, 0);
        ingestService.ingest(new MonitorIngestService.MeasureIngestCommand(
            1001L, "PRESSURE", new BigDecimal("1.00"), base, 1, "edge-a", "h1"));
        ingestService.ingest(new MonitorIngestService.MeasureIngestCommand(
            1001L, "PRESSURE", new BigDecimal("1.20"), base.plusMinutes(1), 1, "edge-a", "h2"));
        ingestService.ingest(new MonitorIngestService.MeasureIngestCommand(
            1001L, "PRESSURE", new BigDecimal("1.40"), base.plusMinutes(2), 1, "edge-a", "h3"));

        MonitorQueryService.HistoryResult history = queryService.queryHistory(new MonitorQueryService.HistoryQuery(
            1001L, base.plusSeconds(30), base.plusMinutes(2), 100));
        List<MonitorQueryService.HistoryItem> items = history.items();
        assertEquals(2, items.size());
        assertEquals(new BigDecimal("1.20"), items.get(0).metricValue());
        assertEquals(new BigDecimal("1.40"), items.get(1).metricValue());
    }

    @Test
    void queryCurrentShouldFilterByDevice() {
        InMemoryMonitorDataStore store = new InMemoryMonitorDataStore();
        store.registerPointThreshold(3001L, "PRESSURE", 9001L, new BigDecimal("0.1"), new BigDecimal("2.0"), 1);
        store.registerPointThreshold(3002L, "PRESSURE", 9002L, new BigDecimal("0.1"), new BigDecimal("2.0"), 1);
        MonitorIngestService ingestService = new MonitorIngestServiceImpl(store);
        MonitorQueryService queryService = new MonitorQueryServiceImpl(store);
        LocalDateTime now = LocalDateTime.now();
        ingestService.ingest(new MonitorIngestService.MeasureIngestCommand(
            3001L, "PRESSURE", new BigDecimal("1.10"), now, 1, "edge-a", "d1"));
        ingestService.ingest(new MonitorIngestService.MeasureIngestCommand(
            3002L, "PRESSURE", new BigDecimal("1.20"), now, 1, "edge-b", "d2"));

        MonitorQueryService.CurrentPageResult page = queryService.queryCurrent(new MonitorQueryService.CurrentQuery(
            null, 9001L, 1, 10));
        assertEquals(1, page.items().size());
        assertEquals(3001L, page.items().get(0).pointId());
    }
}
