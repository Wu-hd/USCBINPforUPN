package com.uscbinp.infra.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final Map<String, String> REQUIRED_ASSET_NETWORK_COLUMNS = Map.of(
            "network_code", "varchar(64)",
            "network_name", "varchar(128)",
            "network_type", "varchar(32)",
            "region_code", "varchar(32)",
            "owner_unit", "varchar(128)",
            "operation_unit", "varchar(128)",
            "service_status", "tinyint",
            "health_level", "tinyint",
            "risk_level", "tinyint"
    );
    private static final Map<String, String> REQUIRED_ASSET_PIPE_SECTION_COLUMNS = Map.ofEntries(
            Map.entry("section_code", "varchar(64)"),
            Map.entry("network_id", "bigint"),
            Map.entry("section_name", "varchar(128)"),
            Map.entry("pipe_material", "varchar(64)"),
            Map.entry("diameter_mm", "decimal(10,2)"),
            Map.entry("bury_depth_m", "decimal(10,2)"),
            Map.entry("pipe_age_year", "int"),
            Map.entry("old_flag", "tinyint"),
            Map.entry("renovation_status", "tinyint"),
            Map.entry("start_node_id", "bigint"),
            Map.entry("end_node_id", "bigint"),
            Map.entry("latest_health_score", "decimal(8,2)"),
            Map.entry("latest_risk_level", "tinyint")
    );
    private static final Map<String, String> REQUIRED_ASSET_NODE_COLUMNS = Map.of(
            "node_code", "varchar(64)",
            "network_id", "bigint",
            "node_type", "varchar(32)",
            "node_name", "varchar(128)",
            "longitude", "decimal(12,7)",
            "latitude", "decimal(12,7)",
            "elevation_m", "decimal(10,2)",
            "access_status", "tinyint",
            "online_flag", "tinyint"
    );
    private static final Map<String, String> REQUIRED_ASSET_FACILITY_COLUMNS = Map.of(
            "facility_code", "varchar(64)",
            "section_id", "bigint",
            "node_id", "bigint",
            "facility_type", "varchar(32)",
            "facility_name", "varchar(128)",
            "manufacturer", "varchar(128)",
            "install_date", "date",
            "maintain_cycle_day", "int",
            "facility_status", "tinyint"
    );
    private static final Map<String, String> REQUIRED_IOT_DEVICE_COLUMNS = Map.of(
            "device_code", "varchar(64)",
            "device_name", "varchar(128)",
            "device_type", "varchar(32)",
            "protocol_type", "varchar(32)",
            "gateway_code", "varchar(64)",
            "facility_id", "bigint",
            "region_code", "varchar(32)",
            "online_status", "tinyint",
            "last_online_time", "datetime",
            "firmware_version", "varchar(64)"
    );
    private static final Map<String, String> REQUIRED_IOT_MEASURE_POINT_COLUMNS = Map.ofEntries(
            Map.entry("point_code", "varchar(64)"),
            Map.entry("point_name", "varchar(128)"),
            Map.entry("device_id", "bigint"),
            Map.entry("section_id", "bigint"),
            Map.entry("node_id", "bigint"),
            Map.entry("metric_type", "varchar(32)"),
            Map.entry("unit_name", "varchar(16)"),
            Map.entry("sample_cycle_sec", "int"),
            Map.entry("threshold_min", "decimal(16,4)"),
            Map.entry("threshold_max", "decimal(16,4)"),
            Map.entry("point_status", "tinyint")
    );
    private static final Map<String, String> REQUIRED_TS_MEASURE_CURRENT_COLUMNS = Map.of(
            "point_id", "bigint",
            "metric_type", "varchar(32)",
            "current_value", "decimal(18,6)",
            "quality_flag", "tinyint",
            "alarm_flag", "tinyint",
            "collect_time", "datetime",
            "receive_time", "datetime",
            "edge_node_code", "varchar(64)",
            "raw_payload", "json"
    );
    private static final Map<String, String> REQUIRED_TS_MEASURE_HISTORY_COLUMNS = Map.of(
            "point_id", "bigint",
            "section_id", "bigint",
            "metric_type", "varchar(32)",
            "metric_value", "decimal(18,6)",
            "quality_flag", "tinyint",
            "collect_time", "timestamp",
            "receive_time", "timestamp",
            "calc_tag", "varchar(32)",
            "trace_id", "varchar(64)"
    );
    private static final Map<String, String> REQUIRED_OPS_ALERT_RULE_COLUMNS = Map.of(
            "rule_code", "varchar(64)",
            "rule_name", "varchar(128)",
            "metric_type", "varchar(32)",
            "target_scope", "varchar(255)",
            "rule_expr", "varchar(1000)",
            "alert_level", "tinyint",
            "merge_window_min", "int",
            "notify_channel", "varchar(255)",
            "rule_status", "tinyint"
    );
    private static final Map<String, String> REQUIRED_OPS_ALERT_EVENT_COLUMNS = Map.ofEntries(
            Map.entry("alert_code", "varchar(64)"),
            Map.entry("rule_id", "bigint"),
            Map.entry("target_type", "varchar(32)"),
            Map.entry("target_id", "bigint"),
            Map.entry("point_id", "bigint"),
            Map.entry("alert_title", "varchar(255)"),
            Map.entry("alert_level", "tinyint"),
            Map.entry("alert_status", "tinyint"),
            Map.entry("occur_time", "datetime"),
            Map.entry("confirm_time", "datetime"),
            Map.entry("close_time", "datetime"),
            Map.entry("work_order_id", "bigint")
    );
    private static final Map<String, String> REQUIRED_OPS_WORK_ORDER_COLUMNS = Map.ofEntries(
            Map.entry("work_order_code", "varchar(64)"),
            Map.entry("source_type", "varchar(32)"),
            Map.entry("source_id", "bigint"),
            Map.entry("title", "varchar(255)"),
            Map.entry("target_type", "varchar(32)"),
            Map.entry("target_id", "bigint"),
            Map.entry("region_code", "varchar(32)"),
            Map.entry("assignee_user_id", "bigint"),
            Map.entry("work_status", "tinyint"),
            Map.entry("expect_finish_time", "datetime"),
            Map.entry("actual_finish_time", "datetime"),
            Map.entry("result_summary", "varchar(500)")
    );
    private static final Map<String, String> REQUIRED_OPS_WORK_ORDER_LOG_COLUMNS = Map.of(
            "work_order_id", "bigint",
            "action_type", "varchar(32)",
            "operator_user_id", "bigint",
            "action_time", "datetime",
            "before_status", "tinyint",
            "after_status", "tinyint",
            "action_desc", "varchar(500)",
            "attachment_group_id", "bigint",
            "location_text", "varchar(255)"
    );

    @Test
    void shouldContainRequiredTask2TablesAndAuditColumns() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();

        assertAll(
                () -> assertTableHasColumns(sql, "sys_user", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_USER_COLUMNS),
                () -> assertTableHasColumns(sql, "sys_role", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_ROLE_COLUMNS),
                () -> assertTableHasColumns(sql, "sys_user_role", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_USER_ROLE_COLUMNS),
                () -> assertTableHasColumns(sql, "sys_role_menu", REQUIRED_AUDIT_COLUMNS, REQUIRED_SYS_ROLE_MENU_COLUMNS),
                () -> assertTableHasColumns(sql, "asset_network", REQUIRED_AUDIT_COLUMNS, REQUIRED_ASSET_NETWORK_COLUMNS),
                () -> assertTableHasColumns(sql, "asset_pipe_section", REQUIRED_AUDIT_COLUMNS, REQUIRED_ASSET_PIPE_SECTION_COLUMNS),
                () -> assertTableHasColumns(sql, "asset_node", REQUIRED_AUDIT_COLUMNS, REQUIRED_ASSET_NODE_COLUMNS),
                () -> assertTableHasColumns(sql, "asset_facility", REQUIRED_AUDIT_COLUMNS, REQUIRED_ASSET_FACILITY_COLUMNS),
                () -> assertTableHasColumns(sql, "iot_device", REQUIRED_AUDIT_COLUMNS, REQUIRED_IOT_DEVICE_COLUMNS),
                () -> assertTableHasColumns(sql, "iot_measure_point", REQUIRED_AUDIT_COLUMNS, REQUIRED_IOT_MEASURE_POINT_COLUMNS),
                () -> assertTableHasColumns(sql, "ts_measure_current", REQUIRED_AUDIT_COLUMNS, REQUIRED_TS_MEASURE_CURRENT_COLUMNS),
                () -> assertTableHasColumns(sql, "ts_measure_history", REQUIRED_AUDIT_COLUMNS, REQUIRED_TS_MEASURE_HISTORY_COLUMNS),
                () -> assertTableHasColumns(sql, "ops_alert_rule", REQUIRED_AUDIT_COLUMNS, REQUIRED_OPS_ALERT_RULE_COLUMNS),
                () -> assertTableHasColumns(sql, "ops_alert_event", REQUIRED_AUDIT_COLUMNS, REQUIRED_OPS_ALERT_EVENT_COLUMNS),
                () -> assertTableHasColumns(sql, "ops_work_order", REQUIRED_AUDIT_COLUMNS, REQUIRED_OPS_WORK_ORDER_COLUMNS),
                () -> assertTableHasColumns(sql, "ops_work_order_log", REQUIRED_AUDIT_COLUMNS, REQUIRED_OPS_WORK_ORDER_LOG_COLUMNS)
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

    @Test
    void shouldContainExpectedTotalTableCount() {
        String sql = SqlResourceLoader.loadAsString("sql/task2-core-schema.sql").toLowerCase();
        assertEquals(16, countCreateTableStatements(sql));
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
                "(?is)create\\s+table\\s+if\\s+not\\s+exists\\s+`?" + Pattern.quote(tableName) + "`?\\s*\\((.*)\\)\\s*[^;]*;");
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

    private static int countCreateTableStatements(String sql) {
        Matcher matcher = Pattern.compile("(?is)create\\s+table\\s+if\\s+not\\s+exists\\s+`?\\w+`?\\s*\\(").matcher(sql);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
