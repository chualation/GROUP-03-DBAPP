import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class ReportPanel extends JPanel{
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();

    public ReportPanel(){
        setLayout(new BorderLayout());
        model.setColumnIdentifiers(new String[]{"Product","Stock","Reorder Level","Stock Status"});
        table.setModel(model);
        
        table.setDefaultEditor(Object.class, null);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if(!isSelected){ 
                    Object stockObj = table.getValueAt(row, 1);
                    Object reorderObj = table.getValueAt(row, 2);

                    Color outOfStock = Color.decode("#ea9899"); // Out of stock
                    Color lowStock   = Color.decode("#fee39a"); // Low
                    Color okStock    = Color.decode("#b7d7a8"); // OK

                    if(stockObj instanceof BigDecimal && reorderObj instanceof BigDecimal){
                        BigDecimal stock = (BigDecimal) stockObj;
                        BigDecimal reorder = (BigDecimal) reorderObj;

                        if(stock.compareTo(BigDecimal.ZERO) == 0){
                            c.setBackground(outOfStock);
                        }else if(stock.compareTo(reorder) < 0){
                            c.setBackground(lowStock);
                        }else{
                            c.setBackground(okStock);
                        }
                    }else{
                        c.setBackground(table.getBackground());
                    }
                }
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        loadReport();
    }

    public void loadReport() {
        model.setRowCount(0);
        String sql = "SELECT product_name," +
                " SUM(CASE WHEN movement_type='IN' THEN quantity ELSE -quantity END) AS stock," +
                " reorder_level " +
                "FROM Product p " +
                "LEFT JOIN StockMovement m ON p.product_id=m.product_id " +
                "GROUP BY product_name, reorder_level";
        try(Connection c = DBUtils.getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while(rs.next()){
                BigDecimal stock = rs.getBigDecimal("stock");
                if(stock == null) stock = BigDecimal.ZERO;
                BigDecimal reorder = rs.getBigDecimal("reorder_level");
                if(reorder == null) reorder = BigDecimal.ZERO;

                String status = (stock.compareTo(BigDecimal.ZERO) == 0) ? "OUT OF STOCK" : (stock.compareTo(reorder) < 0) ? "LOW" : "OK";

                model.addRow(new Object[]{rs.getString("product_name"), stock, reorder, status});
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    public void refresh(){ 
        loadReport();
    }
}
