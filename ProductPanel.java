import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ProductPanel extends JPanel {
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();

    private final RoundedTextField tfName = new RoundedTextField(10);
    private final RoundedTextField tfDesc = new RoundedTextField(10);
    private final RoundedTextField tfCategory = new RoundedTextField(10);
    private final RoundedTextField tfUom = new RoundedTextField(10);
    private final RoundedTextField tfReorder = new RoundedTextField("0", 10);
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    private final RoundedButton btnAdd = new RoundedButton("Add", 15);
    private final RoundedButton btnUpdate = new RoundedButton("Update Selected", 15);
    private final RoundedButton btnDelete = new RoundedButton("Delete Selected", 15);

    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR = new Color(0xEBEBEB);
    private final Color BTN_HOVER = new Color(0xFFE0C7);
    private JButton selectedButton = null;

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.RED);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // --- Table setup ---
        model.setColumnIdentifiers(new String[]{
                "Product ID", "Name", "Description", "Category",
                "UoM", "Reorder", "Status"
        });
        table.setModel(model);

        table.getColumnModel().getColumn(0).setHeaderRenderer(new HeaderRenderer(SwingConstants.CENTER));  // ID
        table.getColumnModel().getColumn(1).setHeaderRenderer(new HeaderRenderer(SwingConstants.LEFT));  // Name
        table.getColumnModel().getColumn(2).setHeaderRenderer(new HeaderRenderer(SwingConstants.LEFT));    // Description
        table.getColumnModel().getColumn(3).setHeaderRenderer(new HeaderRenderer(SwingConstants.CENTER));  // Category
        table.getColumnModel().getColumn(4).setHeaderRenderer(new HeaderRenderer(SwingConstants.CENTER));  // UoM
        table.getColumnModel().getColumn(5).setHeaderRenderer(new HeaderRenderer(SwingConstants.CENTER));   // Reorder
        table.getColumnModel().getColumn(6).setHeaderRenderer(new HeaderRenderer(SwingConstants.CENTER));  // Status

        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(300);  // Description
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Category
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // UoM
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Reorder
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status

        // ScrollPane border
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setViewportBorder(null);
        tableScroll.setBorder(null);
        add(tableScroll, BorderLayout.CENTER);

        // Background colors
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(BG_COLOR);
        table.setSelectionBackground(Color.BLUE);   // highlighted row color
        table.setSelectionForeground(Color.BLACK);
        tableScroll.setBorder(BorderFactory.createLineBorder(BG_COLOR));

        // Row height
        table.setRowHeight(35);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        // Table border
        table.setShowGrid(false);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Intercell spacing (spacing between cells)
        table.setIntercellSpacing(new Dimension(10, 5));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        // ID (center)
        table.getColumnModel().getColumn(0).setCellRenderer(right);

        // Name (center)
        table.getColumnModel().getColumn(1).setCellRenderer(left);

        // Description (left)
        table.getColumnModel().getColumn(2).setCellRenderer(left);

        // Category (center)
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        // UoM (center)
        table.getColumnModel().getColumn(4).setCellRenderer(center);

        // Reorder Level (right-aligned number)
        table.getColumnModel().getColumn(5).setCellRenderer(center);

        // Status (center)
        table.getColumnModel().getColumn(6).setCellRenderer(center);

        // --- Form setup ---
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Add / Update Product"));
        form.setBackground(BG_COLOR);
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Description:")); form.add(tfDesc);
        form.add(new JLabel("Category:")); form.add(tfCategory);
        form.add(new JLabel("Unit of Measure:")); form.add(tfUom);
        form.add(new JLabel("Reorder Level:")); form.add(tfReorder);
        form.add(new JLabel("Status:")); form.add(cbStatus);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(new Color(0xEBEBEB));
        addHoverEffect(btnAdd);
        addHoverEffect(btnUpdate);
        addHoverEffect(btnDelete);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        JPanel south = new JPanel(new BorderLayout(10, 10));
        south.setBackground(new Color(0xEBEBEB));
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // --- Actions ---
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateSelectedProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    tfName.setText(model.getValueAt(row, 1).toString());
                    tfDesc.setText(model.getValueAt(row, 2).toString());
                    tfCategory.setText(model.getValueAt(row, 3).toString());
                    tfUom.setText(model.getValueAt(row, 4).toString());
                    tfReorder.setText(model.getValueAt(row, 5).toString());
                    cbStatus.setSelectedItem(model.getValueAt(row, 6).toString());
                }
            }
        });

        loadProducts();
    }

    static class HeaderRenderer extends DefaultTableCellRenderer {
        public HeaderRenderer(int alignment) {
            setHorizontalAlignment(alignment);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setOpaque(true);
            setBackground(new Color(0xDCDCDC));
        }
    }

    private void addHoverEffect(RoundedButton btn) {
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.BLACK);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BTN_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0xFEFEFE));
            }
        });
    }

    private void loadProducts(){
        model.setRowCount(0);
        String sql = "SELECT product_id, product_name, description, category, " + "unit_of_measure, reorder_level, product_status " + "FROM Product ORDER BY product_name";
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
                        rs.getBigDecimal(6),
                        rs.getString(7)
                });
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void addProduct(){
        if(JOptionPane.showConfirmDialog(this, "Add this product?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

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

        if(JOptionPane.showConfirmDialog(this, "Update product #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

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

        if(JOptionPane.showConfirmDialog(this, "Delete product #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }

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

    // --- Rounded components ---
    static class RoundedButton extends JButton {
        private int radius;
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
        private int radius = 10;
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
