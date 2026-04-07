package com.uscbinp.domain.service.system.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.SysUserService;
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

    private final Map<Long, UserState> users = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> userRoleBindings = new ConcurrentHashMap<>();
    private final AtomicLong userIdSequence = new AtomicLong(100L);

    public SysUserServiceImpl() {
        users.put(1L, new UserState(1L, "admin", "管理员", "13800000001", "admin@uscbinp.com", 1));
        users.put(2L, new UserState(2L, "demo", "演示用户", "13800000002", "demo@uscbinp.com", 1));
        userRoleBindings.put(1L, List.of(1L));
        userRoleBindings.put(2L, List.of(2L));
    }

    @Override
    public synchronized UserPageResult listUsers(int pageNum, int pageSize) {
        int resolvedPageNum = pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        int resolvedPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        List<UserState> orderedUsers = users.values()
            .stream()
            .sorted(Comparator.comparing(UserState::id))
            .toList();
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, orderedUsers.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, orderedUsers.size());
        List<UserItem> list = orderedUsers.subList(fromIndex, toIndex)
            .stream()
            .map(this::toItem)
            .toList();
        return new UserPageResult(new PageInfo(resolvedPageNum, resolvedPageSize, orderedUsers.size()), list);
    }

    @Override
    public synchronized UserItem getUser(Long userId) {
        return toItem(requireUser(userId));
    }

    @Override
    public synchronized UserItem createUser(UserUpsertCommand command) {
        Long userId = userIdSequence.incrementAndGet();
        String username = resolveUsername(command.username(), userId);
        ensureUsernameUnique(username, null);
        UserState user = new UserState(
            userId,
            username,
            command.realName(),
            command.mobile(),
            command.email(),
            resolveAccountStatus(command.accountStatus())
        );
        users.put(userId, user);
        return toItem(user);
    }

    @Override
    public synchronized UserItem updateUser(Long userId, UserUpsertCommand command) {
        UserState existing = requireUser(userId);
        String username = resolveUsername(command.username(), existing.id());
        ensureUsernameUnique(username, existing.id());
        UserState updated = new UserState(
            existing.id(),
            username,
            command.realName(),
            command.mobile(),
            command.email(),
            resolveAccountStatus(command.accountStatus())
        );
        users.put(userId, updated);
        return toItem(updated);
    }

    @Override
    public synchronized void deleteUser(Long userId) {
        requireUser(userId);
        users.remove(userId);
        userRoleBindings.remove(userId);
    }

    @Override
    public synchronized UserRoleBindingResult bindRoles(Long userId, List<Long> roleIds) {
        requireUser(userId);
        List<Long> normalizedRoleIds = roleIds == null
            ? List.of()
            : roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        userRoleBindings.put(userId, List.copyOf(normalizedRoleIds));
        return new UserRoleBindingResult(userId, normalizedRoleIds);
    }

    private UserState requireUser(Long userId) {
        UserState user = users.get(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "用户不存在:" + userId);
        }
        return user;
    }

    private UserItem toItem(UserState user) {
        return new UserItem(
            user.id(),
            user.username(),
            user.realName(),
            user.mobile(),
            user.email(),
            user.accountStatus(),
            userRoleBindings.getOrDefault(user.id(), List.of())
        );
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
                             Integer accountStatus) {
    }
}
