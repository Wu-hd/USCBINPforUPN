package com.uscbinp.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uscbinp.domain.model.entity.IotMeasurePointEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotMeasurePointMapper extends BaseMapper<IotMeasurePointEntity> {
}
