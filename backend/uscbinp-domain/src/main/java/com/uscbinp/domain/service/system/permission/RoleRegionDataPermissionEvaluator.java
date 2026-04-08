package com.uscbinp.domain.service.system.permission;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Component
public class RoleRegionDataPermissionEvaluator implements DataPermissionEvaluator {

    private static final List<String> ADMIN_ROLE_CODES = List.of("ADMIN", "SYS_ADMIN");

    @Override
    public DataPermissionScope evaluate(UserPermissionProfile profile) {
        if (profile == null) {
            return new DataPermissionScope(false, null);
        }
        boolean adminByRole = profile.roleCodes().stream()
            .filter(StringUtils::hasText)
            .map(code -> code.trim().toUpperCase(Locale.ROOT))
            .anyMatch(ADMIN_ROLE_CODES::contains);
        boolean adminByUsername = "admin".equalsIgnoreCase(profile.username());
        return new DataPermissionScope(adminByRole || adminByUsername, normalizeRegionCode(profile.regionCode()));
    }

    private String normalizeRegionCode(String regionCode) {
        if (!StringUtils.hasText(regionCode)) {
            return null;
        }
        return regionCode.trim();
    }
}
