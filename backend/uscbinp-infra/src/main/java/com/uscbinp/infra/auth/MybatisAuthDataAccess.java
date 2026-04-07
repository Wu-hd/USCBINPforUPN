package com.uscbinp.infra.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.uscbinp.domain.model.entity.SysRoleMenuEntity;
import com.uscbinp.domain.model.entity.SysUserEntity;
import com.uscbinp.domain.model.entity.SysUserRoleEntity;
import com.uscbinp.domain.service.auth.AuthDataAccess;
import com.uscbinp.infra.mapper.SysRoleMenuMapper;
import com.uscbinp.infra.mapper.SysUserMapper;
import com.uscbinp.infra.mapper.SysUserRoleMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnBean({SysUserMapper.class, SysUserRoleMapper.class, SysRoleMenuMapper.class})
public class MybatisAuthDataAccess implements AuthDataAccess {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    public MybatisAuthDataAccess(SysUserMapper sysUserMapper,
                                 SysUserRoleMapper sysUserRoleMapper,
                                 SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    @Override
    public Optional<AuthUserSnapshot> findUserByUsername(String username) {
        SysUserEntity user = sysUserMapper.selectOne(Wrappers.<SysUserEntity>lambdaQuery()
            .eq(SysUserEntity::getUsername, username));
        return Optional.ofNullable(user).map(this::toSnapshot);
    }

    @Override
    public Optional<AuthUserSnapshot> findUserById(Long userId) {
        SysUserEntity user = sysUserMapper.selectById(userId);
        return Optional.ofNullable(user).map(this::toSnapshot);
    }

    @Override
    public List<String> findMenuCodesByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(Wrappers.<SysUserRoleEntity>lambdaQuery()
                .eq(SysUserRoleEntity::getUserId, userId))
            .stream()
            .map(SysUserRoleEntity::getRoleId)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMenuMapper.selectList(Wrappers.<SysRoleMenuEntity>lambdaQuery()
                .in(SysRoleMenuEntity::getRoleId, roleIds))
            .stream()
            .map(SysRoleMenuEntity::getMenuCode)
            .distinct()
            .toList();
    }

    private AuthUserSnapshot toSnapshot(SysUserEntity user) {
        return new AuthUserSnapshot(user.getId(), user.getUsername(), user.getPasswordHash(), user.getAccountStatus());
    }
}
