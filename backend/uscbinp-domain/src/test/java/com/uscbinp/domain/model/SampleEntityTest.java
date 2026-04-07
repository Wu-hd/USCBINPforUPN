package com.uscbinp.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SampleEntityTest {

    @Test
    void idStrategyShouldBeAssignId() throws Exception {
        Field idField = SampleEntity.class.getDeclaredField("id");
        TableId tableId = idField.getAnnotation(TableId.class);
        assertNotNull(tableId);
        assertEquals(IdType.ASSIGN_ID, tableId.type());
    }
}
