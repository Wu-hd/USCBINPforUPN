package com.uscbinp.domain.service.system;

import java.util.List;

public interface SysRoleService {

    RolePageResult listRoles(int pageNum, int pageSize);

    RoleItem getRole(Long roleId);

    RoleItem createRole(RoleUpsertCommand command);

    RoleItem updateRole(Long roleId, RoleUpsertCommand command);

    void deleteRole(Long roleId);

    record RolePageResult(PageInfo page, List<RoleItem> list) {
    }

    record PageInfo(int pageNum, int pageSize, long total) {
    }

    record RoleItem(Long id, String roleCode, String roleName, Integer roleStatus) {
    }

    record RoleUpsertCommand(String roleCode, String roleName, Integer roleStatus) {
    }
}
