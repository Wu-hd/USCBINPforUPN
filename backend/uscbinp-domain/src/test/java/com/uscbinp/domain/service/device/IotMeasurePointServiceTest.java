package com.uscbinp.domain.service.device;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.code.impl.InMemoryBizCodeService;
import com.uscbinp.domain.service.device.impl.IotDeviceServiceImpl;
import com.uscbinp.domain.service.device.impl.IotMeasurePointServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IotMeasurePointServiceTest {

    @Test
    void createPointShouldRejectMissingDevice() {
        IotDeviceService deviceService = new IotDeviceServiceImpl(new InMemoryBizCodeService());
        IotMeasurePointService pointService = new IotMeasurePointServiceImpl(new InMemoryBizCodeService(), deviceService);

        BusinessException ex = assertThrows(BusinessException.class, () ->
            pointService.create(new IotMeasurePointService.MeasurePointUpsertCommand(
                null,
                "压力测点",
                999L,
                "PRESSURE",
                "kPa",
                null,
                null,
                1,
                "3301",
                "L1"
            )));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }
}
