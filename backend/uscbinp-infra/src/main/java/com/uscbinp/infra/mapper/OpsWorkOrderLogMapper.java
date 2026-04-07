package com.uscbinp.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uscbinp.domain.model.entity.OpsWorkOrderLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpsWorkOrderLogMapper extends BaseMapper<OpsWorkOrderLogEntity> {
}
