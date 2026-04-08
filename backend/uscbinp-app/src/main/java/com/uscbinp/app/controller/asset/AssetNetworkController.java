package com.uscbinp.app.controller.asset;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.asset.AssetNetworkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/asset/networks")
public class AssetNetworkController {

    private final AssetNetworkService assetNetworkService;

    public AssetNetworkController(AssetNetworkService assetNetworkService) {
        this.assetNetworkService = assetNetworkService;
    }

    @PostMapping
    public ApiResponse<AssetNetworkService.NetworkItem> create(@Valid @RequestBody NetworkSaveRequest request) {
        return ApiResponse.ok(assetNetworkService.create(toCommand(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetNetworkService.NetworkItem> update(@PathVariable Long id,
                                                               @Valid @RequestBody NetworkSaveRequest request) {
        return ApiResponse.ok(assetNetworkService.update(id, toCommand(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetNetworkService.NetworkItem> get(@PathVariable Long id) {
        return ApiResponse.ok(assetNetworkService.get(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assetNetworkService.delete(id);
        return ApiResponse.ok((Void) null);
    }

    @GetMapping
    public ApiResponse<AssetNetworkService.PageResult> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                                            @RequestParam(required = false) String regionCode,
                                                            Authentication authentication) {
        return ApiResponse.ok(assetNetworkService.list(
            pageNum,
            pageSize,
            resolveRegionCode(authentication, regionCode),
            isAdmin(authentication)
        ));
    }

    private AssetNetworkService.NetworkUpsertCommand toCommand(NetworkSaveRequest request) {
        return new AssetNetworkService.NetworkUpsertCommand(
            request.networkCode(),
            request.networkName(),
            request.networkType(),
            request.regionCode(),
            request.serviceStatus(),
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

    private record NetworkSaveRequest(String networkCode,
                                      @NotBlank String networkName,
                                      String networkType,
                                      String regionCode,
                                      Integer serviceStatus,
                                      String levelCode) {
    }
}
