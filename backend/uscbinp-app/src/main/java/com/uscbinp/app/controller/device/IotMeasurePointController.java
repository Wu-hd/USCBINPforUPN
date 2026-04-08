package com.uscbinp.app.controller.device;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.device.IotMeasurePointService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device/measure-points")
public class IotMeasurePointController {

    private final IotMeasurePointService iotMeasurePointService;

    public IotMeasurePointController(IotMeasurePointService iotMeasurePointService) {
        this.iotMeasurePointService = iotMeasurePointService;
    }

    @PostMapping
    public ApiResponse<IotMeasurePointService.MeasurePointItem> create(@Valid @RequestBody MeasurePointSaveRequest request) {
        return ApiResponse.ok(iotMeasurePointService.create(toCommand(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<IotMeasurePointService.MeasurePointItem> update(@PathVariable Long id,
                                                                       @Valid @RequestBody MeasurePointSaveRequest request) {
        return ApiResponse.ok(iotMeasurePointService.update(id, toCommand(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<IotMeasurePointService.MeasurePointItem> get(@PathVariable Long id) {
        return ApiResponse.ok(iotMeasurePointService.get(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        iotMeasurePointService.delete(id);
        return ApiResponse.ok((Void) null);
    }

    @GetMapping
    public ApiResponse<IotMeasurePointService.PageResult> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                                               @RequestParam(required = false) Long deviceId,
                                                               @RequestParam(required = false) String regionCode,
                                                               Authentication authentication) {
        String resolvedRegionCode = resolveRegionCode(authentication, regionCode);
        requireReadableRegion(authentication, resolvedRegionCode);
        return ApiResponse.ok(iotMeasurePointService.list(
            pageNum,
            pageSize,
            deviceId,
            resolvedRegionCode,
            isAdmin(authentication)
        ));
    }

    private IotMeasurePointService.MeasurePointUpsertCommand toCommand(MeasurePointSaveRequest request) {
        return new IotMeasurePointService.MeasurePointUpsertCommand(
            request.pointCode(),
            request.pointName(),
            request.deviceId(),
            request.metricType(),
            request.unitName(),
            request.sampleCycleSec(),
            request.sectionId(),
            request.pointStatus(),
            request.regionCode(),
            request.levelCode()
        );
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && "admin".equalsIgnoreCase(authentication.getName());
    }

    private String resolveRegionCode(Authentication authentication, String requestRegionCode) {
        if (isAdmin(authentication)) {
            return requestRegionCode;
        }
        if (authentication != null && "demo".equalsIgnoreCase(authentication.getName())) {
            return "3302";
        }
        return requestRegionCode;
    }

    private void requireReadableRegion(Authentication authentication, String resolvedRegionCode) {
        if (!isAdmin(authentication) && !StringUtils.hasText(resolvedRegionCode)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN.getCode(), ErrorCode.AUTH_FORBIDDEN.getMessage());
        }
    }

    private record MeasurePointSaveRequest(String pointCode,
                                           @NotBlank String pointName,
                                           @NotNull Long deviceId,
                                           @NotBlank String metricType,
                                           String unitName,
                                           Integer sampleCycleSec,
                                           Long sectionId,
                                           Integer pointStatus,
                                           String regionCode,
                                           String levelCode) {
    }
}
