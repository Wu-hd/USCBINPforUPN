package com.uscbinp.domain.service.system;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysRoleServiceImplTest {

    @Test
    void listRolesShouldBeSynchronizedForConsistencyWithMutations() throws Exception {
        Method listRoles = SysRoleServiceImpl.class.getMethod("listRoles", int.class, int.class);

        assertTrue(Modifier.isSynchronized(listRoles.getModifiers()));
    }

    @Test
    void getRoleShouldBeSynchronizedForConsistencyWithMutations() throws Exception {
        Method getRole = SysRoleServiceImpl.class.getMethod("getRole", Long.class);

        assertTrue(Modifier.isSynchronized(getRole.getModifiers()));
    }

    @Test
    void deleteRoleShouldBeSynchronizedForConsistencyWithMutations() throws Exception {
        Method deleteRole = SysRoleServiceImpl.class.getMethod("deleteRole", Long.class);

        assertTrue(Modifier.isSynchronized(deleteRole.getModifiers()));
    }

    @Test
    void createRoleShouldRejectDuplicateRoleCode() {
        SysRoleService service = new SysRoleServiceImpl();

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.createRole(new SysRoleService.RoleUpsertCommand("sys_admin", "重复角色", 1)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void updateRoleShouldRejectDuplicateRoleCode() {
        SysRoleService service = new SysRoleServiceImpl();

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.updateRole(2L, new SysRoleService.RoleUpsertCommand("sys_admin", "运维用户", 1)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }
}
