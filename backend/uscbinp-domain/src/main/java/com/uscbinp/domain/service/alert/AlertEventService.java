package com.uscbinp.domain.service.alert;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertEventService {

    AlertView confirm(Long alertId, Long operatorUserId);

    AlertView close(Long alertId, Long operatorUserId);

    PageResult pageQuery(PageQuery query);

    record PageQuery(
        Integer alertStatus,
        Integer alertLevel,
        String regionCode,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int pageNum,
        int pageSize
    ) {
    }

    record AlertView(
        Long id,
        String alertCode,
        Integer alertStatus,
        Integer alertLevel,
        LocalDateTime occurTime,
        LocalDateTime confirmTime,
        LocalDateTime closeTime,
        Long workOrderId
    ) {
    }

    record PageResult(
        List<AlertView> items,
        long total,
        int pageNum,
        int pageSize
    ) {
    }
}
