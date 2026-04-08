package com.uscbinp.domain.service.system.permission;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleRegionDataPermissionEvaluatorTest {

    private final DataPermissionEvaluator evaluator = new RoleRegionDataPermissionEvaluator();

    @Test
    void adminRoleShouldGetFullAccessScope() {
        DataPermissionEvaluator.DataPermissionScope scope = evaluator.evaluate(
            new DataPermissionEvaluator.UserPermissionProfile(1L, "ops", "3301", List.of("ADMIN")));

        assertTrue(scope.fullAccess());
        assertEquals("3301", scope.regionCode());
    }

    @Test
    void sysAdminRoleShouldGetFullAccessScope() {
        DataPermissionEvaluator.DataPermissionScope scope = evaluator.evaluate(
            new DataPermissionEvaluator.UserPermissionProfile(1L, "ops", "3301", List.of("sys_admin")));

        assertTrue(scope.fullAccess());
        assertEquals("3301", scope.regionCode());
    }

    @Test
    void nonAdminShouldOnlyGetRegionScope() {
        DataPermissionEvaluator.DataPermissionScope scope = evaluator.evaluate(
            new DataPermissionEvaluator.UserPermissionProfile(2L, "demo", "3302", List.of("monitor")));

        assertFalse(scope.fullAccess());
        assertEquals("3302", scope.regionCode());
    }
}
