package com.uscbinp.infra.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AuthSchemaSqlContractTest {

    private static final Map<String, String> REQUIRED_AUDIT_COLUMNS = Map.of(
            "created_by", "bigint",
            "created_time", "datetime",
            "updated_by", "bigint",
            "updated_time", "datetime",
            "is_deleted", "tinyint"
    );

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
    void shouldRequireCreateTableIfNotExistsClause() {
        assertAll(
                () -> assertFalse(hasCreateTableClause("create table sys_user (id bigint);", "sys_user")),
                () -> assertTrue(hasCreateTableClause("create table if not exists sys_user (id bigint);", "sys_user"))
        );
    }

    @Test
    void shouldRejectAuditColumnsWithIncorrectTypes() {
        String sql = """
                create table if not exists sys_user (
                    id bigint primary key,
                    created_by varchar(32),
                    created_time timestamp,
                    updated_by varchar(32),
                    updated_time timestamp,
                    is_deleted int
                );
                """;
        assertThrows(AssertionError.class, () -> assertTableHasAuditColumns(sql, "sys_user"));
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
        for (Map.Entry<String, String> auditColumn : REQUIRED_AUDIT_COLUMNS.entrySet()) {
            String columnName = auditColumn.getKey();
            String expectedType = auditColumn.getValue();
            assertTrue(
                    hasColumnWithType(tableBody, columnName, expectedType),
                    () -> tableName + " must define " + columnName + " as " + expectedType
            );
        }
    }

    private static String extractCreateTableBody(String sql, String tableName) {
        Pattern pattern = Pattern.compile(
                "(?is)create\\s+table\\s+if\\s+not\\s+exists\\s+`?" + Pattern.quote(tableName) + "`?\\s*\\((.*?)\\)\\s*;");
        Matcher matcher = pattern.matcher(sql);
        assertTrue(matcher.find(), () -> "Missing CREATE TABLE IF NOT EXISTS for " + tableName);
        return matcher.group(1);
    }

    private static boolean hasCreateTableClause(String sql, String tableName) {
        return Pattern.compile(
                        "(?is).*create\\s+table\\s+if\\s+not\\s+exists\\s+`?" + Pattern.quote(tableName) + "`?\\s*\\(.*")
                .matcher(sql)
                .matches();
    }

    private static boolean hasColumnWithType(String tableBody, String columnName, String expectedType) {
        return Pattern.compile(
                        "(?im)^\\s*`?" + Pattern.quote(columnName) + "`?\\s+" + Pattern.quote(expectedType) + "\\b")
                .matcher(tableBody)
                .find();
    }
}
