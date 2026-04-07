package com.uscbinp.domain.service.system.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.SysRoleService;
import com.uscbinp.domain.service.system.SystemModelLock;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SystemModelLock systemModelLock;
    private final Map<Long, RoleState> roles = new ConcurrentHashMap<>();
    private final AtomicLong roleIdSequence = new AtomicLong(100L);

    public SysRoleServiceImpl(SystemModelLock systemModelLock) {
        this.systemModelLock = systemModelLock;
        roles.put(1L, new RoleState(1L, "sys_admin", "系统管理员", 1));
        roles.put(2L, new RoleState(2L, "ops_user", "运维用户", 1));
    }

    @Override
    public RolePageResult listRoles(int pageNum, int pageSize) {
        return systemModelLock.withReadLock(() -> {
            int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
            int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
            List<RoleState> orderedRoles = roles.values()
                .stream()
                .sorted(Comparator.comparing(RoleState::id))
                .toList();
            int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, orderedRoles.size());
            int toIndex = Math.min(fromIndex + resolvedPageSize, orderedRoles.size());
            List<RoleItem> list = orderedRoles.subList(fromIndex, toIndex)
                .stream()
                .map(this::toItem)
                .toList();
            return new RolePageResult(new PageInfo(resolvedPageNum, resolvedPageSize, orderedRoles.size()), list);
        });
    }

    @Override
    public RoleItem getRole(Long roleId) {
        return systemModelLock.withReadLock(() -> toItem(requireRole(roleId)));
    }

    @Override
    public RoleItem createRole(RoleUpsertCommand command) {
        return systemModelLock.withWriteLock(() -> {
            Long roleId = roleIdSequence.incrementAndGet();
            String roleCode = resolveRoleCode(command.roleCode(), roleId);
            ensureRoleCodeUnique(roleCode, null);
            RoleState role = new RoleState(
                roleId,
                roleCode,
                resolveRoleName(command.roleName(), roleId),
                resolveRoleStatus(command.roleStatus())
            );
            roles.put(roleId, role);
            return toItem(role);
        });
    }

    @Override
    public RoleItem updateRole(Long roleId, RoleUpsertCommand command) {
        return systemModelLock.withWriteLock(() -> {
            requireRole(roleId);
            String roleCode = resolveRoleCode(command.roleCode(), roleId);
            ensureRoleCodeUnique(roleCode, roleId);
            RoleState role = new RoleState(
                roleId,
                roleCode,
                resolveRoleName(command.roleName(), roleId),
                resolveRoleStatus(command.roleStatus())
            );
            roles.put(roleId, role);
            return toItem(role);
        });
    }

    @Override
    public void deleteRole(Long roleId) {
        systemModelLock.withWriteLock(() -> {
            requireRole(roleId);
            roles.remove(roleId);
        });
    }

    private RoleState requireRole(Long roleId) {
        RoleState role = roles.get(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "角色不存在:" + roleId);
        }
        return role;
    }

    private RoleItem toItem(RoleState role) {
        return new RoleItem(role.id(), role.roleCode(), role.roleName(), role.roleStatus());
    }

    private String resolveRoleCode(String roleCode, Long roleId) {
        if (StringUtils.hasText(roleCode)) {
            return roleCode.trim();
        }
        return "role_" + roleId;
    }

    private String resolveRoleName(String roleName, Long roleId) {
        if (StringUtils.hasText(roleName)) {
            return roleName.trim();
        }
        return "角色-" + roleId;
    }

    private Integer resolveRoleStatus(Integer roleStatus) {
        return roleStatus == null ? 1 : roleStatus;
    }

    private void ensureRoleCodeUnique(String roleCode, Long currentRoleId) {
        boolean duplicated = roles.values()
            .stream()
            .anyMatch(role -> role.roleCode().equals(roleCode) && !role.id().equals(currentRoleId));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "角色编码已存在:" + roleCode);
        }
    }

    private record RoleState(Long id, String roleCode, String roleName, Integer roleStatus) {
    }
}
