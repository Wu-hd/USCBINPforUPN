package com.uscbinp.domain.service.device.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.code.BizCodeService;
import com.uscbinp.domain.service.device.IotDeviceService;
import com.uscbinp.domain.service.device.IotMeasurePointService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IotMeasurePointServiceImpl implements IotMeasurePointService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BizCodeService bizCodeService;
    private final IotDeviceService iotDeviceService;
    private final AtomicLong idSequence = new AtomicLong(12000L);
    private final Map<Long, MeasurePointState> points = new ConcurrentHashMap<>();

    public IotMeasurePointServiceImpl(BizCodeService bizCodeService, IotDeviceService iotDeviceService) {
        this.bizCodeService = bizCodeService;
        this.iotDeviceService = iotDeviceService;
    }

    @Override
    public MeasurePointItem create(MeasurePointUpsertCommand command) {
        requireDevice(command.deviceId());
        Long id = idSequence.incrementAndGet();
        String regionCode = normalize(command.regionCode());
        String pointCode = resolveCode(command.pointCode(), regionCode, command.levelCode());
        ensureUniqueCode(pointCode, null);
        MeasurePointState state = new MeasurePointState(
            id,
            pointCode,
            defaultName(command.pointName(), pointCode),
            command.deviceId(),
            normalize(command.metricType()),
            normalize(command.unitName()),
            command.sampleCycleSec(),
            command.sectionId(),
            command.pointStatus() == null ? 1 : command.pointStatus(),
            regionCode
        );
        points.put(id, state);
        return toItem(state);
    }

    @Override
    public MeasurePointItem update(Long id, MeasurePointUpsertCommand command) {
        MeasurePointState existing = require(id);
        requireDevice(command.deviceId());
        String regionCode = normalize(command.regionCode());
        String pointCode = resolveCode(command.pointCode(), regionCode, command.levelCode());
        ensureUniqueCode(pointCode, id);
        MeasurePointState updated = new MeasurePointState(
            id,
            pointCode,
            defaultName(command.pointName(), existing.pointName()),
            command.deviceId(),
            normalize(command.metricType()),
            normalize(command.unitName()),
            command.sampleCycleSec() == null ? existing.sampleCycleSec() : command.sampleCycleSec(),
            command.sectionId(),
            command.pointStatus() == null ? existing.pointStatus() : command.pointStatus(),
            regionCode
        );
        points.put(id, updated);
        return toItem(updated);
    }

    @Override
    public void delete(Long id) {
        require(id);
        points.remove(id);
    }

    @Override
    public MeasurePointItem get(Long id) {
        return toItem(require(id));
    }

    @Override
    public PageResult list(int pageNum, int pageSize, Long deviceId, String regionCode, boolean fullAccess) {
        int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        List<MeasurePointState> visible = points.values().stream()
            .sorted(Comparator.comparing(MeasurePointState::id))
            .filter(item -> deviceId == null || item.deviceId().equals(deviceId))
            .filter(item -> fullAccess || canAccessRegion(item.regionCode(), regionCode))
            .toList();
        int from = Math.min((resolvedPageNum - 1) * resolvedPageSize, visible.size());
        int to = Math.min(from + resolvedPageSize, visible.size());
        List<MeasurePointItem> list = visible.subList(from, to).stream().map(this::toItem).toList();
        return new PageResult(new PageInfo(resolvedPageNum, resolvedPageSize, visible.size()), list);
    }

    private void requireDevice(Long deviceId) {
        if (deviceId == null || !iotDeviceService.exists(deviceId)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "device_id 不存在:" + deviceId);
        }
    }

    private String resolveCode(String inputCode, String regionCode, String levelCode) {
        if (StringUtils.hasText(inputCode)) {
            return inputCode.trim();
        }
        return bizCodeService.generate(BizCodeService.BizCategory.MPT, regionCode, levelCode);
    }

    private String defaultName(String inputName, String fallback) {
        if (StringUtils.hasText(inputName)) {
            return inputName.trim();
        }
        return fallback;
    }

    private boolean canAccessRegion(String pointRegionCode, String requestRegionCode) {
        if (!StringUtils.hasText(requestRegionCode)) {
            return false;
        }
        return requestRegionCode.trim().equals(pointRegionCode);
    }

    private void ensureUniqueCode(String pointCode, Long selfId) {
        boolean duplicated = points.values().stream()
            .anyMatch(item -> item.pointCode().equals(pointCode) && !Objects.equals(item.id(), selfId));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "point_code 已存在:" + pointCode);
        }
    }

    private MeasurePointState require(Long id) {
        MeasurePointState state = points.get(id);
        if (state == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "测点不存在:" + id);
        }
        return state;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private MeasurePointItem toItem(MeasurePointState state) {
        return new MeasurePointItem(
            state.id(),
            state.pointCode(),
            state.pointName(),
            state.deviceId(),
            state.metricType(),
            state.unitName(),
            state.sampleCycleSec(),
            state.sectionId(),
            state.pointStatus(),
            state.regionCode()
        );
    }

    private record MeasurePointState(Long id,
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
}
