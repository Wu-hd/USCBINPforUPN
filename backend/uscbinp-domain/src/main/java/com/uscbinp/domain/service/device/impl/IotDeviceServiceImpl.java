package com.uscbinp.domain.service.device.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.code.BizCodeService;
import com.uscbinp.domain.service.device.IotDeviceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IotDeviceServiceImpl implements IotDeviceService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BizCodeService bizCodeService;
    private final AtomicLong idSequence = new AtomicLong(9000L);
    private final Map<Long, DeviceState> devices = new ConcurrentHashMap<>();

    public IotDeviceServiceImpl(BizCodeService bizCodeService) {
        this.bizCodeService = bizCodeService;
    }

    @Override
    public DeviceItem create(DeviceUpsertCommand command) {
        Long id = idSequence.incrementAndGet();
        String regionCode = normalize(command.regionCode());
        String code = resolveCode(command.deviceCode(), regionCode, command.levelCode());
        ensureUniqueCode(code, null);
        DeviceState state = new DeviceState(
            id,
            code,
            defaultName(command.deviceName(), code),
            normalize(command.deviceType()),
            normalize(command.protocolType()),
            normalize(command.gatewayCode()),
            command.facilityId(),
            regionCode,
            command.onlineStatus() == null ? 0 : command.onlineStatus(),
            command.lastOnlineTime(),
            normalize(command.firmwareVersion())
        );
        devices.put(id, state);
        return toItem(state);
    }

    @Override
    public DeviceItem update(Long id, DeviceUpsertCommand command) {
        DeviceState existing = require(id);
        String regionCode = normalize(command.regionCode());
        String code = resolveCode(command.deviceCode(), regionCode, command.levelCode());
        ensureUniqueCode(code, id);
        DeviceState updated = new DeviceState(
            id,
            code,
            defaultName(command.deviceName(), existing.deviceName()),
            normalize(command.deviceType()),
            normalize(command.protocolType()),
            normalize(command.gatewayCode()),
            command.facilityId(),
            regionCode,
            command.onlineStatus() == null ? existing.onlineStatus() : command.onlineStatus(),
            command.lastOnlineTime() == null ? existing.lastOnlineTime() : command.lastOnlineTime(),
            normalize(command.firmwareVersion())
        );
        devices.put(id, updated);
        return toItem(updated);
    }

    @Override
    public void delete(Long id) {
        require(id);
        devices.remove(id);
    }

    @Override
    public DeviceItem get(Long id) {
        return toItem(require(id));
    }

    @Override
    public PageResult list(int pageNum, int pageSize, String regionCode, boolean fullAccess) {
        int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        List<DeviceState> visible = devices.values().stream()
            .sorted(Comparator.comparing(DeviceState::id))
            .filter(item -> fullAccess || canAccessRegion(item.regionCode(), regionCode))
            .toList();
        int from = Math.min((resolvedPageNum - 1) * resolvedPageSize, visible.size());
        int to = Math.min(from + resolvedPageSize, visible.size());
        List<DeviceItem> list = visible.subList(from, to).stream().map(this::toItem).toList();
        return new PageResult(new PageInfo(resolvedPageNum, resolvedPageSize, visible.size()), list);
    }

    @Override
    public boolean exists(Long id) {
        return id != null && devices.containsKey(id);
    }

    private String resolveCode(String inputCode, String regionCode, String levelCode) {
        if (StringUtils.hasText(inputCode)) {
            return inputCode.trim();
        }
        return bizCodeService.generate(BizCodeService.BizCategory.DEV, regionCode, levelCode);
    }

    private String defaultName(String inputName, String fallback) {
        if (StringUtils.hasText(inputName)) {
            return inputName.trim();
        }
        return fallback;
    }

    private boolean canAccessRegion(String deviceRegionCode, String requestRegionCode) {
        if (!StringUtils.hasText(requestRegionCode)) {
            return false;
        }
        return requestRegionCode.trim().equals(deviceRegionCode);
    }

    private void ensureUniqueCode(String deviceCode, Long selfId) {
        boolean duplicated = devices.values().stream()
            .anyMatch(item -> item.deviceCode().equals(deviceCode) && !item.id().equals(selfId));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "device_code 已存在:" + deviceCode);
        }
    }

    private DeviceState require(Long id) {
        DeviceState state = devices.get(id);
        if (state == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "设备不存在:" + id);
        }
        return state;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private DeviceItem toItem(DeviceState state) {
        return new DeviceItem(
            state.id(),
            state.deviceCode(),
            state.deviceName(),
            state.deviceType(),
            state.protocolType(),
            state.gatewayCode(),
            state.facilityId(),
            state.regionCode(),
            state.onlineStatus(),
            state.lastOnlineTime(),
            state.firmwareVersion()
        );
    }

    private record DeviceState(Long id,
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
}
