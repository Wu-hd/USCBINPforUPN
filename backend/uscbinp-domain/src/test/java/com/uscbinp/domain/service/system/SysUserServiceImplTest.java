package com.uscbinp.domain.service.system;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.system.impl.SysRoleServiceImpl;
import com.uscbinp.domain.service.system.impl.SysUserServiceImpl;
import com.uscbinp.domain.service.system.permission.RoleRegionDataPermissionEvaluator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysUserServiceImplTest {

    @Test
    void createUserShouldRejectDuplicateUsername() {
        SysUserService service = newService();

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.createUser(new SysUserService.UserUpsertCommand("admin", "重复用户", "13800000999", "dup@uscbinp.com", 1)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void updateUserShouldRejectDuplicateUsername() {
        SysUserService service = newService();

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.updateUser(2L, new SysUserService.UserUpsertCommand("admin", "演示用户", "13800000002", "demo@uscbinp.com", 1)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void bindRolesShouldNotLeaveBindingsForDeletedUser() throws Exception {
        SysUserServiceImpl service = newService();
        SysUserService.UserItem created = service.createUser(
            new SysUserService.UserUpsertCommand("race-user", "并发用户", "13800000888", "race@uscbinp.com", 1));
        CountDownLatch streamStarted = new CountDownLatch(1);
        List<Long> slowRoleIds = new SlowRoleIdList(List.of(1L, 2L, 2L, 1L, 2L, 1L), streamStarted);

        Thread bindThread = new Thread(() -> service.bindRoles(created.id(), slowRoleIds));
        bindThread.start();
        assertTrue(streamStarted.await(2, TimeUnit.SECONDS), "bindRoles should start processing role list");

        service.deleteUser(created.id());

        bindThread.join(3000);
        assertFalse(bindThread.isAlive(), "bind thread should finish");
        assertThrows(BusinessException.class, () -> service.getUser(created.id()));
        assertFalse(readRoleBindings(service).containsKey(created.id()), "deleted user should not keep role bindings");
    }

    @Test
    void bindRolesShouldRejectNonExistentRoleIds() {
        SysUserService service = newService();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.bindRoles(1L, List.of(999L)));

        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void staleRoleBindingsShouldBeFilteredFromUserReadModel() throws Exception {
        ServiceFixture fixture = newServiceFixture();
        SysUserServiceImpl service = fixture.userService();
        readRoleBindings(service).put(1L, List.of(1L, 999L));

        SysUserService.UserItem user = service.getUser(1L);

        assertEquals(List.of(1L), user.roleIds());
    }

    @Test
    void deletingBoundRoleDuringConcurrentBindingShouldFailConsistently() throws Exception {
        ServiceFixture fixture = newServiceFixture();
        SysRoleServiceImpl roleService = fixture.roleService();
        SysUserServiceImpl service = fixture.userService();
        SysRoleService.RoleItem temporaryRole = roleService.createRole(
            new SysRoleService.RoleUpsertCommand("temp_role_" + System.nanoTime(), "临时角色", 1));
        CountDownLatch streamStarted = new CountDownLatch(1);
        List<Long> slowRoleIds = new SlowRoleIdList(List.of(1L, temporaryRole.id(), 1L, temporaryRole.id()), streamStarted);

        AtomicReference<Throwable> bindFailure = new AtomicReference<>();
        AtomicReference<Throwable> deleteFailure = new AtomicReference<>();
        Thread bindThread = new Thread(() -> {
            try {
                service.bindRoles(1L, slowRoleIds);
            } catch (Throwable throwable) {
                bindFailure.set(throwable);
            }
        }, "bind-role-thread");
        bindThread.start();
        assertTrue(streamStarted.await(2, TimeUnit.SECONDS), "bind thread should start role normalization");

        Thread deleteThread = new Thread(() -> {
            try {
                roleService.deleteRole(temporaryRole.id());
            } catch (Throwable throwable) {
                deleteFailure.set(throwable);
            }
        }, "delete-role-thread");
        deleteThread.start();

        bindThread.join(3000);
        deleteThread.join(3000);
        assertFalse(bindThread.isAlive(), "bind thread should finish");
        assertFalse(deleteThread.isAlive(), "delete thread should finish");
        assertNull(bindFailure.get(), "bind should succeed even when delete runs concurrently");
        assertTrue(deleteFailure.get() instanceof BusinessException, "delete should fail while role is still bound");
        assertEquals(List.of(1L, temporaryRole.id()), service.getUser(1L).roleIds());
    }

    private SysUserServiceImpl newService() {
        return newServiceFixture().userService();
    }

    private ServiceFixture newServiceFixture() {
        SystemModelLock lock = new SystemModelLock();
        DelegatingUserServiceBridge userServiceBridge = new DelegatingUserServiceBridge();
        SysRoleServiceImpl roleService = new SysRoleServiceImpl(userServiceBridge, lock);
        SysUserServiceImpl userService = new SysUserServiceImpl(roleService, lock, new RoleRegionDataPermissionEvaluator());
        userServiceBridge.bindDelegate(userService);
        return new ServiceFixture(userService, roleService);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, List<Long>> readRoleBindings(SysUserServiceImpl service) throws Exception {
        Field field = SysUserServiceImpl.class.getDeclaredField("userRoleBindings");
        field.setAccessible(true);
        return (Map<Long, List<Long>>) field.get(service);
    }

    private static final class SlowRoleIdList extends AbstractList<Long> {

        private final List<Long> delegate;
        private final CountDownLatch streamStarted;

        private SlowRoleIdList(List<Long> delegate, CountDownLatch streamStarted) {
            this.delegate = delegate;
            this.streamStarted = streamStarted;
        }

        @Override
        public Long get(int index) {
            return delegate.get(index);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Stream<Long> stream() {
            streamStarted.countDown();
            return delegate.stream().map(roleId -> {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(3));
                return roleId;
            });
        }
    }

    private record ServiceFixture(SysUserServiceImpl userService, SysRoleServiceImpl roleService) {
    }

    private static final class DelegatingUserServiceBridge implements SysUserService {

        private final AtomicReference<SysUserService> delegateRef = new AtomicReference<>();

        private void bindDelegate(SysUserService delegate) {
            delegateRef.set(delegate);
        }

        private SysUserService delegate() {
            SysUserService delegate = delegateRef.get();
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
        public UserRoleBindingResult bindRoles(Long userId, List<Long> roleIds) {
            return delegate().bindRoles(userId, roleIds);
        }

        @Override
        public boolean hasRoleBindings(Long roleId) {
            return delegate().hasRoleBindings(roleId);
        }
    }

}
