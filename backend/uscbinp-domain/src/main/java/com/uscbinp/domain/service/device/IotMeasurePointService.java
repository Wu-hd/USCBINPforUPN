package com.uscbinp.domain.service.device;

import java.util.List;

public interface IotMeasurePointService {

    MeasurePointItem create(MeasurePointUpsertCommand command);

    MeasurePointItem update(Long id, MeasurePointUpsertCommand command);

    void delete(Long id);

    MeasurePointItem get(Long id);

    PageResult list(int pageNum, int pageSize, Long deviceId, String regionCode, boolean fullAccess);

    record MeasurePointUpsertCommand(String pointCode,
                                     String pointName,
                                     Long deviceId,
                                     String metricType,
                                     String unitName,
                                     Integer sampleCycleSec,
                                     Long sectionId,
                                     Integer pointStatus,
                                     String regionCode,
                                     String levelCode) {
    }

    record MeasurePointItem(Long id,
                            String pointCode,
                            String pointName,
                            Long deviceId,
                            String metricType,
                            String unitName,
                            Integer sampleCycleSec,
                            Long sectionId,
                            Integer pointStatus,
                            String regionCode) {
    }

    record PageInfo(int pageNum, int pageSize, long total) {
    }

    record PageResult(PageInfo page, List<MeasurePointItem> list) {
    }
}
