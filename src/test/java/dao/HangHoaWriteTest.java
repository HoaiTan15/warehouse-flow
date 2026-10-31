package dao;

import controller.QuanLyHangHoaServlet;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HangHoa;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class HangHoaWriteTest {
    private static class FakeDAO extends HangHoaDAO {
        HangHoa saved;
        boolean result = true;
        String action;

        FakeDAO() { super(null); }

        @Override public boolean themHangHoa(HangHoa hh) {
            saved = hh;
            action = "add";
            return result;
        }

        @Override public boolean suaHangHoa(HangHoa hh) {
            saved = hh;
            action = "edit";
            return result;
        }
    }

    private static class TestServlet extends QuanLyHangHoaServlet {
        void post(HttpServletRequest request, HttpServletResponse response) throws Exception {
            doPost(request, response);
        }
    }

    private Map<String, String> form(String action) {
        Map<String, String> params = new HashMap<>();
        params.put("action", action);
        params.put("maHang", "edit".equals(action) ? "HH01" : "");
        params.put("tenHang", "Sản phẩm mới");
        params.put("dvt", "Chai");
        params.put("soLuongTonKho", "25");
        params.put("hanMucTonToiThieu", "10");
        params.put("quyCach", "500ml/chai");
        params.put("maLoai", "L01");
        params.put("maViTri", "VT01");
        return params;
    }

    private String post(Map<String, String> params, FakeDAO dao) throws Exception {
        TestServlet servlet = new TestServlet();
        Field field = QuanLyHangHoaServlet.class.getDeclaredField("hangHoaDAO");
        field.setAccessible(true);
        field.set(servlet, dao);
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName())) return params.get(args[0]);
                    if ("setCharacterEncoding".equals(method.getName())) return null;
                    throw new AssertionError("Unexpected request call: " + method.getName());
                });
        String[] redirect = new String[1];
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("sendRedirect".equals(method.getName())) redirect[0] = (String) args[0];
                    else if (!"setCharacterEncoding".equals(method.getName()))
                        throw new AssertionError("Unexpected response call: " + method.getName());
                    return null;
                });
        servlet.post(request, response);
        return redirect[0];
    }

    @Test public void addAndEditUseFieldNamesSubmittedByTheForm() throws Exception {
        for (String action : new String[]{"add", "edit"}) {
            FakeDAO dao = new FakeDAO();
            assertEquals("LoadDanhSachKhoServlet?success=" + action, post(form(action), dao));
            assertEquals(action, dao.action);
            assertEquals(25, dao.saved.getSoLuongTonKho());
            assertEquals(10, dao.saved.getHanMucTonToiThieu());
            assertEquals("Sản phẩm mới", dao.saved.getTenHang());
            assertEquals("L01", dao.saved.getMaLoai());
            assertEquals("VT01", dao.saved.getMaViTri());
            if ("edit".equals(action)) assertEquals("HH01", dao.saved.getMaHang());
        }
    }

    @Test public void optionalInitialStockDefaultsToZero() throws Exception {
        for (String stock : new String[]{null, "", "  "}) {
            Map<String, String> params = form("add");
            params.put("soLuongTonKho", stock);
            FakeDAO dao = new FakeDAO();
            assertEquals("LoadDanhSachKhoServlet?success=add", post(params, dao));
            assertEquals(0, dao.saved.getSoLuongTonKho());
        }
    }

    @Test public void invalidNumbersDoNotWriteToDatabase() throws Exception {
        for (String action : new String[]{"add", "edit"}) {
            for (String field : new String[]{"soLuongTonKho", "hanMucTonToiThieu"}) {
                for (String invalid : new String[]{"-1", "1.5", "abc", "2147483648"}) {
                    Map<String, String> params = form(action);
                    params.put(field, invalid);
                    FakeDAO dao = new FakeDAO();
                    assertEquals("LoadDanhSachKhoServlet?error=invalid", post(params, dao));
                    assertNull(dao.saved);
                }
            }
        }
    }

    @Test public void missingRequiredNumbersDoNotWriteToDatabase() throws Exception {
        for (String action : new String[]{"add", "edit"}) {
            Map<String, String> params = form(action);
            params.remove("hanMucTonToiThieu");
            FakeDAO dao = new FakeDAO();
            assertEquals("LoadDanhSachKhoServlet?error=invalid", post(params, dao));
            assertNull(dao.saved);
        }
        Map<String, String> params = form("edit");
        params.remove("soLuongTonKho");
        FakeDAO dao = new FakeDAO();
        assertEquals("LoadDanhSachKhoServlet?error=invalid", post(params, dao));
        assertNull(dao.saved);
    }

    @Test public void databaseFailureDoesNotReportSuccess() throws Exception {
        for (String action : new String[]{"add", "edit"}) {
            FakeDAO dao = new FakeDAO();
            dao.result = false;
            assertEquals("LoadDanhSachKhoServlet?error=" + action, post(form(action), dao));
        }
    }

    // A JDBC driver that captures the actual INSERT without accessing external services.
    private static class RecordingDriver implements Driver {
        Map<Integer, Object> values = new HashMap<>();
        String sql;
        boolean fail;
        boolean rolledBack;
        String[] codes = {"SP01", "SP02", "SP03", "SP04", "HH1c7f80a19c39464185"};

        @Override public Connection connect(String url, Properties properties) {
            if (!acceptsURL(url)) return null;
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                        if ("prepareStatement".equals(method.getName())) {
                            if (((String) args[0]).startsWith("INSERT")) sql = (String) args[0];
                            return Proxy.newProxyInstance(getClass().getClassLoader(),
                                    new Class<?>[]{PreparedStatement.class}, (ps, operation, parameters) -> {
                                        switch (operation.getName()) {
                                            case "executeQuery":
                                                int[] row = {-1};
                                                return Proxy.newProxyInstance(getClass().getClassLoader(),
                                                        new Class<?>[]{ResultSet.class}, (rs, read, columns) -> {
                                                            switch (read.getName()) {
                                                                case "next": return ++row[0] < codes.length;
                                                                case "getString": return codes[row[0]];
                                                                case "close": return null;
                                                                default: throw new AssertionError(read.getName());
                                                            }
                                                        });
                                            case "setString": case "setInt":
                                                values.put((Integer) parameters[0], parameters[1]);
                                                return null;
                                            case "executeUpdate":
                                                if (fail) throw new SQLException("Simulated insert failure");
                                                return 0;
                                            case "close": return null;
                                            default: throw new AssertionError(operation.getName());
                                        }
                                    });
                        }
                        if ("rollback".equals(method.getName())) { rolledBack = true; return null; }
                        if ("close".equals(method.getName()) || "setAutoCommit".equals(method.getName())) return null;
                        throw new AssertionError(method.getName());
                    });
        }

        @Override public boolean acceptsURL(String url) { return "jdbc:warehouse-write-test".equals(url); }
        @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties props) { return new DriverPropertyInfo[0]; }
        @Override public int getMajorVersion() { return 1; }
        @Override public int getMinorVersion() { return 0; }
        @Override public boolean jdbcCompliant() { return false; }
        @Override public Logger getParentLogger() { return Logger.getGlobal(); }
    }

    @Test public void productCodesFollowExistingSpNumbering() throws Exception {
        RecordingDriver driver = new RecordingDriver();
        Connection conn = driver.connect("jdbc:warehouse-write-test", new Properties());
        assertEquals("SP05", HangHoaDAO.taoMaHang(conn));
        driver.codes = new String[0];
        assertEquals("SP01", HangHoaDAO.taoMaHang(conn));
        driver.codes = new String[]{"SP09", "SP02", "SP-invalid", "HHabc"};
        assertEquals("SP10", HangHoaDAO.taoMaHang(conn));
        driver.codes = new String[]{"SP99", "SP10", "SP01"};
        assertEquals("SP100", HangHoaDAO.taoMaHang(conn));
        driver.codes = new String[]{"SP100", "SP99"};
        assertEquals("SP101", HangHoaDAO.taoMaHang(conn));
    }

    @Test public void insertSuppliesGeneratedPrimaryKeyAndReportsSqlFailure() throws Exception {
        assumeTrue("Test uses an isolated JDBC URL", System.getenv("SQLSERVER_URL") == null);
        Path config = Files.createTempFile("warehouse-write-test", ".properties");
        String previous = System.getProperty("warehouse.config");
        RecordingDriver driver = new RecordingDriver();
        DriverManager.registerDriver(driver);
        try {
            Properties settings = new Properties();
            settings.setProperty("sqlserver.url", "jdbc:warehouse-write-test");
            settings.setProperty("sqlserver.password", "test");
            try (java.io.OutputStream out = Files.newOutputStream(config)) { settings.store(out, null); }
            System.setProperty("warehouse.config", config.toString());
            HangHoaDAO dao = new HangHoaDAO(null);
            HangHoa hh = new HangHoa(null, "Sản phẩm mới", "Chai", 25, "500ml", 10, "L01", "VT01");
            assertFalse(dao.themHangHoa(hh));
            assertTrue(driver.sql.startsWith("INSERT INTO HangHoa (MaHang,"));
            String firstCode = (String) driver.values.get(1);
            assertEquals("SP05", firstCode);
            assertTrue(driver.rolledBack);
            assertEquals(hh.getTenHang(), driver.values.get(2));
            assertEquals(hh.getDvt(), driver.values.get(3));
            assertEquals(25, driver.values.get(4));
            assertEquals(hh.getQuyCach(), driver.values.get(5));
            assertEquals(10, driver.values.get(6));
            assertEquals("L01", driver.values.get(7));
            assertEquals("VT01", driver.values.get(8));
            assertNull(hh.getMaHang());
            driver.fail = true;
            assertFalse(dao.themHangHoa(hh));
            assertEquals(firstCode, driver.values.get(1));
        } finally {
            if (previous == null) System.clearProperty("warehouse.config");
            else System.setProperty("warehouse.config", previous);
            DriverManager.deregisterDriver(driver);
            Files.deleteIfExists(config);
        }
    }
}
