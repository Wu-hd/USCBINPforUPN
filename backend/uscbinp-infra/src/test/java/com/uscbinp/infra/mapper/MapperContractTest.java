package com.uscbinp.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uscbinp.domain.model.entity.AssetFacilityEntity;
import com.uscbinp.domain.model.entity.AssetNetworkEntity;
import com.uscbinp.domain.model.entity.AssetNodeEntity;
import com.uscbinp.domain.model.entity.AssetPipeSectionEntity;
import com.uscbinp.domain.model.entity.IotDeviceEntity;
import com.uscbinp.domain.model.entity.IotMeasurePointEntity;
import com.uscbinp.domain.model.entity.OpsAlertEventEntity;
import com.uscbinp.domain.model.entity.OpsAlertRuleEntity;
import com.uscbinp.domain.model.entity.OpsWorkOrderEntity;
import com.uscbinp.domain.model.entity.OpsWorkOrderLogEntity;
import com.uscbinp.domain.model.entity.SysRoleEntity;
import com.uscbinp.domain.model.entity.SysRoleMenuEntity;
import com.uscbinp.domain.model.entity.SysUserEntity;
import com.uscbinp.domain.model.entity.SysUserRoleEntity;
import com.uscbinp.domain.model.entity.TsMeasureCurrentEntity;
import com.uscbinp.domain.model.entity.TsMeasureHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperContractTest {

    private static final Map<Class<?>, Class<?>> MAPPER_ENTITY_MAPPING = Map.ofEntries(
            Map.entry(SysUserMapper.class, SysUserEntity.class),
            Map.entry(SysRoleMapper.class, SysRoleEntity.class),
            Map.entry(SysUserRoleMapper.class, SysUserRoleEntity.class),
            Map.entry(SysRoleMenuMapper.class, SysRoleMenuEntity.class),
            Map.entry(AssetNetworkMapper.class, AssetNetworkEntity.class),
            Map.entry(AssetPipeSectionMapper.class, AssetPipeSectionEntity.class),
            Map.entry(AssetNodeMapper.class, AssetNodeEntity.class),
            Map.entry(AssetFacilityMapper.class, AssetFacilityEntity.class),
            Map.entry(IotDeviceMapper.class, IotDeviceEntity.class),
            Map.entry(IotMeasurePointMapper.class, IotMeasurePointEntity.class),
            Map.entry(TsMeasureCurrentMapper.class, TsMeasureCurrentEntity.class),
            Map.entry(TsMeasureHistoryMapper.class, TsMeasureHistoryEntity.class),
            Map.entry(OpsAlertRuleMapper.class, OpsAlertRuleEntity.class),
            Map.entry(OpsAlertEventMapper.class, OpsAlertEventEntity.class),
            Map.entry(OpsWorkOrderMapper.class, OpsWorkOrderEntity.class),
            Map.entry(OpsWorkOrderLogMapper.class, OpsWorkOrderLogEntity.class)
    );

    @Test
    void allMappersShouldDeclareMapperAnnotationAndBaseMapperGeneric() {
        MAPPER_ENTITY_MAPPING.forEach((mapperClass, entityClass) -> assertAll(
                () -> assertMapperAnnotation(mapperClass),
                () -> assertBaseMapperGeneric(mapperClass, entityClass)
        ));
    }

    private static void assertMapperAnnotation(Class<?> mapperClass) {
        Mapper mapper = mapperClass.getAnnotation(Mapper.class);
        assertNotNull(mapper, () -> mapperClass.getSimpleName() + " should have @Mapper");
    }

    private static void assertBaseMapperGeneric(Class<?> mapperClass, Class<?> entityClass) {
        boolean matched = false;
        for (Type type : mapperClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType().equals(BaseMapper.class)
                    && parameterizedType.getActualTypeArguments()[0].equals(entityClass)) {
                matched = true;
                break;
            }
        }
        assertTrue(matched, () -> mapperClass.getSimpleName() + " should extend BaseMapper<"
                + entityClass.getSimpleName() + ">");
    }
}
