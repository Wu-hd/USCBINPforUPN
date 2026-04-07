package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_role")
public class SysRoleEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String roleCode;
    private String roleName;
    private String dataScope;
    private String subsystemScope;
    private String menuScope;
    private Integer sortNo;
    private Integer roleStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getSubsystemScope() {
        return subsystemScope;
    }

    public void setSubsystemScope(String subsystemScope) {
        this.subsystemScope = subsystemScope;
    }

    public String getMenuScope() {
        return menuScope;
    }

    public void setMenuScope(String menuScope) {
        this.menuScope = menuScope;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(Integer roleStatus) {
        this.roleStatus = roleStatus;
    }
}
