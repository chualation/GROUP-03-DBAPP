import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class LocationPanel extends JPanel {
    private final JTable table = new JTable();
    private final DefaultTableModel model;
    private final JTextField tfName = new JTextField();
    private final JTextField tfAreaDesc = new JTextField();
    private final JTextField tfCapacity = new JTextField("0");
    private final JComboBox<String> cbTemp = new JComboBox<>(new String[]{"None","Refrigerated","Freezer"});
    private final JComboBox<String> cbFilter = new JComboBox<>(); //new: filter dropdown

    public LocationPanel() {
        setLayout(new BorderLayout(10,10));

        // ====== TOP PANEL FOR FILTER ===== //new
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Filter by Location:")); //new
        topPanel.add(cbFilter); //new
        add(topPanel, BorderLayout.NORTH); //new

        // ====== TABLE MODEL (LOCKED) ======
        model = new DefaultTableModel(new String[]{
                "location_id","location_name","area_description","capacity","temperature_control"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);

        // Styling (optional but cleaner)
        table.setRowHeight(30);
        table.setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 28));

        // Center ID column
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ====== FORM AREA (boxes remain editable) ======
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createTitledBorder("Add / Update Location"));
        form.add(new JLabel("Name:"));                 form.add(tfName);
        form.add(new JLabel("Area Description:"));     form.add(tfAreaDesc);
        form.add(new JLabel("Capacity:"));             form.add(tfCapacity);
        form.add(new JLabel("Temperature Control:"));  form.add(cbTemp);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd    = new JButton("Add");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // ====== BUTTON ACTIONS ======
        btnAdd.addActionListener(e -> addLocation());
        btnUpdate.addActionListener(e -> updateSelectedLocation());
        btnDelete.addActionListener(e -> deleteSelectedLocation());

        // ====== CLICK LOGIC (same as Product panel) ======
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedRow  = table.rowAtPoint(e.getPoint());
                int selectedRow = table.getSelectedRow();
                if (e.getClickCount() == 2 && clickedRow >= 0) {
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    fillFormFromRow(clickedRow);
                    return;
                }
                if (e.getClickCount() == 1) {
                    if (clickedRow < 0) return;
                    if (clickedRow == selectedRow) {
                        table.clearSelection();
                        clearForm();
                        return;
                    }
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                }
            }
        });

        loadLocations();
        loadFilterOptions(); //new: populate filter dropdown

        // ====== FILTER ACTION ===== //new
        cbFilter.addActionListener(e -> {
            String selected = (String) cbFilter.getSelectedItem();
            if(selected == null || selected.equals("None")){
                loadLocations();
            }else{
                showProductsInLocation(selected);
            }
        });
    }

    private void fillFormFromRow(int row) {
        tfName.setText(model.getValueAt(row, 1).toString());
        tfAreaDesc.setText(model.getValueAt(row, 2).toString());
        tfCapacity.setText(model.getValueAt(row, 3).toString());
        cbTemp.setSelectedItem(model.getValueAt(row, 4).toString());
    }

    private void loadLocations(){
        model.setRowCount(0);
        String sql = "SELECT location_id, location_name, area_description, capacity, temperature_control " +
                "FROM StorageLocation ORDER BY location_name";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getBigDecimal(4),
                        rs.getString(5)
                });
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    //new: populate filter dropdown
    private void loadFilterOptions(){
        cbFilter.removeAllItems();
        cbFilter.addItem("None");
        String sql = "SELECT location_name FROM StorageLocation ORDER BY location_name";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                cbFilter.addItem(rs.getString(1));
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    //new: show products in selected location
    private void showProductsInLocation(String locationName){
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to see all products in \""+locationName+"\"?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        // Popup dialog
        JDialog dialog = new JDialog((Frame)null,"Products in "+locationName,true);
        DefaultTableModel prodModel = new DefaultTableModel(new String[]{
                "Product ID","Name","Description","UoM"
        },0){
            @Override
            public boolean isCellEditable(int row, int column){ return false; }
        };
        JTable prodTable = new JTable(prodModel);
        prodTable.setRowHeight(25);
        prodTable.setBackground(Color.WHITE);
        prodTable.setShowGrid(false);
        prodTable.setShowHorizontalLines(false);
        prodTable.setShowVerticalLines(false);
        // Center all columns //new
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0;i<prodTable.getColumnCount();i++){
            prodTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        String sql = "SELECT product_id, product_name, description, unit_of_measure " +
                "FROM Product p " +
                "JOIN StorageLocation l ON p.location_id = l.location_id " +
                "WHERE l.location_name = ? ORDER BY product_name"; //new: only from Product.location_id
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,locationName);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                prodModel.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                });
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }

        dialog.add(new JScrollPane(prodTable));
        dialog.setSize(600,400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addLocation(){
        if(JOptionPane.showConfirmDialog(this,"Add this location?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        String sql = "INSERT INTO StorageLocation " +
                "(location_name, area_description, capacity, temperature_control) " +
                "VALUES (?,?,?,?)";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfAreaDesc.getText().trim());
            ps.setBigDecimal(3, DBUtils.toDecimal(tfCapacity.getText()));
            ps.setString(4, (String) cbTemp.getSelectedItem());
            ps.executeUpdate();
            loadLocations();
            loadFilterOptions(); //new: refresh filter after adding
            clearForm();
            table.clearSelection();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedLocation(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a location first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this,"Update location #"+id+"?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        String sql = "UPDATE StorageLocation SET location_name=?, area_description=?, " +
                "capacity=?, temperature_control=? WHERE location_id=?";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfAreaDesc.getText().trim());
            ps.setBigDecimal(3, DBUtils.toDecimal(tfCapacity.getText()));
            ps.setString(4, (String) cbTemp.getSelectedItem());
            ps.setInt(5, id);
            ps.executeUpdate();
            loadLocations();
            loadFilterOptions(); //new: refresh filter after update
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedLocation(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a location first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this,"Delete location #"+id+"?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM StorageLocation WHERE location_id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
            loadLocations();
            loadFilterOptions(); //new: refresh filter after delete
            table.clearSelection();
            clearForm();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void clearForm(){
        tfName.setText("");
        tfAreaDesc.setText("");
        tfCapacity.setText("0");
        cbTemp.setSelectedIndex(0);
    }
}
