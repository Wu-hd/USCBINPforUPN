package com.uscbinp.domain.service.system.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.SysRoleService;
import com.uscbinp.domain.service.system.SysUserService;
import com.uscbinp.domain.service.system.SystemModelLock;
import com.uscbinp.domain.service.system.permission.DataPermissionEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SysUserServiceImpl implements SysUserService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SysRoleService sysRoleService;
    private final SystemModelLock systemModelLock;
    private final DataPermissionEvaluator dataPermissionEvaluator;
    private final Map<Long, UserState> users = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> userRoleBindings = new ConcurrentHashMap<>();
    private final AtomicLong userIdSequence = new AtomicLong(100L);

    public SysUserServiceImpl(SysRoleService sysRoleService,
                              SystemModelLock systemModelLock,
                              DataPermissionEvaluator dataPermissionEvaluator) {
        this.sysRoleService = sysRoleService;
        this.systemModelLock = systemModelLock;
        this.dataPermissionEvaluator = dataPermissionEvaluator;
        users.put(1L, new UserState(1L, "admin", "管理员", "13800000001", "admin@uscbinp.com", "3301", 1));
        users.put(2L, new UserState(2L, "demo", "演示用户", "13800000002", "demo@uscbinp.com", "3302", 1));
        userRoleBindings.put(1L, List.of(1L));
        userRoleBindings.put(2L, List.of(2L));
    }

    @Override
    public UserPageResult listUsers(int pageNum, int pageSize, DataPermissionContext permissionContext) {
        return systemModelLock.withWriteLock(() -> {
            int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
            int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
            UserState requester = resolveRequester(permissionContext);
            if (requester == null) {
                return new UserPageResult(new PageInfo(resolvedPageNum, resolvedPageSize, 0), List.of());
            }
            DataPermissionEvaluator.DataPermissionScope permissionScope = resolvePermissionScope(requester);
            List<UserState> visibleUsers = users.values()
                .stream()
                .sorted(Comparator.comparing(UserState::id))
                .filter(user -> canView(requester, user, permissionScope))
                .toList();
            int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, visibleUsers.size());
            int toIndex = Math.min(fromIndex + resolvedPageSize, visibleUsers.size());
            List<UserItem> list = visibleUsers.subList(fromIndex, toIndex)
                .stream()
                .map(this::toItem)
                .toList();
            return new UserPageResult(new PageInfo(resolvedPageNum, resolvedPageSize, visibleUsers.size()), list);
        });
    }

    @Override
    public UserItem getUser(Long userId) {
        return systemModelLock.withWriteLock(() -> toItem(requireUser(userId)));
    }

    @Override
    public UserItem createUser(UserUpsertCommand command) {
        return systemModelLock.withWriteLock(() -> {
            Long userId = userIdSequence.incrementAndGet();
            String username = resolveUsername(command.username(), userId);
            ensureUsernameUnique(username, null);
            UserState user = new UserState(
                userId,
                username,
                command.realName(),
                command.mobile(),
                command.email(),
                null,
                resolveAccountStatus(command.accountStatus())
            );
            users.put(userId, user);
            return toItem(user);
        });
    }

    @Override
    public UserItem updateUser(Long userId, UserUpsertCommand command) {
        return systemModelLock.withWriteLock(() -> {
            UserState existing = requireUser(userId);
            String username = resolveUsername(command.username(), existing.id());
            ensureUsernameUnique(username, existing.id());
            UserState updated = new UserState(
                existing.id(),
                username,
                command.realName(),
                command.mobile(),
                command.email(),
                existing.regionCode(),
                resolveAccountStatus(command.accountStatus())
            );
            users.put(userId, updated);
            return toItem(updated);
        });
    }

    @Override
    public void deleteUser(Long userId) {
        systemModelLock.withWriteLock(() -> {
            requireUser(userId);
            users.remove(userId);
            userRoleBindings.remove(userId);
        });
    }

    @Override
    public UserRoleBindingResult bindRoles(Long userId, List<Long> roleIds) {
        return systemModelLock.withWriteLock(() -> {
            requireUser(userId);
            List<Long> normalizedRoleIds = roleIds == null
                ? List.of()
                : roleIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            normalizedRoleIds.forEach(this::requireRole);
            userRoleBindings.put(userId, List.copyOf(normalizedRoleIds));
            return new UserRoleBindingResult(userId, normalizedRoleIds);
        });
    }

    @Override
    public boolean hasRoleBindings(Long roleId) {
        return systemModelLock.withReadLock(() -> userRoleBindings.values()
            .stream()
            .anyMatch(roleIds -> roleIds.contains(roleId)));
    }

    private UserState requireUser(Long userId) {
        UserState user = users.get(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "用户不存在:" + userId);
        }
        return user;
    }

    private UserItem toItem(UserState user) {
        List<Long> roleIds = filterExistingRoleIds(user.id());
        return new UserItem(
            user.id(),
            user.username(),
            user.realName(),
            user.mobile(),
            user.email(),
            user.accountStatus(),
            roleIds
        );
    }

    private void requireRole(Long roleId) {
        sysRoleService.getRole(roleId);
    }

    private List<Long> filterExistingRoleIds(Long userId) {
        List<Long> roleIds = userRoleBindings.getOrDefault(userId, List.of());
        List<Long> filteredRoleIds = roleIds.stream()
            .filter(this::roleExists)
            .toList();
        if (!roleIds.equals(filteredRoleIds)) {
            userRoleBindings.put(userId, List.copyOf(filteredRoleIds));
        }
        return filteredRoleIds;
    }

    private boolean roleExists(Long roleId) {
        try {
            requireRole(roleId);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private UserState resolveRequester(DataPermissionContext permissionContext) {
        if (permissionContext == null) {
            return null;
        }
        if (permissionContext.requesterUserId() != null) {
            UserState userById = users.get(permissionContext.requesterUserId());
            if (userById != null) {
                return userById;
            }
        }
        if (StringUtils.hasText(permissionContext.requesterUsername())) {
            return users.values()
                .stream()
                .filter(user -> user.username().equals(permissionContext.requesterUsername().trim()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private DataPermissionEvaluator.DataPermissionScope resolvePermissionScope(UserState requester) {
        DataPermissionEvaluator.UserPermissionProfile profile = new DataPermissionEvaluator.UserPermissionProfile(
            requester.id(),
            requester.username(),
            requester.regionCode(),
            resolveRoleCodes(requester.id())
        );
        return dataPermissionEvaluator.evaluate(profile);
    }

    private boolean canView(UserState requester,
                            UserState candidate,
                            DataPermissionEvaluator.DataPermissionScope scope) {
        if (scope.fullAccess()) {
            return true;
        }
        if (Objects.equals(requester.id(), candidate.id())) {
            return true;
        }
        if (!StringUtils.hasText(scope.regionCode())) {
            return false;
        }
        return scope.regionCode().equals(candidate.regionCode());
    }

    private List<String> resolveRoleCodes(Long userId) {
        return filterExistingRoleIds(userId).stream()
            .map(this::resolveRoleCode)
            .filter(StringUtils::hasText)
            .toList();
    }

    private String resolveRoleCode(Long roleId) {
        try {
            return sysRoleService.getRole(roleId).roleCode();
        } catch (BusinessException ex) {
            return null;
        }
    }

    private String resolveUsername(String username, Long userId) {
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        return "user-" + userId;
    }

    private Integer resolveAccountStatus(Integer accountStatus) {
        return accountStatus == null ? 1 : accountStatus;
    }

    private void ensureUsernameUnique(String username, Long currentUserId) {
        boolean duplicated = users.values()
            .stream()
            .anyMatch(user -> user.username().equals(username) && !user.id().equals(currentUserId));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "用户名已存在:" + username);
        }
    }

    private record UserState(Long id,
                             String username,
                             String realName,
                             String mobile,
                             String email,
                             String regionCode,
                             Integer accountStatus) {
    }
}
