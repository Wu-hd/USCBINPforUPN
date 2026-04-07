package com.uscbinp.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uscbinp.domain.model.entity.OpsAlertRuleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpsAlertRuleMapper extends BaseMapper<OpsAlertRuleEntity> {
}
