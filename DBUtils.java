import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;

public class DBUtils {
    // Database credentials
    private static final String DB_URL  = "jdbc:mysql://127.0.0.1:3306/CloudKitchenInventory_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "hansisFrancis17*";

    /** Get a database connection */
    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /** Show error message for SQLException */
    public static void showErr(SQLException ex){
        JOptionPane.showMessageDialog(null, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    /** Show information message */
    public static void info(String msg){
        JOptionPane.showMessageDialog(null, msg);
    }

    /** Convert string to BigDecimal safely */
    public static BigDecimal toDecimal(String s){
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e){
            return BigDecimal.ZERO;
        }
    }
}
