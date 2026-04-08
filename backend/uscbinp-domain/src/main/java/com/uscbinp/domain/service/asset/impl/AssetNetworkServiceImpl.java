package com.uscbinp.domain.service.asset.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.asset.AssetNetworkService;
import com.uscbinp.domain.service.code.BizCodeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AssetNetworkServiceImpl implements AssetNetworkService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BizCodeService bizCodeService;
    private final AtomicLong idSequence = new AtomicLong(1000L);
    private final Map<Long, NetworkState> networks = new ConcurrentHashMap<>();

    public AssetNetworkServiceImpl(BizCodeService bizCodeService) {
        this.bizCodeService = bizCodeService;
    }

    @Override
    public NetworkItem create(NetworkUpsertCommand command) {
        Long id = idSequence.incrementAndGet();
        String regionCode = normalize(command.regionCode());
        String code = resolveCode(command.networkCode(), regionCode, command.levelCode());
        ensureUniqueCode(code, null);
        NetworkState state = new NetworkState(
            id,
            code,
            defaultName(command.networkName(), code),
            normalize(command.networkType()),
            regionCode,
            command.serviceStatus() == null ? 1 : command.serviceStatus()
        );
        networks.put(id, state);
        return toItem(state);
    }

    @Override
    public NetworkItem update(Long id, NetworkUpsertCommand command) {
        NetworkState existing = require(id);
        String regionCode = normalize(command.regionCode());
        String code = resolveCode(command.networkCode(), regionCode, command.levelCode());
        ensureUniqueCode(code, id);
        NetworkState updated = new NetworkState(
            id,
            code,
            defaultName(command.networkName(), existing.networkName()),
            normalize(command.networkType()),
            regionCode,
            command.serviceStatus() == null ? existing.serviceStatus() : command.serviceStatus()
        );
        networks.put(id, updated);
        return toItem(updated);
    }

    @Override
    public void delete(Long id) {
        require(id);
        networks.remove(id);
    }

    @Override
    public NetworkItem get(Long id) {
        return toItem(require(id));
    }

    @Override
    public PageResult list(int pageNum, int pageSize, String regionCode, boolean fullAccess) {
        int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        List<NetworkState> visible = networks.values().stream()
            .sorted(Comparator.comparing(NetworkState::id))
            .filter(state -> fullAccess || canAccessRegion(state.regionCode(), regionCode))
            .toList();
        int from = Math.min((resolvedPageNum - 1) * resolvedPageSize, visible.size());
        int to = Math.min(from + resolvedPageSize, visible.size());
        List<NetworkItem> list = visible.subList(from, to).stream().map(this::toItem).toList();
        return new PageResult(new PageInfo(resolvedPageNum, resolvedPageSize, visible.size()), list);
    }

    @Override
    public boolean exists(Long id) {
        return id != null && networks.containsKey(id);
    }

    private boolean canAccessRegion(String networkRegionCode, String requestRegionCode) {
        if (!StringUtils.hasText(requestRegionCode)) {
            return false;
        }
        return requestRegionCode.trim().equals(networkRegionCode);
    }

    private String resolveCode(String inputCode, String regionCode, String levelCode) {
        if (StringUtils.hasText(inputCode)) {
            return inputCode.trim();
        }
        return bizCodeService.generate(BizCodeService.BizCategory.NET, regionCode, levelCode);
    }

    private String defaultName(String inputName, String fallback) {
        if (StringUtils.hasText(inputName)) {
            return inputName.trim();
        }
        return fallback;
    }

    private void ensureUniqueCode(String networkCode, Long selfId) {
        boolean duplicated = networks.values().stream()
            .anyMatch(item -> item.networkCode().equals(networkCode) && !item.id().equals(selfId));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "network_code 已存在:" + networkCode);
        }
    }

    private NetworkState require(Long id) {
        NetworkState state = networks.get(id);
        if (state == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "管网不存在:" + id);
        }
        return state;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private NetworkItem toItem(NetworkState state) {
        return new NetworkItem(
            state.id(),
            state.networkCode(),
            state.networkName(),
            state.networkType(),
            state.regionCode(),
            state.serviceStatus()
        );
    }

    private record NetworkState(Long id,
                                String networkCode,
                                String networkName,
                                String networkType,
                                String regionCode,
                                Integer serviceStatus) {
    }
}
