package com.uscbinp.domain.service.asset;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.asset.impl.AssetNetworkServiceImpl;
import com.uscbinp.domain.service.asset.impl.AssetPipeSectionServiceImpl;
import com.uscbinp.domain.service.code.impl.InMemoryBizCodeService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetPipeSectionServiceTest {

    @Test
    void createPipeSectionShouldRejectMissingNetwork() {
        AssetNetworkService networkService = new AssetNetworkServiceImpl(new InMemoryBizCodeService());
        AssetPipeSectionService sectionService = new AssetPipeSectionServiceImpl(new InMemoryBizCodeService(), networkService);

        BusinessException ex = assertThrows(BusinessException.class, () ->
            sectionService.create(new AssetPipeSectionService.PipeSectionUpsertCommand(
                null,
                "示例管段",
                "PVC",
                new BigDecimal("600"),
                new BigDecimal("2.5"),
                999L,
                "3301",
                1,
                "L1"
            )));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }
}
