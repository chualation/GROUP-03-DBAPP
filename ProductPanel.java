import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductPanel extends JPanel{
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTextField tfName = new JTextField();
    private final JTextField tfDesc = new JTextField();
    private final JTextField tfCategory = new JTextField();
    private final JTextField tfUom = new JTextField();
    private final JTextField tfReorder = new JTextField("0");
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    private final JButton btnLoad = new JButton("Load");
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update Selected");
    private final JButton btnDelete = new JButton("Delete Selected");

    public ProductPanel(){
        setLayout(new BorderLayout(10, 10));

        model.setColumnIdentifiers(new String[]{
                "product_id", "product_name", "description", "category",
                "unit_of_measure", "reorder_level", "product_status"
        });
        table.setModel(model);

        table.setDefaultEditor(Object.class, null);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Add / Update Product"));
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Description:")); form.add(tfDesc);
        form.add(new JLabel("Category:")); form.add(tfCategory);
        form.add(new JLabel("Unit of Measure:")); form.add(tfUom);
        form.add(new JLabel("Reorder Level:")); form.add(tfReorder);
        form.add(new JLabel("Status:")); form.add(cbStatus);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnLoad);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> loadProducts());
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateSelectedProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        loadProducts();
    }

    private void loadProducts(){
        model.setRowCount(0);
        String sql = "SELECT product_id, product_name, description, category, " + "unit_of_measure, reorder_level, product_status " + "FROM Product ORDER BY product_name";
        try(Connection c = DBUtils.getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBigDecimal(6),
                        rs.getString(7)
                });
            }
        } catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void addProduct(){
        if(JOptionPane.showConfirmDialog(this, "Add this product?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO Product " + "(product_name, description, category, unit_of_measure, reorder_level, product_status) " + "VALUES (?,?,?,?,?,?)";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfDesc.getText().trim());
                ps.setString(3, tfCategory.getText().trim());
                ps.setString(4, tfUom.getText().trim());
                ps.setBigDecimal(5, DBUtils.toDecimal(tfReorder.getText()));
                ps.setString(6, (String) cbStatus.getSelectedItem());
                ps.executeUpdate();
                loadProducts();
                clearForm();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedProduct(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a product first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this, "Update product #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE Product SET product_name=?, description=?, category=?, " + "unit_of_measure=?, reorder_level=?, product_status=? WHERE product_id=?";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfDesc.getText().trim());
                ps.setString(3, tfCategory.getText().trim());
                ps.setString(4, tfUom.getText().trim());
                ps.setBigDecimal(5, DBUtils.toDecimal(tfReorder.getText()));
                ps.setString(6, (String) cbStatus.getSelectedItem());
                ps.setInt(7, id);
                ps.executeUpdate();
                loadProducts();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedProduct(){
        int row = table.getSelectedRow();
        if(row < 0){ 
            DBUtils.info("Select a product first."); 
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this, "Delete product #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement("DELETE FROM Product WHERE product_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadProducts();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void clearForm(){
        tfName.setText("");
        tfDesc.setText("");
        tfCategory.setText("");
        tfUom.setText("");
        tfReorder.setText("0");
        cbStatus.setSelectedIndex(0);
    }
}
