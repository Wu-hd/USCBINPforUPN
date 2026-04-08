package com.uscbinp.app.controller.monitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonitorIngestRequest(
    @NotNull(message = "pointId不能为空")
    Long pointId,
    @NotBlank(message = "metricType不能为空")
    String metricType,
    @NotNull(message = "metricValue不能为空")
    BigDecimal metricValue,
    LocalDateTime collectTime,
    Integer qualityFlag,
    String edgeNodeCode,
    String traceId
) {
}
