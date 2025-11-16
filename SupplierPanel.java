import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class SupplierPanel extends JPanel {
    private final JTable table = new JTable();
    private final DefaultTableModel model;

    private final JTextField tfName           = new JTextField();
    private final JTextField tfContactPerson  = new JTextField();
    private final JTextField tfContactNo      = new JTextField();
    private final JTextField tfEmail          = new JTextField();
    private final JTextField tfAddress        = new JTextField();
    private final JComboBox<String> cbStatus  = new JComboBox<>(new String[]{"Active","Inactive"});

    public SupplierPanel(){
        setLayout(new BorderLayout(10,10));

        // ===== TABLE MODEL (LOCKED) =====
        model = new DefaultTableModel(new String[]{
                "supplier_id","supplier_name","contact_person",
                "contact_number","email","address","supplier_status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // lock all table cells; editing only via form
                return false;
            }
        };
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null); // extra safety

        // styling (optional but consistent with others)
        table.setRowHeight(30);
        table.setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 28));

        // center supplier_id column
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        add(new JScrollPane(table),BorderLayout.CENTER);

        // ===== FORM (boxes stay editable) =====
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createTitledBorder("Add / Update Supplier"));
        form.add(new JLabel("Name:"));            form.add(tfName);
        form.add(new JLabel("Contact Person:"));  form.add(tfContactPerson);
        form.add(new JLabel("Contact Number:"));  form.add(tfContactNo);
        form.add(new JLabel("Email:"));           form.add(tfEmail);
        form.add(new JLabel("Address:"));         form.add(tfAddress);
        form.add(new JLabel("Status:"));          form.add(cbStatus);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd    = new JButton("Add");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form,BorderLayout.CENTER);
        south.add(buttons,BorderLayout.SOUTH);
        add(south,BorderLayout.SOUTH);

        // ===== BUTTON ACTIONS =====
        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSelectedSupplier());
        btnDelete.addActionListener(e -> deleteSelectedSupplier());

        // ===== CLICK LOGIC (same as ProductPanel) =====
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedRow  = table.rowAtPoint(e.getPoint());
                int selectedRow = table.getSelectedRow();

                // DOUBLE CLICK → select and load into form
                if (e.getClickCount() == 2 && clickedRow >= 0) {
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    fillFormFromRow(clickedRow);
                    return;
                }

                // SINGLE CLICK
                if (e.getClickCount() == 1) {
                    if (clickedRow < 0) {
                        // clicked outside rows → ignore
                        return;
                    }

                    // click same selected row → unselect + clear
                    if (clickedRow == selectedRow) {
                        table.clearSelection();
                        clearForm();
                        return;
                    }

                    // click different row → just select
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    // (we only fill form on double-click to match ProductPanel)
                }
            }
        });

        loadSuppliers();
    }

    private void fillFormFromRow(int row) {
        tfName.setText(model.getValueAt(row, 1).toString());
        tfContactPerson.setText(model.getValueAt(row, 2).toString());
        tfContactNo.setText(model.getValueAt(row, 3).toString());
        tfEmail.setText(model.getValueAt(row, 4).toString());
        tfAddress.setText(model.getValueAt(row, 5).toString());
        cbStatus.setSelectedItem(model.getValueAt(row, 6).toString());
    }

    private void loadSuppliers(){
        model.setRowCount(0);
        String sql = "SELECT supplier_id,supplier_name,contact_person,contact_number," +
                "email,address,supplier_status " +
                "FROM Supplier ORDER BY supplier_name";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)
                });
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void addSupplier(){
        if(JOptionPane.showConfirmDialog(this,"Add this supplier?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        String sql = "INSERT INTO Supplier " +
                "(supplier_name, contact_person, contact_number, email, address, supplier_status) " +
                "VALUES (?,?,?,?,?,?)";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,tfName.getText().trim());
            ps.setString(2,tfContactPerson.getText().trim());
            ps.setString(3,tfContactNo.getText().trim());
            ps.setString(4,tfEmail.getText().trim());
            ps.setString(5,tfAddress.getText().trim());
            ps.setString(6,(String)cbStatus.getSelectedItem());
            ps.executeUpdate();
            loadSuppliers();
            clearForm();
            table.clearSelection();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedSupplier(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a supplier first.");
            return;
        }

        int id = (int)model.getValueAt(row,0);
        if(JOptionPane.showConfirmDialog(this,"Update supplier #"+id+"?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        String sql = "UPDATE Supplier SET supplier_name=?, contact_person=?, contact_number=?, " +
                "email=?, address=?, supplier_status=? WHERE supplier_id=?";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,tfName.getText().trim());
            ps.setString(2,tfContactPerson.getText().trim());
            ps.setString(3,tfContactNo.getText().trim());
            ps.setString(4,tfEmail.getText().trim());
            ps.setString(5,tfAddress.getText().trim());
            ps.setString(6,(String)cbStatus.getSelectedItem());
            ps.setInt(7,id);
            ps.executeUpdate();
            loadSuppliers();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedSupplier(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a supplier first.");
            return;
        }

        int id = (int)model.getValueAt(row,0);
        if(JOptionPane.showConfirmDialog(this,"Delete supplier #"+id+"?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement("DELETE FROM Supplier WHERE supplier_id=?")){
            ps.setInt(1,id);
            ps.executeUpdate();
            loadSuppliers();
            table.clearSelection();
            clearForm();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void clearForm(){
        tfName.setText("");
        tfContactPerson.setText("");
        tfContactNo.setText("");
        tfEmail.setText("");
        tfAddress.setText("");
        cbStatus.setSelectedIndex(0);
    }
}
