import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * 데이터베이스 연결 및 자원 관리 유틸리티
 */
public class DBUtil {
    private static final String DB_PROPERTIES_FILE = "db.properties";
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    // 정적 블록에서 DB 설정 로드
    static {
        loadDatabaseConfig();
    }
    
    /**
     * DB 설정 파일 로드
     */
    private static void loadDatabaseConfig() {
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream(DB_PROPERTIES_FILE)) {
            props.load(fis);
            
            DB_URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/comic_rental?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true");
            DB_USERNAME = props.getProperty("db.username", "root");
            DB_PASSWORD = props.getProperty("db.password", "");
            DB_DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
        } catch (IOException e) {
            // 설정 파일이 없으면 기본값 사용 (MySQL)
            System.out.println("DB 설정 파일을 찾을 수 없습니다. MySQL 기본 설정을 사용합니다.");
            DB_URL = "jdbc:mysql://localhost:3306/comic_rental?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
            DB_USERNAME = "root";
            DB_PASSWORD = "";
            DB_DRIVER = "com.mysql.cj.jdbc.Driver";
        }
        
        // JDBC 드라이버 로드
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다: " + DB_DRIVER);
            System.err.println("MySQL JDBC 드라이버가 클래스패스에 있는지 확인해주세요.");
        }
    }
    
    /**
     * 데이터베이스 연결 가져오기
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
    
    /**
     * Connection 자원 해제
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Connection 해제 중 오류 발생: " + e.getMessage());
            }
        }
    }
    
    /**
     * PreparedStatement 자원 해제
     */
    public static void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("PreparedStatement 해제 중 오류 발생: " + e.getMessage());
            }
        }
    }
    
    /**
     * ResultSet 자원 해제
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("ResultSet 해제 중 오류 발생: " + e.getMessage());
            }
        }
    }
    
    /**
     * 모든 자원 해제 (Connection, PreparedStatement, ResultSet)
     */
    public static void closeAll(Connection conn, PreparedStatement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
    
    /**
     * Connection과 PreparedStatement 해제
     */
    public static void closeAll(Connection conn, PreparedStatement stmt) {
        closeStatement(stmt);
        closeConnection(conn);
    }
    
    /**
     * DB 연결 테스트
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 현재 날짜 가져오기 (yyyy-MM-dd 형식)
     */
    public static String getCurrentDate() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT CURRENT_DATE");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("현재 날짜 조회 오류: " + e.getMessage());
        }
        
        // 실패시 Java에서 현재 날짜 반환
        return java.time.LocalDate.now().toString();
    }
}