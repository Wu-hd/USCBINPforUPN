package com.uscbinp.app.controller.workorder.dto;

import jakarta.validation.constraints.NotNull;

public record AssignWorkOrderRequest(
    @NotNull(message = "assigneeUserId不能为空")
    Long assigneeUserId
) {
}
