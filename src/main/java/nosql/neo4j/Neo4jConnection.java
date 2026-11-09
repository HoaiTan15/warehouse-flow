package nosql.neo4j;

import org.neo4j.driver.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Neo4jConnection {
    private static Driver driver;

    private static String setting(Properties config, String env, String key, String fallback) {
        String value = System.getenv(env);
        return value != null ? value : config.getProperty(key, fallback);
    }

    public static synchronized Driver getDriver() {
        if (driver == null) {
            try {
                Properties config = new Properties();
                Path path = Paths.get(System.getProperty("warehouse.config", "config.properties"));
                if (Files.exists(path)) {
                    try (InputStream in = Files.newInputStream(path)) { config.load(in); }
                } else {
                    try (InputStream in = Neo4jConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
                        if (in != null) config.load(in);
                    } catch (Exception ignored) {}
                }

                String uri = setting(config, "NEO4J_URL", "neo4j.url", "bolt://localhost:7687");
                String user = setting(config, "NEO4J_USERNAME", "neo4j.username", "neo4j");
                String pass = setting(config, "NEO4J_PASSWORD", "neo4j.password", "12345678");

                Config driverConfig = Config.builder()
                        .withConnectionTimeout(3, TimeUnit.SECONDS)
                        .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                        .build();

                driver = GraphDatabase.driver(uri, AuthTokens.basic(user, pass), driverConfig);
                System.out.println("Neo4j driver initialized pointing to: " + uri);
            } catch (Exception e) {
                System.err.println("Failed to initialize Neo4j driver: " + e.getMessage());
                driver = null;
            }
        }
        return driver;
    }

    public static Session getSession() {
        try {
            Driver d = getDriver();
            if (d != null) {
                return d.session();
            }
        } catch (Exception e) {
            System.err.println("Failed to obtain Neo4j session: " + e.getMessage());
        }
        return null;
    }

    public static boolean isConnected() {
        try {
            Driver d = getDriver();
            if (d == null) return false;
            d.verifyConnectivity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void close() {
        if (driver != null) {
            try {
                driver.close();
                System.out.println("Neo4j driver closed.");
            } catch (Exception e) {
                System.err.println("Error closing Neo4j driver: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }
}

