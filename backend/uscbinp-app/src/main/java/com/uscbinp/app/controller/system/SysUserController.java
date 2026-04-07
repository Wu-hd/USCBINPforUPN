package com.uscbinp.app.controller.system;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.system.SysUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
        @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.ok(sysUserService.listUsers(pageNum, pageSize));
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

    private record UserSaveRequest(@NotBlank String username,
                                   String realName,
                                   String mobile,
                                   String email,
                                   @NotNull Integer accountStatus) {
    }

    private record UserRoleBindRequest(@NotEmpty List<@NotNull Long> roleIds) {
    }
}
