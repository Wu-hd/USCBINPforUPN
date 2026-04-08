package com.uscbinp.domain.service.system;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.impl.SysRoleServiceImpl;
import com.uscbinp.domain.service.system.impl.SysUserServiceImpl;
import com.uscbinp.domain.service.system.permission.RoleRegionDataPermissionEvaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SysRoleServiceImplTest {

    @Test
    void listRolesShouldReturnSeededData() {
        SysRoleService service = new SysRoleServiceImpl(noBindingUserService(), new SystemModelLock());
        SysRoleService.RolePageResult pageResult = service.listRoles(1, 10);

        assertEquals(2, pageResult.list().size());
    }

    @Test
    void createRoleShouldRejectDuplicateRoleCode() {
        SysRoleService service = new SysRoleServiceImpl(noBindingUserService(), new SystemModelLock());

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.createRole(new SysRoleService.RoleUpsertCommand("sys_admin", "重复角色", 1)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void updateRoleShouldRejectDuplicateRoleCode() {
        SysRoleService service = new SysRoleServiceImpl(noBindingUserService(), new SystemModelLock());

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.updateRole(2L, new SysRoleService.RoleUpsertCommand("sys_admin", "运维用户", 1)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void deleteRoleShouldRejectWhenUsersStillBound() {
        SystemModelLock lock = new SystemModelLock();
        DelegatingUserServiceBridge userServiceBridge = new DelegatingUserServiceBridge();
        SysRoleServiceImpl roleService = new SysRoleServiceImpl(userServiceBridge, lock);
        SysUserServiceImpl userService = new SysUserServiceImpl(roleService, lock, new RoleRegionDataPermissionEvaluator());
        userServiceBridge.bindDelegate(userService);
        userService.bindRoles(1L, java.util.List.of(1L, 2L));

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.deleteRole(2L));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    private SysUserService noBindingUserService() {
        return new SysUserService() {
            @Override
            public UserPageResult listUsers(int pageNum, int pageSize) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UserPageResult listUsers(int pageNum, int pageSize, DataPermissionContext permissionContext) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UserItem getUser(Long userId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UserItem createUser(UserUpsertCommand command) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UserItem updateUser(Long userId, UserUpsertCommand command) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void deleteUser(Long userId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UserRoleBindingResult bindRoles(Long userId, java.util.List<Long> roleIds) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasRoleBindings(Long roleId) {
                return false;
            }
        };
    }

    private static final class DelegatingUserServiceBridge implements SysUserService {

        private SysUserService delegate;

        private void bindDelegate(SysUserService delegate) {
            this.delegate = delegate;
        }

        private SysUserService delegate() {
            if (delegate == null) {
                throw new IllegalStateException("delegate not bound");
            }
            return delegate;
        }

        @Override
        public UserPageResult listUsers(int pageNum, int pageSize) {
            return delegate().listUsers(pageNum, pageSize);
        }

        @Override
        public UserPageResult listUsers(int pageNum, int pageSize, DataPermissionContext permissionContext) {
            return delegate().listUsers(pageNum, pageSize, permissionContext);
        }

        @Override
        public UserItem getUser(Long userId) {
            return delegate().getUser(userId);
        }

        @Override
        public UserItem createUser(UserUpsertCommand command) {
            return delegate().createUser(command);
        }

        @Override
        public UserItem updateUser(Long userId, UserUpsertCommand command) {
            return delegate().updateUser(userId, command);
        }

        @Override
        public void deleteUser(Long userId) {
            delegate().deleteUser(userId);
        }

        @Override
        public UserRoleBindingResult bindRoles(Long userId, java.util.List<Long> roleIds) {
            return delegate().bindRoles(userId, roleIds);
        }

        @Override
        public boolean hasRoleBindings(Long roleId) {
            return delegate().hasRoleBindings(roleId);
        }
    }
}
