    import javax.swing.*;
    import java.math.BigDecimal;
    import java.sql.*;
    import java.awt.Component;
    import javax.swing.JOptionPane;
    import java.time.LocalDate;

    public class DBUtils {
        // Database credentials
        private static final String DB_URL  = "jdbc:mysql://127.0.0.1:3306/CloudKitchenInventory_db?useSSL=false&serverTimezone=UTC";
        private static final String DB_USER = "root";
        private static final String DB_PASS = "12345678";

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

        /** Input validation for String (inputs that only contain symbols or numbers are not allowed) */
        public static boolean validateText(String text, String fieldName, Component parent) {
            if (text == null || text.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        fieldName + " cannot be empty.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Must contain at least one letter
            if (!text.matches(".*[A-Za-z].*")) {
                JOptionPane.showMessageDialog(parent,
                        fieldName + " must contain letters.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }

        /** Input validation for Numbers (inputs that contain letters or symbols are not allowed) */
        public static boolean validateNumber(String text, String fieldName, Component parent) {
            if (text == null || text.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        fieldName + " cannot be empty.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                float value = Float.parseFloat(text.trim());
                if (value < 0) {
                    JOptionPane.showMessageDialog(parent,
                            fieldName + " cannot be negative.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent,
                        fieldName + " must be a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        /** Input validation for Dates (format YYYY-MM-DD) */
        public static boolean validateDate(String text, String fieldName, Component parent) {
            if (text == null || text.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        fieldName + " cannot be empty.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                LocalDate.parse(text.trim());
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        fieldName + " must be a valid date in YYYY-MM-DD format.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
