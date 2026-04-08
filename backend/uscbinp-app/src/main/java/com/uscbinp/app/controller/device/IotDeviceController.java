package com.uscbinp.app.controller.device;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.device.IotDeviceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/device/devices")
public class IotDeviceController {

    private final IotDeviceService iotDeviceService;

    public IotDeviceController(IotDeviceService iotDeviceService) {
        this.iotDeviceService = iotDeviceService;
    }

    @PostMapping
    public ApiResponse<IotDeviceService.DeviceItem> create(@Valid @RequestBody DeviceSaveRequest request) {
        return ApiResponse.ok(iotDeviceService.create(toCommand(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<IotDeviceService.DeviceItem> update(@PathVariable Long id,
                                                           @Valid @RequestBody DeviceSaveRequest request) {
        return ApiResponse.ok(iotDeviceService.update(id, toCommand(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<IotDeviceService.DeviceItem> get(@PathVariable Long id) {
        return ApiResponse.ok(iotDeviceService.get(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        iotDeviceService.delete(id);
        return ApiResponse.ok((Void) null);
    }

    @GetMapping
    public ApiResponse<IotDeviceService.PageResult> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false) String regionCode,
                                                         Authentication authentication) {
        String resolvedRegionCode = resolveRegionCode(authentication, regionCode);
        requireReadableRegion(authentication, resolvedRegionCode);
        return ApiResponse.ok(iotDeviceService.list(
            pageNum,
            pageSize,
            resolvedRegionCode,
            isAdmin(authentication)
        ));
    }

    private IotDeviceService.DeviceUpsertCommand toCommand(DeviceSaveRequest request) {
        return new IotDeviceService.DeviceUpsertCommand(
            request.deviceCode(),
            request.deviceName(),
            request.deviceType(),
            request.protocolType(),
            request.gatewayCode(),
            request.facilityId(),
            request.regionCode(),
            request.onlineStatus(),
            request.lastOnlineTime(),
            request.firmwareVersion(),
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

    private record DeviceSaveRequest(String deviceCode,
                                     @NotBlank String deviceName,
                                     String deviceType,
                                     String protocolType,
                                     String gatewayCode,
                                     Long facilityId,
                                     String regionCode,
                                     Integer onlineStatus,
                                     LocalDateTime lastOnlineTime,
                                     String firmwareVersion,
                                     String levelCode) {
    }
}
