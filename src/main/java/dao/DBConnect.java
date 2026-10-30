package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DBConnect {
    private static String setting(Properties config, String env, String key, String fallback) {
        String value = System.getenv(env);
        return value != null ? value : config.getProperty(key, fallback);
    }

    // Hàm này sẽ cấp phát một Connection mỗi khi Servlet gọi đến
    public static Connection getConnection() throws Exception {
        Properties config = new Properties();
        Path path = Paths.get(System.getProperty("warehouse.config", "config.properties"));
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) { config.load(in); }
        }
        String url = setting(config, "SQLSERVER_URL", "sqlserver.url",
                "jdbc:sqlserver://localhost;databaseName=QuanLyKhoHang;encrypt=false;trustServerCertificate=true");
        String user = setting(config, "SQLSERVER_USERNAME", "sqlserver.username", "sa");
        String password = setting(config, "SQLSERVER_PASSWORD", "sqlserver.password", null);
        if (password == null) throw new IllegalStateException(
                "Set SQLSERVER_PASSWORD or sqlserver.password in local config.properties");
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, password);
    }
}
