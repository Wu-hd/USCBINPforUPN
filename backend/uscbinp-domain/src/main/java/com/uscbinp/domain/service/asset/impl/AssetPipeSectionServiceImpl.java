package com.uscbinp.domain.service.asset.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.asset.AssetNetworkService;
import com.uscbinp.domain.service.asset.AssetPipeSectionService;
import com.uscbinp.domain.service.code.BizCodeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AssetPipeSectionServiceImpl implements AssetPipeSectionService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BizCodeService bizCodeService;
    private final AssetNetworkService assetNetworkService;
    private final AtomicLong idSequence = new AtomicLong(5000L);
    private final Map<Long, PipeSectionState> sections = new ConcurrentHashMap<>();

    public AssetPipeSectionServiceImpl(BizCodeService bizCodeService, AssetNetworkService assetNetworkService) {
        this.bizCodeService = bizCodeService;
        this.assetNetworkService = assetNetworkService;
    }

    @Override
    public PipeSectionItem create(PipeSectionUpsertCommand command) {
        requireNetwork(command.networkId());
        Long id = idSequence.incrementAndGet();
        String regionCode = normalize(command.regionCode());
        String sectionCode = resolveCode(command.sectionCode(), regionCode, command.levelCode());
        ensureUniqueCode(sectionCode, null);
        PipeSectionState state = new PipeSectionState(
            id,
            sectionCode,
            defaultName(command.sectionName(), sectionCode),
            normalize(command.pipeMaterial()),
            command.diameterMm(),
            command.buryDepthM(),
            command.networkId(),
            regionCode,
            command.renovationStatus()
        );
        sections.put(id, state);
        return toItem(state);
    }

    @Override
    public PipeSectionItem update(Long id, PipeSectionUpsertCommand command) {
        PipeSectionState existing = require(id);
        requireNetwork(command.networkId());
        String regionCode = normalize(command.regionCode());
        String sectionCode = resolveCode(command.sectionCode(), regionCode, command.levelCode());
        ensureUniqueCode(sectionCode, id);
        PipeSectionState updated = new PipeSectionState(
            id,
            sectionCode,
            defaultName(command.sectionName(), existing.sectionName()),
            normalize(command.pipeMaterial()),
            coalesce(command.diameterMm(), existing.diameterMm()),
            coalesce(command.buryDepthM(), existing.buryDepthM()),
            command.networkId(),
            regionCode,
            command.renovationStatus() == null ? existing.renovationStatus() : command.renovationStatus()
        );
        sections.put(id, updated);
        return toItem(updated);
    }

    @Override
    public void delete(Long id) {
        require(id);
        sections.remove(id);
    }

    @Override
    public PipeSectionItem get(Long id) {
        return toItem(require(id));
    }

    @Override
    public PageResult list(int pageNum, int pageSize, Long networkId, String regionCode, boolean fullAccess) {
        int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        List<PipeSectionState> visible = sections.values().stream()
            .sorted(Comparator.comparing(PipeSectionState::id))
            .filter(state -> networkId == null || state.networkId().equals(networkId))
            .filter(state -> fullAccess || canAccessRegion(state.regionCode(), regionCode))
            .toList();
        int from = Math.min((resolvedPageNum - 1) * resolvedPageSize, visible.size());
        int to = Math.min(from + resolvedPageSize, visible.size());
        List<PipeSectionItem> list = visible.subList(from, to).stream().map(this::toItem).toList();
        return new PageResult(new PageInfo(resolvedPageNum, resolvedPageSize, visible.size()), list);
    }

    private void requireNetwork(Long networkId) {
        if (networkId == null || !assetNetworkService.exists(networkId)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "network_id 不存在:" + networkId);
        }
    }

    private String resolveCode(String inputCode, String regionCode, String levelCode) {
        if (StringUtils.hasText(inputCode)) {
            return inputCode.trim();
        }
        return bizCodeService.generate(BizCodeService.BizCategory.SEC, regionCode, levelCode);
    }

    private String defaultName(String inputName, String fallback) {
        if (StringUtils.hasText(inputName)) {
            return inputName.trim();
        }
        return fallback;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BigDecimal coalesce(BigDecimal incoming, BigDecimal existing) {
        return incoming == null ? existing : incoming;
    }

    private PipeSectionState require(Long id) {
        PipeSectionState state = sections.get(id);
        if (state == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "管段不存在:" + id);
        }
        return state;
    }

    private boolean canAccessRegion(String sectionRegionCode, String requestRegionCode) {
        if (!StringUtils.hasText(requestRegionCode)) {
            return false;
        }
        return requestRegionCode.trim().equals(sectionRegionCode);
    }

    private void ensureUniqueCode(String sectionCode, Long selfId) {
        boolean duplicated = sections.values().stream()
            .anyMatch(item -> item.sectionCode().equals(sectionCode) && !Objects.equals(item.id(), selfId));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "section_code 已存在:" + sectionCode);
        }
    }

    private PipeSectionItem toItem(PipeSectionState state) {
        return new PipeSectionItem(
            state.id(),
            state.sectionCode(),
            state.sectionName(),
            state.pipeMaterial(),
            state.diameterMm(),
            state.buryDepthM(),
            state.networkId(),
            state.regionCode(),
            state.renovationStatus()
        );
    }

    private record PipeSectionState(Long id,
                                    String sectionCode,
                                    String sectionName,
                                    String pipeMaterial,
                                    BigDecimal diameterMm,
                                    BigDecimal buryDepthM,
                                    Long networkId,
                                    String regionCode,
                                    Integer renovationStatus) {
    }
}
