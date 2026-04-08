package com.uscbinp.domain.service.code.impl;

import com.uscbinp.domain.service.code.BizCodeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryBizCodeService implements BizCodeService {

    private final Map<String, AtomicLong> sequences = new ConcurrentHashMap<>();

    @Override
    public String generate(BizCategory category, String regionCode, String levelCode) {
        String resolvedRegionCode = StringUtils.hasText(regionCode) ? regionCode.trim() : DEFAULT_REGION_CODE;
        String resolvedLevelCode = StringUtils.hasText(levelCode) ? levelCode.trim() : DEFAULT_LEVEL_CODE;
        String key = resolvedRegionCode + "-" + category.name() + "-" + resolvedLevelCode;
        long sequence = sequences.computeIfAbsent(key, ignored -> new AtomicLong(0L)).incrementAndGet();
        return "%s-%s-%s-%06d".formatted(resolvedRegionCode, category.name(), resolvedLevelCode, sequence);
    }
}
