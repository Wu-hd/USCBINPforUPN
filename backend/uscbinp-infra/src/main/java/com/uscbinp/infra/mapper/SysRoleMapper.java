package com.uscbinp.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uscbinp.domain.model.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {
}
