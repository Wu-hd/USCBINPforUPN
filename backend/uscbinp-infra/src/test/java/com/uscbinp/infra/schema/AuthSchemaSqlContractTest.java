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
    private static final Map<String, String> REQUIRED_SYS_USER_COLUMNS = Map.of(
            "username", "varchar(64)",
            "password_hash", "varchar(255)",
            "real_name", "varchar(64)",
            "account_status", "tinyint"
    );
    private static final Map<String, String> REQUIRED_SYS_ROLE_COLUMNS = Map.of(
            "role_code", "varchar(64)",
            "role_name", "varchar(64)",
            "role_status", "tinyint"
    );
    private static final Map<String, String> REQUIRED_SYS_USER_ROLE_COLUMNS = Map.of(
            "user_id", "bigint",
            "role_id", "bigint"
    );
    private static final Map<String, String> REQUIRED_SYS_ROLE_MENU_COLUMNS = Map.of(
            "role_id", "bigint",
            "menu_code", "varchar(64)"
    );

    @Test
    void shouldContainRequiredAuthTablesAndAuditColumns() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();

        assertAll(
                () -> assertTableHasColumns(sql, "sys_user", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_USER_COLUMNS),
                () -> assertTableHasColumns(sql, "sys_role", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_ROLE_COLUMNS),
                () -> assertTableHasColumns(sql, "sys_user_role", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_USER_ROLE_COLUMNS),
                () -> assertTableHasColumns(sql, "sys_role_menu", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_ROLE_MENU_COLUMNS)
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
        assertThrows(AssertionError.class, () -> assertTableHasColumns(sql, "sys_user", REQUIRED_AUDIT_COLUMNS));
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

    @Test
    void shouldDefineRequiredCompositeUniqueConstraints() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();

        assertAll(
                () -> assertTrue(
                        hasCompositeUniqueConstraint(extractCreateTableBody(sql, "sys_user_role"), "user_id", "role_id"),
                        "sys_user_role must define unique(user_id, role_id)"),
                () -> assertTrue(
                        hasCompositeUniqueConstraint(extractCreateTableBody(sql, "sys_role_menu"), "role_id", "menu_code"),
                        "sys_role_menu must define unique(role_id, menu_code)")
        );
    }

    @SafeVarargs
    private static void assertTableHasColumns(String sql, String tableName, Map<String, String>... requiredColumns) {
        String tableBody = extractCreateTableBody(sql, tableName);
        for (Map<String, String> columns : requiredColumns) {
            for (Map.Entry<String, String> column : columns.entrySet()) {
                String columnName = column.getKey();
                String expectedType = column.getValue();
                assertTrue(
                        hasColumnWithType(tableBody, columnName, expectedType),
                        () -> tableName + " must define " + columnName + " as " + expectedType
                );
            }
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
                        "(?im)^\\s*`?" + Pattern.quote(columnName) + "`?\\s+" + Pattern.quote(expectedType) + "(?=\\s|,|$)")
                .matcher(tableBody)
                .find();
    }

    private static boolean hasCompositeUniqueConstraint(String tableBody, String firstColumn, String secondColumn) {
        return Pattern.compile(
                        "(?is)(?:constraint\\s+`?\\w+`?\\s+)?unique(?:\\s+key\\s+`?\\w+`?)?\\s*\\(\\s*`?"
                                + Pattern.quote(firstColumn)
                                + "`?\\s*,\\s*`?"
                                + Pattern.quote(secondColumn)
                                + "`?\\s*\\)")
                .matcher(tableBody)
                .find();
    }
}
