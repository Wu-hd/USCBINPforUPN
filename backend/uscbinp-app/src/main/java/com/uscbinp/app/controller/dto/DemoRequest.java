package com.uscbinp.app.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record DemoRequest(
    @NotBlank(message = "name不能为空")
    String name
) {
}
