package com.uscbinp.domain.service.system;

import java.util.List;

public interface SysUserService {

    UserPageResult listUsers(int pageNum, int pageSize, DataPermissionContext permissionContext);

    default UserPageResult listUsers(int pageNum, int pageSize) {
        return listUsers(pageNum, pageSize, DataPermissionContext.system());
    }

    UserItem getUser(Long userId);

    UserItem createUser(UserUpsertCommand command);

    UserItem updateUser(Long userId, UserUpsertCommand command);

    void deleteUser(Long userId);

    UserRoleBindingResult bindRoles(Long userId, List<Long> roleIds);

    boolean hasRoleBindings(Long roleId);

    record UserPageResult(PageInfo page, List<UserItem> list) {
    }

    record PageInfo(int pageNum, int pageSize, long total) {
    }

    record DataPermissionContext(Long requesterUserId, String requesterUsername) {
        public static DataPermissionContext system() {
            return new DataPermissionContext(1L, "admin");
        }
    }

    record UserItem(Long id,
                    String username,
                    String realName,
                    String mobile,
                    String email,
                    Integer accountStatus,
                    List<Long> roleIds) {
    }

    record UserUpsertCommand(String username,
                             String realName,
                             String mobile,
                             String email,
                             Integer accountStatus) {
    }

    record UserRoleBindingResult(Long userId, List<Long> roleIds) {
    }
}
