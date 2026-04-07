package com.uscbinp.domain.service.system.permission;

import java.util.List;

public interface DataPermissionEvaluator {

    DataPermissionScope evaluate(UserPermissionProfile profile);

    record UserPermissionProfile(Long userId, String username, String regionCode, List<String> roleCodes) {
    }

    record DataPermissionScope(boolean fullAccess, String regionCode) {
    }
}
