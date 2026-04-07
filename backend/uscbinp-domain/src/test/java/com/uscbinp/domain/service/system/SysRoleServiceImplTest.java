package com.uscbinp.domain.service.system;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SysRoleServiceImplTest {

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
