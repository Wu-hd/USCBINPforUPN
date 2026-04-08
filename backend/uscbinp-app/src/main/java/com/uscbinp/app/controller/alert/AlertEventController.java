package com.uscbinp.app.controller.alert;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.alert.AlertEventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/alerts/events")
public class AlertEventController {

    private final AlertEventService alertEventService;

    public AlertEventController(AlertEventService alertEventService) {
        this.alertEventService = alertEventService;
    }

    @GetMapping
    public ApiResponse<AlertEventService.PageResult> page(
        @RequestParam(required = false) Integer alertStatus,
        @RequestParam(required = false) Integer alertLevel,
        @RequestParam(required = false) String regionCode,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ApiResponse.ok(alertEventService.pageQuery(new AlertEventService.PageQuery(
            alertStatus,
            alertLevel,
            regionCode,
            startTime,
            endTime,
            pageNum,
            pageSize
        )));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<AlertEventService.AlertView> confirm(@PathVariable Long id) {
        return ApiResponse.ok(alertEventService.confirm(id, 0L));
    }

    @PutMapping("/{id}/close")
    public ApiResponse<AlertEventService.AlertView> close(@PathVariable Long id) {
        return ApiResponse.ok(alertEventService.close(id, 0L));
    }
}
