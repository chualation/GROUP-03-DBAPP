import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;

public class MovementPanel extends JPanel {
    private final ReportPanel reportPanel;

    private final JTable table = new JTable();
    private final DefaultTableModel model;

    private final JComboBox<Item> cbProduct  = new JComboBox<>();
    private final JComboBox<Item> cbLocation = new JComboBox<>();
    private final JComboBox<Item> cbSupplier = new JComboBox<>();
    private final JTextField tfQty   = new JTextField();
    private final JComboBox<String> cbType =
            new JComboBox<>(new String[]{"IN","OUT"});
    private final JTextField tfDate  = new JTextField(LocalDate.now().toString());
    private final JTextField tfReason = new JTextField();

    public MovementPanel(ReportPanel rp){
        this.reportPanel = rp;
        setLayout(new BorderLayout(10,10));

        // ====== TABLE MODEL (LOCKED) ======
        model = new DefaultTableModel(new String[]{
                "movement_id","product","location","supplier",
                "quantity","type","date","reason"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // lock all cells; movements are added via form only
                return false;
            }
        };
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null); // extra safety

        // ====== STYLING ======
        table.setRowHeight(30);
        table.setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 28));

        // Center movement_id column
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ====== FORM (boxes remain editable) ======
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createTitledBorder("Add Stock Movement"));
        form.add(new JLabel("Product:"));              form.add(cbProduct);
        form.add(new JLabel("Location:"));             form.add(cbLocation);
        form.add(new JLabel("Supplier (optional):"));  form.add(cbSupplier);
        form.add(new JLabel("Quantity:"));             form.add(tfQty);
        form.add(new JLabel("Type:"));                 form.add(cbType);
        form.add(new JLabel("Date (YYYY-MM-DD):"));    form.add(tfDate);
        form.add(new JLabel("Reason:"));               form.add(tfReason);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Add");
        buttons.add(btnAdd);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // ====== CLICK LOGIC (same behavior as ProductPanel) ======
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedRow  = table.rowAtPoint(e.getPoint());
                int selectedRow = table.getSelectedRow();

                // DOUBLE CLICK on a row → select + fill form
                if (e.getClickCount() == 2 && clickedRow >= 0) {
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    fillFormFromRow(clickedRow);
                    return;
                }

                // SINGLE CLICK
                if (e.getClickCount() == 1) {
                    if (clickedRow < 0) {
                        // click outside rows → ignore
                        return;
                    }

                    // Clicked the SAME selected row → unselect + clear
                    if (clickedRow == selectedRow) {
                        table.clearSelection();
                        clearForm();
                        return;
                    }

                    // Clicked a different row → just select it
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    // (we only fill form on double-click, to match ProductPanel)
                }
            }
        });

        // ====== BUTTON ACTIONS ======
        btnAdd.addActionListener(e -> addMovement());

        loadComboData();
        loadMovements();
        clearForm();
    }

    // --- helpers for filling / clearing form ---

    private void clearForm() {
        cbProduct.setSelectedIndex(cbProduct.getItemCount() > 0 ? 0 : -1);
        cbLocation.setSelectedIndex(cbLocation.getItemCount() > 0 ? 0 : -1);
        cbSupplier.setSelectedIndex(cbSupplier.getItemCount() > 0 ? 0 : -1);
        tfQty.setText("");
        cbType.setSelectedIndex(0);
        tfDate.setText(LocalDate.now().toString());
        tfReason.setText("");
    }

    private void fillFormFromRow(int row) {
        String prodName  = (String) model.getValueAt(row, 1);
        String locName   = (String) model.getValueAt(row, 2);
        String suppName  = (String) model.getValueAt(row, 3);
        Object qtyObj    = model.getValueAt(row, 4);
        String type      = (String) model.getValueAt(row, 5);
        Object dateObj   = model.getValueAt(row, 6);
        String reason    = (String) model.getValueAt(row, 7);

        // match combobox item by name
        selectComboItemByName(cbProduct, prodName);
        selectComboItemByName(cbLocation, locName);

        if (suppName == null || suppName.isEmpty()) {
            // no supplier → select "(none)"
            selectComboItemByName(cbSupplier, "(none)");
        } else {
            selectComboItemByName(cbSupplier, suppName);
        }

        tfQty.setText(qtyObj == null ? "" : qtyObj.toString());
        cbType.setSelectedItem(type);
        tfDate.setText(dateObj == null ? "" : dateObj.toString());
        tfReason.setText(reason == null ? "" : reason);
    }

    private void selectComboItemByName(JComboBox<Item> combo, String name) {
        if (name == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            Item it = combo.getItemAt(i);
            if (name.equals(it.name)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // --- DB loading ---

    private void loadComboData(){
        cbProduct.removeAllItems();
        cbLocation.removeAllItems();
        cbSupplier.removeAllItems();
        try(Connection c = DBUtils.getConn()){
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT product_id, product_name FROM Product ORDER BY product_name");
            while(rs.next()) cbProduct.addItem(new Item(rs.getInt(1), rs.getString(2)));

            rs = c.createStatement().executeQuery(
                    "SELECT location_id, location_name FROM StorageLocation ORDER BY location_name");
            while(rs.next()) cbLocation.addItem(new Item(rs.getInt(1), rs.getString(2)));

            cbSupplier.addItem(new Item(0, "(none)"));
            rs = c.createStatement().executeQuery(
                    "SELECT supplier_id, supplier_name FROM Supplier ORDER BY supplier_name");
            while (rs.next()) cbSupplier.addItem(new Item(rs.getInt(1), rs.getString(2)));

        }catch (SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void loadMovements(){
        model.setRowCount(0);
        String sql = "SELECT m.movement_id, p.product_name, l.location_name, s.supplier_name, " +
                "m.quantity, m.movement_type, m.movement_date, m.reason " +
                "FROM StockMovement m " +
                "LEFT JOIN Product p ON m.product_id = p.product_id " +
                "LEFT JOIN StorageLocation l ON m.location_id = l.location_id " +
                "LEFT JOIN Supplier s ON m.supplier_id = s.supplier_id " +
                "ORDER BY m.movement_date DESC, m.movement_id DESC";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getBigDecimal(5),
                        rs.getString(6),
                        rs.getDate(7),
                        rs.getString(8)
                });
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void addMovement(){
        // Input validation
        if(!DBUtils.validateNumber(tfQty.getText().trim(), "Quantity", this)) return;
        if(!DBUtils.validateText(tfReason.getText(), "Reason", this)) return;
        if(!DBUtils.validateDate(tfDate.getText(), "Date", this)) return;
        
        if(JOptionPane.showConfirmDialog(this, "Add this movement?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

        String sql = "INSERT INTO StockMovement " +
                "(product_id, location_id, supplier_id, quantity, " +
                " movement_type, movement_date, reason) " +
                "VALUES (?,?,?,?,?,?,?)";

        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, ((Item) cbProduct.getSelectedItem()).id);
            ps.setInt(2, ((Item) cbLocation.getSelectedItem()).id);

            int supId = ((Item) cbSupplier.getSelectedItem()).id;
            if(supId == 0){
                ps.setNull(3, Types.INTEGER);
            }else{
                ps.setInt(3, supId);
            }

            ps.setBigDecimal(4, DBUtils.toDecimal(tfQty.getText()));
            ps.setString(5, (String) cbType.getSelectedItem());
            ps.setDate(6, Date.valueOf(tfDate.getText().trim()));
            ps.setString(7, tfReason.getText().trim());
            ps.executeUpdate();

            loadMovements();
            reportPanel.refresh(); // auto-refresh report
            clearForm();
            table.clearSelection();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    public static class Item{
        public int id;
        public String name;
        public Item(int i, String n){
            id = i; name = n;
        }
        public String toString(){
            return name;
        }
    }
}

