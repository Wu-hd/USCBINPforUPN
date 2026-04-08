package com.uscbinp.app.controller.asset;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.asset.AssetPipeSectionService;
import jakarta.validation.Valid;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/asset/pipe-sections")
public class AssetPipeSectionController {

    private final AssetPipeSectionService assetPipeSectionService;

    public AssetPipeSectionController(AssetPipeSectionService assetPipeSectionService) {
        this.assetPipeSectionService = assetPipeSectionService;
    }

    @PostMapping
    public ApiResponse<AssetPipeSectionService.PipeSectionItem> create(@Valid @RequestBody PipeSectionSaveRequest request) {
        return ApiResponse.ok(assetPipeSectionService.create(toCommand(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetPipeSectionService.PipeSectionItem> update(@PathVariable Long id,
                                                                       @Valid @RequestBody PipeSectionSaveRequest request) {
        return ApiResponse.ok(assetPipeSectionService.update(id, toCommand(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetPipeSectionService.PipeSectionItem> get(@PathVariable Long id) {
        return ApiResponse.ok(assetPipeSectionService.get(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assetPipeSectionService.delete(id);
        return ApiResponse.ok((Void) null);
    }

    @GetMapping
    public ApiResponse<AssetPipeSectionService.PageResult> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                                @RequestParam(required = false) Long networkId,
                                                                @RequestParam(required = false) String regionCode,
                                                                Authentication authentication) {
        String resolvedRegionCode = resolveRegionCode(authentication, regionCode);
        requireReadableRegion(authentication, resolvedRegionCode);
        return ApiResponse.ok(assetPipeSectionService.list(
            pageNum,
            pageSize,
            networkId,
            resolvedRegionCode,
            isAdmin(authentication)
        ));
    }

    private AssetPipeSectionService.PipeSectionUpsertCommand toCommand(PipeSectionSaveRequest request) {
        return new AssetPipeSectionService.PipeSectionUpsertCommand(
            request.sectionCode(),
            request.sectionName(),
            request.pipeMaterial(),
            request.diameterMm(),
            request.buryDepthM(),
            request.networkId(),
            request.regionCode(),
            request.renovationStatus(),
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

    private record PipeSectionSaveRequest(String sectionCode,
                                          String sectionName,
                                          String pipeMaterial,
                                          BigDecimal diameterMm,
                                          BigDecimal buryDepthM,
                                          @NotNull Long networkId,
                                          String regionCode,
                                          Integer renovationStatus,
                                          String levelCode) {
    }
}
