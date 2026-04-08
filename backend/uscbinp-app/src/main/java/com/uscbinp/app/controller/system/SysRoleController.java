package com.uscbinp.app.controller.system;

import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.system.SysRoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

@RestController
@RequestMapping("/api/system/roles")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @GetMapping
    public ApiResponse<SysRoleService.RolePageResult> listRoles(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.ok(sysRoleService.listRoles(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRoleService.RoleItem> getRole(@PathVariable Long id) {
        return ApiResponse.ok(sysRoleService.getRole(id));
    }

    @PostMapping
    public ApiResponse<SysRoleService.RoleItem> createRole(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(sysRoleService.createRole(toCommand(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<SysRoleService.RoleItem> updateRole(@PathVariable Long id,
                                                           @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(sysRoleService.updateRole(id, toCommand(request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return ApiResponse.ok((Void) null);
    }

    private SysRoleService.RoleUpsertCommand toCommand(RoleSaveRequest request) {
        return new SysRoleService.RoleUpsertCommand(request.roleCode(), request.roleName(), request.roleStatus());
    }

    private record RoleSaveRequest(@NotBlank String roleCode,
                                   @NotBlank String roleName,
                                   @NotNull Integer roleStatus) {
    }
}
