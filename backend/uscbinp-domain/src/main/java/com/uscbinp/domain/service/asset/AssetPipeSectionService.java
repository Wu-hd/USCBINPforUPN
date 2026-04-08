package com.uscbinp.domain.service.asset;

import java.math.BigDecimal;
import java.util.List;

public interface AssetPipeSectionService {

    PipeSectionItem create(PipeSectionUpsertCommand command);

    PipeSectionItem update(Long id, PipeSectionUpsertCommand command);

    void delete(Long id);

    PipeSectionItem get(Long id);

    PageResult list(int pageNum, int pageSize, Long networkId, String regionCode, boolean fullAccess);

    record PipeSectionUpsertCommand(String sectionCode,
                                    String sectionName,
                                    String pipeMaterial,
                                    BigDecimal diameterMm,
                                    BigDecimal buryDepthM,
                                    Long networkId,
                                    String regionCode,
                                    Integer renovationStatus,
                                    String levelCode) {
    }

    record PipeSectionItem(Long id,
                           String sectionCode,
                           String sectionName,
                           String pipeMaterial,
                           BigDecimal diameterMm,
                           BigDecimal buryDepthM,
                           Long networkId,
                           String regionCode,
                           Integer renovationStatus) {
    }

    record PageInfo(int pageNum, int pageSize, long total) {
    }

    record PageResult(PageInfo page, List<PipeSectionItem> list) {
    }
}
