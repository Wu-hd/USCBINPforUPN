package com.uscbinp.app.controller.system;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.system.SysUserService;
import com.uscbinp.infra.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public ApiResponse<SysUserService.UserPageResult> listUsers(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        Authentication authentication) {
        return ApiResponse.ok(sysUserService.listUsers(pageNum, pageSize, resolvePermissionContext(authentication)));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUserService.UserItem> getUser(@PathVariable Long id) {
        return ApiResponse.ok(sysUserService.getUser(id));
    }

    @PostMapping
    public ApiResponse<SysUserService.UserItem> createUser(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(sysUserService.createUser(toCommand(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<SysUserService.UserItem> updateUser(@PathVariable Long id,
                                                           @Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(sysUserService.updateUser(id, toCommand(request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return ApiResponse.ok((Void) null);
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<SysUserService.UserRoleBindingResult> bindRoles(@PathVariable Long id,
                                                                        @Valid @RequestBody UserRoleBindRequest request) {
        return ApiResponse.ok(sysUserService.bindRoles(id, request.roleIds()));
    }

    private SysUserService.UserUpsertCommand toCommand(UserSaveRequest request) {
        return new SysUserService.UserUpsertCommand(
            request.username(),
            request.realName(),
            request.mobile(),
            request.email(),
            request.accountStatus()
        );
    }

    private SysUserService.DataPermissionContext resolvePermissionContext(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return new SysUserService.DataPermissionContext(null, null);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return new SysUserService.DataPermissionContext(authenticatedUser.userId(), authenticatedUser.username());
        }
        if (principal instanceof String username && !"anonymousUser".equals(username)) {
            return new SysUserService.DataPermissionContext(null, username);
        }
        return new SysUserService.DataPermissionContext(null, null);
    }

    private record UserSaveRequest(@NotBlank String username,
                                   String realName,
                                   String mobile,
                                   String email,
                                   @NotNull Integer accountStatus) {
    }

    private record UserRoleBindRequest(@NotNull List<@NotNull Long> roleIds) {
    }
}
