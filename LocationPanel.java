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

    public LocationPanel() {
        setLayout(new BorderLayout(10,10));

        // ====== TABLE MODEL (LOCKED) ======
        model = new DefaultTableModel(new String[]{
                "location_id","location_name","area_description","capacity","temperature_control"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Lock all table cells; editing only via form below
                return false;
            }
        };
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null); // extra safety

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

                // DOUBLE CLICK on row → select & load into form
                if (e.getClickCount() == 2 && clickedRow >= 0) {
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    fillFormFromRow(clickedRow);
                    return;
                }

                // SINGLE CLICK behavior
                if (e.getClickCount() == 1) {
                    // Clicked outside rows → ignore (no change)
                    if (clickedRow < 0) {
                        return;
                    }

                    // Clicked the SAME selected row → unselect + clear form
                    if (clickedRow == selectedRow) {
                        table.clearSelection();
                        clearForm();
                        return;
                    }

                    // Clicked a DIFFERENT row → just select it
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    // (form only fills on double-click, same as ProductPanel)
                }
            }
        });

        loadLocations();
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
