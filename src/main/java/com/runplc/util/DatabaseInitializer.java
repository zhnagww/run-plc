package com.runplc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @PostConstruct
    public void init() {
        try {
            // 显式加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
            logger.info("SQLite JDBC驱动加载成功");
        } catch (ClassNotFoundException e) {
            logger.error("无法加载SQLite JDBC驱动", e);
        }
    }

    /**
     * 初始化数据库表结构
     */
    public void initializeTables() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            // 创建app_data表
            String createAppDataTableSQL = "CREATE TABLE IF NOT EXISTS app_data (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "data_key VARCHAR(100) NOT NULL," +
                    "data_value TEXT," +
                    "data_type VARCHAR(50)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";

            stmt.execute(createAppDataTableSQL);
            
            // 创建plc_info表
            String createPlcInfoTableSQL = "CREATE TABLE IF NOT EXISTS plc_info (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "ip_addr TEXT NOT NULL," +
                    "port_no INTEGER NOT NULL" +
                    ");";

            stmt.execute(createPlcInfoTableSQL);

            try {
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_plc_info_ip_port ON plc_info(ip_addr, port_no)");
            } catch (SQLException e) {
                logger.warn("创建 plc_info 唯一索引失败，可能存在重复数据：{}", e.getMessage());
            }
            
            // 创建plc_addr表 (已移除detail字段)
            String createPlcAddrTableSQL = "CREATE TABLE IF NOT EXISTS plc_addr (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "plc_no INTEGER NOT NULL DEFAULT 0," +
                    "name TEXT NOT NULL DEFAULT ''," +
                    "type INTEGER NOT NULL DEFAULT 0," +
                    "plc_info_id INTEGER NOT NULL" +
                    ");";

            stmt.execute(createPlcAddrTableSQL);

            try {
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_plc_addr_plc_info_id_plc_no ON plc_addr(plc_info_id, plc_no)");
            } catch (SQLException e) {
                logger.warn("创建 plc_addr 唯一索引失败，可能存在重复数据：{}", e.getMessage());
            }
            
            // 创建plc_run表
            String createPlcRunTableSQL = "CREATE TABLE IF NOT EXISTS plc_run (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL" +
                    ");";

            stmt.execute(createPlcRunTableSQL);
            migratePlcRunTableIfNeeded(stmt);

            try {
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_plc_run_name ON plc_run(name)");
            } catch (SQLException e) {
                logger.warn("创建 plc_run 名称唯一索引失败，可能存在重复数据：{}", e.getMessage());
            }
            
            // 创建plc_run_detail表并添加新字段
            String createPlcRunDetailTableSQL = "CREATE TABLE IF NOT EXISTS plc_run_detail (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "plc_run_id INTEGER NOT NULL," +
                    "plc_addr_id INTEGER NOT NULL," +
                    "sort_no INTEGER NOT NULL," +
                    "plc_value INTEGER NOT NULL DEFAULT 0," +  // 新增字段，默认值为0
                    "time_out_second INTEGER NOT NULL DEFAULT 60" +  // 新增字段，默认值为60
                    ");";

            stmt.execute(createPlcRunDetailTableSQL);
            logger.info("数据库表初始化完成");

        } catch (SQLException e) {
            logger.error("初始化数据库表失败", e);
        }
    }

    private void migratePlcRunTableIfNeeded(Statement stmt) throws SQLException {
        boolean hasDetail = false;
        boolean hasPlcAddrIds = false;

        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(plc_run)")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if ("detail".equalsIgnoreCase(name)) {
                    hasDetail = true;
                }
                if ("plc_addr_ids".equalsIgnoreCase(name)) {
                    hasPlcAddrIds = true;
                }
            }
        }

        if (!hasDetail && !hasPlcAddrIds) {
            return;
        }

        stmt.execute("DROP TABLE IF EXISTS plc_run_old");
        stmt.execute("ALTER TABLE plc_run RENAME TO plc_run_old");
        stmt.execute("CREATE TABLE plc_run (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)");
        stmt.execute("INSERT INTO plc_run (id, name) SELECT id, name FROM plc_run_old");
        stmt.execute("DROP TABLE plc_run_old");
    }
}