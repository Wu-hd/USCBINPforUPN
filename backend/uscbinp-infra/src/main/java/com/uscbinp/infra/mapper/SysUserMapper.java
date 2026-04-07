package com.uscbinp.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uscbinp.domain.model.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {
}
