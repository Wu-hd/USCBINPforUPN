package com.uscbinp.domain.service.code;

public interface BizCodeService {

    String DEFAULT_REGION_CODE = "0000";
    String DEFAULT_LEVEL_CODE = "L1";

    String generate(BizCategory category, String regionCode, String levelCode);

    enum BizCategory {
        NET, SEC, DEV, MPT
    }
}
