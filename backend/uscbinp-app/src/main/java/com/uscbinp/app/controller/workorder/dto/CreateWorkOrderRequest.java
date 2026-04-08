package com.uscbinp.app.controller.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateWorkOrderRequest(
    @NotBlank(message = "sourceType不能为空")
    String sourceType,
    @NotNull(message = "sourceId不能为空")
    Long sourceId,
    @NotBlank(message = "title不能为空")
    String title,
    String targetType,
    Long targetId,
    String regionCode,
    Long assigneeUserId,
    LocalDateTime expectFinishTime
) {
}
