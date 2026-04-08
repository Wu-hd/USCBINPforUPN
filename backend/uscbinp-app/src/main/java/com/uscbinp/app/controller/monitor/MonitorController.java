package com.uscbinp.app.controller.monitor;

import com.uscbinp.app.controller.monitor.dto.MonitorIngestRequest;
import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.monitor.MonitorIngestService;
import com.uscbinp.domain.service.monitor.MonitorQueryService;
import com.uscbinp.infra.monitor.MonitorBroadcastPublisher;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorIngestService monitorIngestService;
    private final MonitorQueryService monitorQueryService;
    private final MonitorBroadcastPublisher monitorBroadcastPublisher;

    public MonitorController(
        MonitorIngestService monitorIngestService,
        MonitorQueryService monitorQueryService,
        MonitorBroadcastPublisher monitorBroadcastPublisher
    ) {
        this.monitorIngestService = monitorIngestService;
        this.monitorQueryService = monitorQueryService;
        this.monitorBroadcastPublisher = monitorBroadcastPublisher;
    }

    @PostMapping("/ingest")
    public ApiResponse<MonitorIngestService.IngestResult> ingest(@Valid @RequestBody MonitorIngestRequest request) {
        MonitorIngestService.IngestResult result = monitorIngestService.ingest(new MonitorIngestService.MeasureIngestCommand(
            request.pointId(),
            request.metricType(),
            request.metricValue(),
            request.collectTime(),
            request.qualityFlag(),
            request.edgeNodeCode(),
            request.traceId()
        ));
        monitorBroadcastPublisher.publish(new MonitorBroadcastPublisher.MonitorRealtimeMessage(
            request.pointId(),
            request.metricType(),
            result.currentValue(),
            request.collectTime(),
            request.qualityFlag(),
            result.alarmFlag(),
            request.traceId(),
            result.alertCode()
        ));
        return ApiResponse.ok(result);
    }

    @GetMapping("/current")
    public ApiResponse<MonitorQueryService.CurrentPageResult> current(
        @RequestParam(required = false) Long pointId,
        @RequestParam(required = false) Long deviceId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ApiResponse.ok(monitorQueryService.queryCurrent(new MonitorQueryService.CurrentQuery(
            pointId,
            deviceId,
            pageNum,
            pageSize
        )));
    }

    @GetMapping("/history")
    public ApiResponse<MonitorQueryService.HistoryResult> history(
        @RequestParam Long pointId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.ok(monitorQueryService.queryHistory(new MonitorQueryService.HistoryQuery(
            pointId,
            startTime,
            endTime,
            limit
        )));
    }
}
