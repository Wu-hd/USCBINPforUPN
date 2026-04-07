package com.uscbinp.infra.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AuthSchemaSqlContractTest {

    private static final String[] REQUIRED_AUDIT_COLUMNS = {
            "created_by",
            "created_time",
            "updated_by",
            "updated_time",
            "is_deleted"
    };

    @Test
    void shouldContainRequiredAuthTablesAndAuditColumns() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();

        assertAll(
                () -> assertTableHasAuditColumns(sql, "sys_user"),
                () -> assertTableHasAuditColumns(sql, "sys_role"),
                () -> assertTableHasAuditColumns(sql, "sys_user_role"),
                () -> assertTableHasAuditColumns(sql, "sys_role_menu")
        );
    }

    @Test
    void shouldAcceptCreateTableWithOrWithoutIfNotExistsClause() {
        assertAll(
                () -> assertTrue(hasCreateTableClause("create table sys_user (id bigint);", "sys_user")),
                () -> assertTrue(hasCreateTableClause("create table if not exists sys_user (id bigint);", "sys_user"))
        );
    }

    @Test
    void shouldAlignWithAssignIdAndRoleMenuModel() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();

        assertAll(
                () -> assertFalse(extractCreateTableBody(sql, "sys_user").contains("auto_increment")),
                () -> assertFalse(extractCreateTableBody(sql, "sys_role").contains("auto_increment")),
                () -> assertFalse(extractCreateTableBody(sql, "sys_user_role").contains("auto_increment")),
                () -> assertFalse(extractCreateTableBody(sql, "sys_role_menu").contains("auto_increment")),
                () -> assertTrue(extractCreateTableBody(sql, "sys_role_menu").contains("menu_code")),
                () -> assertFalse(extractCreateTableBody(sql, "sys_role_menu").contains("menu_id"))
        );
    }

    private static void assertTableHasAuditColumns(String sql, String tableName) {
        String tableBody = extractCreateTableBody(sql, tableName);
        for (String auditColumn : REQUIRED_AUDIT_COLUMNS) {
            assertTrue(tableBody.contains(auditColumn), () -> tableName + " must contain " + auditColumn);
        }
    }

    private static String extractCreateTableBody(String sql, String tableName) {
        Pattern pattern = Pattern.compile(
                "(?is)create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?`?" + Pattern.quote(tableName) + "`?\\s*\\((.*?)\\)\\s*;");
        Matcher matcher = pattern.matcher(sql);
        assertTrue(matcher.find(), () -> "Missing CREATE TABLE for " + tableName);
        return matcher.group(1);
    }

    private static boolean hasCreateTableClause(String sql, String tableName) {
        return Pattern.compile(
                        "(?is).*create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?`?" + Pattern.quote(tableName) + "`?\\s*\\(.*")
                .matcher(sql)
                .matches();
    }
}
