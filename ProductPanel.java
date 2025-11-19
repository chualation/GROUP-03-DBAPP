import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Products tab
 *  - Table is read-only (view only)
 *  - DOUBLE-CLICK a row = select it and load data into form
 *  - SINGLE-CLICK the same highlighted row = unselect + clear form
 *  - Add / Update / Delete done via the form and buttons
 *
 * (Extended: now also displays supplier + storage location, and form includes dropdowns)
 */
public class ProductPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;

    // Form fields
    private final RoundedTextField tfName     = new RoundedTextField(10);
    private final RoundedTextField tfDesc     = new RoundedTextField(10);
    private final RoundedTextField tfCategory = new RoundedTextField(10);
    private final RoundedTextField tfUom      = new RoundedTextField(10);
    private final RoundedTextField tfReorder  = new RoundedTextField("0", 10);
    private final JComboBox<String> cbStatus  = new JComboBox<>(new String[]{"Active", "Inactive"});

    // NEW: Supplier + Location dropdowns (IDs stored in parallel lists)
    private final JComboBox<String> cbSupplier = new JComboBox<>();
    private final JComboBox<String> cbLocation = new JComboBox<>();
    private final List<Integer> supplierIds   = new ArrayList<>();
    private final List<Integer> locationIds   = new ArrayList<>();

    // Buttons
    private final RoundedButton btnAdd     = new RoundedButton("Add", 15);
    private final RoundedButton btnUpdate  = new RoundedButton("Update Selected", 15);
    private final RoundedButton btnDelete  = new RoundedButton("Delete Selected", 15);

    // Colors
    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR     = new Color(0xEBEBEB);
    private final Color BTN_HOVER    = new Color(0xFFE0C7);

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));

        // ================= TABLE (READ-ONLY, CLEAN) =================
        model = new DefaultTableModel(new String[]{
                "Product ID", "Name", "Description", "Category",
                "UoM", "Reorder Level", "Status", "Supplier", "Location"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // lock all cells – table is view-only
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null); // extra safety: no in-cell editing
        table.setRowHeight(30);
        table.setBackground(Color.WHITE);

        // Remove grid lines for a clean look
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        header.setBorder(null);

        // Column alignment
        DefaultTableCellRenderer left   = new DefaultTableCellRenderer();
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        DefaultTableCellRenderer right  = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        center.setHorizontalAlignment(SwingConstants.CENTER);
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        // Product ID centered (your request)
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(left);   // Name
        table.getColumnModel().getColumn(2).setCellRenderer(left);   // Description
        table.getColumnModel().getColumn(3).setCellRenderer(center); // Category
        table.getColumnModel().getColumn(4).setCellRenderer(center); // UoM
        table.getColumnModel().getColumn(5).setCellRenderer(center); // Reorder
        table.getColumnModel().getColumn(6).setCellRenderer(center); // Status
        table.getColumnModel().getColumn(7).setCellRenderer(center); // Supplier
        table.getColumnModel().getColumn(8).setCellRenderer(center); // Location

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);
        add(tableScroll, BorderLayout.CENTER);

        // ================= CLICK BEHAVIOUR (TOGGLE UNSELECT) =================
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedRow  = table.rowAtPoint(e.getPoint());
                int selectedRow = table.getSelectedRow();

                // DOUBLE CLICK on a row → select and load into form
                if (e.getClickCount() == 2 && clickedRow >= 0) {
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                    fillFormFromRow(clickedRow);
                    return;
                }

                // SINGLE CLICK logic
                if (e.getClickCount() == 1) {
                    if (clickedRow < 0) return;

                    if (clickedRow == selectedRow) {
                        table.clearSelection();
                        clearForm();
                        return;
                    }

                    // Note: fields only fill on double-click (same behavior preserved)
                    table.setRowSelectionInterval(clickedRow, clickedRow);
                }
            }
        });

        // ================= FORM AREA (CLEAN, NO BOX) =================
        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.setBackground(BG_COLOR);

        JLabel title = new JLabel("Add / Update Product");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBackground(BG_COLOR);
        form.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // padding only, no border frame

        form.add(new JLabel("Name:"));            form.add(tfName);
        form.add(new JLabel("Description:"));     form.add(tfDesc);
        form.add(new JLabel("Category:"));        form.add(tfCategory);
        form.add(new JLabel("Unit of Measure:")); form.add(tfUom);
        form.add(new JLabel("Reorder Level:"));   form.add(tfReorder);
        form.add(new JLabel("Status:"));          form.add(cbStatus);

        // NEW: supplier + location form inputs
        form.add(new JLabel("Supplier:"));        form.add(cbSupplier);
        form.add(new JLabel("Location:"));        form.add(cbLocation);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.setBackground(BG_COLOR);
        addHoverEffect(btnAdd);
        addHoverEffect(btnUpdate);
        addHoverEffect(btnDelete);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        south.add(title,   BorderLayout.NORTH);
        south.add(form,    BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // Load dropdowns (NEW behavior)
        loadSuppliersToCombo();
        loadLocationsToCombo();

        // ================= BUTTON ACTIONS =================
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateSelectedProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        // Load products initially
        loadProducts();
    }

    // ---------- helper methods ----------

    private void clearForm() {
        tfName.setText("");
        tfDesc.setText("");
        tfCategory.setText("");
        tfUom.setText("");
        tfReorder.setText("0");
        cbStatus.setSelectedIndex(0);
        cbSupplier.setSelectedIndex(0);
        cbLocation.setSelectedIndex(0);
    }

    private void fillFormFromRow(int row) {
        tfName.setText(model.getValueAt(row, 1).toString());
        tfDesc.setText(model.getValueAt(row, 2).toString());
        tfCategory.setText(model.getValueAt(row, 3).toString());
        tfUom.setText(model.getValueAt(row, 4).toString());
        tfReorder.setText(model.getValueAt(row, 5).toString());
        cbStatus.setSelectedItem(model.getValueAt(row, 6).toString());

        // NEW: match selected supplier/location names
        cbSupplier.setSelectedItem(model.getValueAt(row, 7));
        cbLocation.setSelectedItem(model.getValueAt(row, 8));
    }

    // Loads suppliers into combo while storing IDs
    private void loadSuppliersToCombo() {
        cbSupplier.removeAllItems();
        supplierIds.clear();

        cbSupplier.addItem("--- None ---");
        supplierIds.add(null);

        String sql = "SELECT supplier_id, supplier_name FROM Supplier ORDER BY supplier_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                supplierIds.add(rs.getInt("supplier_id"));
                cbSupplier.addItem(rs.getString("supplier_name"));
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // Loads locations into combo while storing IDs
    private void loadLocationsToCombo() {
        cbLocation.removeAllItems();
        locationIds.clear();

        cbLocation.addItem("--- None ---");
        locationIds.add(null);

        String sql = "SELECT location_id, location_name FROM StorageLocation ORDER BY location_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                locationIds.add(rs.getInt("location_id"));
                cbLocation.addItem(rs.getString("location_name"));
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void loadProducts() {
        model.setRowCount(0);

        // Slightly expanded query to fetch supplier + location names
        String sql =
                "SELECT p.product_id, p.product_name, p.description, p.category, " +
                        "p.unit_of_measure, p.reorder_level, p.product_status, " +
                        "s.supplier_name, l.location_name " +
                        "FROM Product p " +
                        "LEFT JOIN Supplier s ON s.supplier_id = p.supplier_id " +
                        "LEFT JOIN StorageLocation l ON l.location_id = p.location_id " +
                        "ORDER BY p.product_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("unit_of_measure"),
                        rs.getBigDecimal("reorder_level"),
                        rs.getString("product_status"),
                        rs.getString("supplier_name") == null ? "—" : rs.getString("supplier_name"),
                        rs.getString("location_name") == null ? "—" : rs.getString("location_name")
                });
            }
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void addProduct() {
        String sql =
                "INSERT INTO Product " +
                        "(product_name, description, category, unit_of_measure, reorder_level, product_status, supplier_id, location_id) " +
                        "VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfDesc.getText().trim());
            ps.setString(3, tfCategory.getText().trim());
            ps.setString(4, tfUom.getText().trim());
            ps.setBigDecimal(5, DBUtils.toDecimal(tfReorder.getText()));
            ps.setString(6, (String) cbStatus.getSelectedItem());

            // NEW: supplier_id + location_id (null allowed)
            ps.setObject(7, supplierIds.get(cbSupplier.getSelectedIndex()));
            ps.setObject(8, locationIds.get(cbLocation.getSelectedIndex()));

            ps.executeUpdate();

            loadProducts();
            clearForm();
            table.clearSelection();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            DBUtils.info("Select a product first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        String sql =
                "UPDATE Product SET product_name=?, description=?, category=?, unit_of_measure=?, " +
                        "reorder_level=?, product_status=?, supplier_id=?, location_id=? " +
                        "WHERE product_id=?";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfDesc.getText().trim());
            ps.setString(3, tfCategory.getText().trim());
            ps.setString(4, tfUom.getText().trim());
            ps.setBigDecimal(5, DBUtils.toDecimal(tfReorder.getText()));
            ps.setString(6, (String) cbStatus.getSelectedItem());

            // NEW
            ps.setObject(7, supplierIds.get(cbSupplier.getSelectedIndex()));
            ps.setObject(8, locationIds.get(cbLocation.getSelectedIndex()));

            ps.setInt(9, id);

            ps.executeUpdate();
            loadProducts();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            DBUtils.info("Select a product first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        String sql = "DELETE FROM Product WHERE product_id=?";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            loadProducts();
            table.clearSelection();
            clearForm();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // ---------- pretty components ----------

    private void addHoverEffect(RoundedButton btn) {
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BTN_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT_COLOR);
            }
        });
    }

    static class RoundedButton extends JButton {
        private final int radius;
        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setContentAreaFilled(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    static class RoundedTextField extends JTextField {
        private final int radius = 10;
        public RoundedTextField(int columns) {
            super(columns);
            setOpaque(false);
        }
        public RoundedTextField(String text, int columns) {
            super(text, columns);
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
