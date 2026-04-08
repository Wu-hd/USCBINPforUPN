package com.uscbinp.domain.service.device;

import java.time.LocalDateTime;
import java.util.List;

public interface IotDeviceService {

    DeviceItem create(DeviceUpsertCommand command);

    DeviceItem update(Long id, DeviceUpsertCommand command);

    void delete(Long id);

    DeviceItem get(Long id);

    PageResult list(int pageNum, int pageSize, String regionCode, boolean fullAccess);

    boolean exists(Long id);

    record DeviceUpsertCommand(String deviceCode,
                               String deviceName,
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

    record DeviceItem(Long id,
                      String deviceCode,
                      String deviceName,
                      String deviceType,
                      String protocolType,
                      String gatewayCode,
                      Long facilityId,
                      String regionCode,
                      Integer onlineStatus,
                      LocalDateTime lastOnlineTime,
                      String firmwareVersion) {
    }

    record PageInfo(int pageNum, int pageSize, long total) {
    }

    record PageResult(PageInfo page, List<DeviceItem> list) {
    }
}
