package com.uscbinp.app.controller;

import com.uscbinp.app.controller.dto.DemoRequest;
import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @PostMapping("/validate")
    public ApiResponse<String> validate(@Valid @RequestBody DemoRequest request) {
        return ApiResponse.ok(request.name());
    }

    @GetMapping("/business-error")
    public ApiResponse<Void> businessError() {
        throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), ErrorCode.BUSINESS_ERROR.getMessage());
    }
}
