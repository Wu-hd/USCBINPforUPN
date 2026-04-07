package com.uscbinp.infra.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSchemaSqlContractTest {

    @Test
    void shouldContainRequiredAuthTablesAndAuditColumns() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();

        assertAll(
                () -> assertTrue(sql.matches("(?s).*create\\s+table\\s+`?sys_user`?.*")),
                () -> assertTrue(sql.matches("(?s).*create\\s+table\\s+`?sys_role`?.*")),
                () -> assertTrue(sql.matches("(?s).*create\\s+table\\s+`?sys_user_role`?.*")),
                () -> assertTrue(sql.matches("(?s).*create\\s+table\\s+`?sys_role_menu`?.*")),
                () -> assertTrue(sql.contains("created_by")),
                () -> assertTrue(sql.contains("created_time")),
                () -> assertTrue(sql.contains("updated_by")),
                () -> assertTrue(sql.contains("updated_time")),
                () -> assertTrue(sql.contains("is_deleted"))
        );
    }
}
