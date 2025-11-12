import java.sql.*;
import java.math.BigDecimal;
import javax.swing.*;

public class DBUtils{
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/CloudKitchenInventory_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "021606";

    public static Connection getConn() throws SQLException{
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static BigDecimal toDecimal(String s){
        try{
            return new BigDecimal(s.trim());
        }

        catch(Exception e){ 
            return BigDecimal.ZERO;
        }
    }

    public static void info(String msg){
        JOptionPane.showMessageDialog(null,msg);
    }

    public static void showErr(SQLException ex){
        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static class Item{
        public int id; public String name;
        public Item(int i, String n){
            id=i; name=n;
        }
        public String toString(){
            return name;
        }
    }
}
