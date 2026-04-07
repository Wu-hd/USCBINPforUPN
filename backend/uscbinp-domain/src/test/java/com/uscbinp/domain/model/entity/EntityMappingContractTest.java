package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityMappingContractTest {

    private static final Map<Class<?>, String> ENTITY_TABLE_MAPPING = Map.ofEntries(
            Map.entry(SysUserEntity.class, "sys_user"),
            Map.entry(SysRoleEntity.class, "sys_role"),
            Map.entry(SysUserRoleEntity.class, "sys_user_role"),
            Map.entry(SysRoleMenuEntity.class, "sys_role_menu"),
            Map.entry(AssetNetworkEntity.class, "asset_network"),
            Map.entry(AssetPipeSectionEntity.class, "asset_pipe_section"),
            Map.entry(AssetNodeEntity.class, "asset_node"),
            Map.entry(AssetFacilityEntity.class, "asset_facility"),
            Map.entry(IotDeviceEntity.class, "iot_device"),
            Map.entry(IotMeasurePointEntity.class, "iot_measure_point"),
            Map.entry(TsMeasureCurrentEntity.class, "ts_measure_current"),
            Map.entry(TsMeasureHistoryEntity.class, "ts_measure_history"),
            Map.entry(OpsAlertRuleEntity.class, "ops_alert_rule"),
            Map.entry(OpsAlertEventEntity.class, "ops_alert_event"),
            Map.entry(OpsWorkOrderEntity.class, "ops_work_order"),
            Map.entry(OpsWorkOrderLogEntity.class, "ops_work_order_log")
    );

    @Test
    void baseAuditEntityShouldDefineStandardAuditFields() throws Exception {
        assertAll(
                () -> assertNotNull(BaseAuditEntity.class.getDeclaredField("createdBy")),
                () -> assertNotNull(BaseAuditEntity.class.getDeclaredField("createdTime")),
                () -> assertNotNull(BaseAuditEntity.class.getDeclaredField("updatedBy")),
                () -> assertNotNull(BaseAuditEntity.class.getDeclaredField("updatedTime")),
                () -> assertTableLogic(BaseAuditEntity.class, "isDeleted")
        );
    }

    @Test
    void allTask2EntitiesShouldDefineTableNameAssignIdAndAuditInheritance() {
        ENTITY_TABLE_MAPPING.forEach((entityClass, tableName) -> assertAll(
                () -> assertEquals(BaseAuditEntity.class, entityClass.getSuperclass()),
                () -> assertTableName(entityClass, tableName),
                () -> assertAssignId(entityClass)
        ));
    }

    private static void assertTableName(Class<?> entityClass, String tableName) {
        TableName annotation = entityClass.getAnnotation(TableName.class);
        assertNotNull(annotation, () -> entityClass.getSimpleName() + " should have @TableName");
        assertEquals(tableName, annotation.value());
    }

    private static void assertAssignId(Class<?> entityClass) {
        try {
            Field idField = entityClass.getDeclaredField("id");
            TableId tableId = idField.getAnnotation(TableId.class);
            assertNotNull(tableId, () -> entityClass.getSimpleName() + " should have @TableId on id");
            assertEquals(IdType.ASSIGN_ID, tableId.type());
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(entityClass.getSimpleName() + " should declare id field", ex);
        }
    }

    private static void assertTableLogic(Class<?> entityClass, String fieldName) {
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            TableLogic tableLogic = field.getAnnotation(TableLogic.class);
            assertNotNull(tableLogic, () -> entityClass.getSimpleName() + "." + fieldName + " should have @TableLogic");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(entityClass.getSimpleName() + " should declare field: " + fieldName, ex);
        }
    }
}
