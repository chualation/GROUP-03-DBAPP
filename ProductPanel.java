import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.math.BigDecimal;

/**
 * Products tab with card-based layout - IMPROVED VERSION
 */
public class ProductPanel extends JPanel {

    // Fonts
    private final Font lexendRegular = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-regular.ttf", 12f);
    private final Font lexendBold = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-bold.ttf", 14f);

    // Colors
    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR = new Color(0xEBEBEB);
    private final Color BTN_HOVER = new Color(0xFFE0C7);
    private final Color CARD_BG = Color.WHITE;
    private final Color ACTIVE_GREEN = new Color(0x4CAF50);
    private final Color INACTIVE_RED = new Color(0xF44336);
    private final Color SUCCESS_COLOR = new Color(0x4CAF50);
    private final Color WARNING_COLOR = new Color(0xFF9800);

    // Product cards container
    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private ProductCard selectedCard = null;

    // Search/Filter fields
    private final RoundedTextField tfSearch = new RoundedTextField(20, Color.WHITE);
    private final JComboBox<String> cbFilterStatus = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
    private final JComboBox<String> cbFilterCategory = new JComboBox<>(new String[]{
            "All", "Ingredient", "Packaging", "Beverage", "Equipment", "Cleaning Supply", "Utensil", "Others"
    });
    private final JCheckBox chkLowStock = new JCheckBox("Low Stock Only");

    // Form fields
    private final RoundedTextField tfName = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfDesc = new RoundedTextField(10, new Color(0xEBEBEB));
    private final JComboBox<String> cbCategory = new JComboBox<>(new String[]{
            "Ingredient", "Packaging", "Beverage", "Equipment", "Cleaning Supply", "Utensil", "Others"
    });
    private final RoundedTextField tfUom = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfReorder = new RoundedTextField(10, new Color(0xEBEBEB));
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
    private final JComboBox<Item> cbSupplier = new JComboBox<>();
    private final JComboBox<Item> cbLocation = new JComboBox<>();

    // Buttons
    private final RoundedButton btnAdd = new RoundedButton("Add", 15);
    private final RoundedButton btnUpdate = new RoundedButton("Update Selected", 15);
    private final RoundedButton btnDelete = new RoundedButton("Delete Selected", 15);
    private final RoundedButton btnClear = new RoundedButton("Clear Form", 15);

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);

        // ================= SEARCH/FILTER PANEL =================
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // ================= CARDS PANEL =================
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(BG_COLOR);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 10, 10));

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // ================= FORM =================
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.SOUTH);

        // ================= BUTTON ACTIONS =================
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateSelectedProduct());
        btnDelete.addActionListener(e -> deleteSelectedProduct());
        btnClear.addActionListener(e -> clearForm());

        loadComboData();
        loadProducts();
    }

    // ---------- FILTER PANEL ----------
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout(10, 5));
        filterPanel.setBackground(BG_COLOR);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(-10, -10, 0, 10));

        JLabel lblFilter = new JLabel();
        lblFilter.setFont(lexendBold.deriveFont(12f));
        lblFilter.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterControls.setBackground(BG_COLOR);

        // Search field with placeholder
        tfSearch.setPlaceholder("Search products...");
        tfSearch.setPreferredSize(new Dimension(200, 40));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
        });

        // Status filter
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(lexendRegular);
        cbFilterStatus.addActionListener(e -> applyFilters());

        // Category filter
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(lexendRegular);
        cbFilterCategory.addActionListener(e -> applyFilters());

        // Low stock checkbox
        chkLowStock.setFont(lexendRegular);
        chkLowStock.setBackground(BG_COLOR);
        chkLowStock.addActionListener(e -> applyFilters());

        filterControls.add(tfSearch);
        filterControls.add(lblCategory);
        filterControls.add(cbFilterCategory);
        filterControls.add(lblStatus);
        filterControls.add(cbFilterStatus);
        filterControls.add(chkLowStock);

        filterPanel.add(lblFilter, BorderLayout.NORTH);
        filterPanel.add(filterControls, BorderLayout.CENTER);

        return filterPanel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 0),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Add / Update Product");
        title.setFont(lexendBold.deriveFont(14f));
        title.setForeground(ACCENT_COLOR);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);

        form.add(createLabel("Name:*"));
        form.add(tfName);
        form.add(createLabel("Description:"));
        form.add(tfDesc);
        form.add(createLabel("Category:*"));
        form.add(cbCategory);
        form.add(createLabel("Unit of Measure:*"));
        form.add(tfUom);
        form.add(createLabel("Reorder Level:*"));
        form.add(tfReorder);
        form.add(createLabel("Supplier:"));
        form.add(cbSupplier);
        form.add(createLabel("Storage Location:"));
        form.add(cbLocation);
        form.add(createLabel("Status:"));
        form.add(cbStatus);

        addHoverEffect(btnAdd, ACCENT_COLOR);
        addHoverEffect(btnUpdate, ACCENT_COLOR);
        addHoverEffect(btnDelete, INACTIVE_RED);
        addHoverEffect(btnClear, new Color(0x888888));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.setBackground(CARD_BG);
        buttons.add(btnClear);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(lexendRegular);
        return label;
    }

    // ---------- VALIDATION METHODS ----------
    private boolean validateProductForm() {
        StringBuilder errors = new StringBuilder();

        // Validate name
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            errors.append("- Product name is required\n");
        } else if (name.length() > 100) {
            errors.append("- Product name must be 100 characters or less\n");
        }

        // Validate category
        String category = (String) cbCategory.getSelectedItem();
        if (category == null || category.isEmpty()) {
            errors.append("- Category is required\n");
        }

        // Validate unit of measure
        String uom = tfUom.getText().trim();
        if (uom.isEmpty()) {
            errors.append("- Unit of measure is required\n");
        } else if (uom.length() > 20) {
            errors.append("- Unit of measure must be 20 characters or less\n");
        }

        // Validate reorder level
        String reorderText = tfReorder.getText().trim();
        if (reorderText.isEmpty()) {
            errors.append("- Reorder level is required\n");
        } else {
            try {
                BigDecimal reorder = new BigDecimal(reorderText);
                if (reorder.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("- Reorder level must be non-negative\n");
                }
                if (reorder.scale() > 2) {
                    errors.append("- Reorder level can have at most 2 decimal places\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Reorder level must be a valid number\n");
            }
        }

        // Validate description length
        String desc = tfDesc.getText().trim();
        if (desc.length() > 255) {
            errors.append("- Description must be 255 characters or less\n");
        }

        if (errors.length() > 0) {
            showValidationError("Please fix the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showSuccess(String message) {
        JLabel label = new JLabel(message);
        label.setFont(lexendRegular);
        JOptionPane.showMessageDialog(
                this,
                label,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message, Exception ex) {
        String detailedMessage = message;
        if (ex != null) {
            // Check for common SQL errors
            String sqlMessage = ex.getMessage().toLowerCase();
            if (sqlMessage.contains("foreign key") || sqlMessage.contains("constraint")) {
                detailedMessage += "\n\nThis product is referenced by other records and cannot be deleted.";
            } else if (sqlMessage.contains("duplicate") || sqlMessage.contains("unique")) {
                detailedMessage += "\n\nA product with this name already exists.";
            } else {
                detailedMessage += "\n\nError: " + ex.getMessage();
            }
        }

        JOptionPane.showMessageDialog(
                this,
                detailedMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // ---------- HELPER METHODS ----------
    private void loadComboData() {
        cbSupplier.removeAllItems();
        cbLocation.removeAllItems();

        try (Connection conn = DBUtils.getConn()) {
            // Load suppliers
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT supplier_id, supplier_name FROM Supplier ORDER BY supplier_name");
            while (rs.next()) {
                cbSupplier.addItem(new Item(rs.getInt(1), rs.getString(2)));
            }

            // Load storage locations
            rs = conn.createStatement().executeQuery(
                    "SELECT location_id, location_name FROM StorageLocation ORDER BY location_name");
            while (rs.next()) {
                cbLocation.addItem(new Item(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            showError("Failed to load supplier and location data.", ex);
        }
    }

    private void clearForm() {
        tfName.setText("");
        tfDesc.setText("");
        cbCategory.setSelectedIndex(0);
        tfUom.setText("");
        tfReorder.setText("0");
        cbSupplier.setSelectedIndex(cbSupplier.getItemCount() > 0 ? 0 : -1);
        cbLocation.setSelectedIndex(cbLocation.getItemCount() > 0 ? 0 : -1);
        cbStatus.setSelectedIndex(0);

        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
        }
    }

    private void fillFormFromProduct(ProductData product) {
        tfName.setText(product.name != null ? product.name : "");
        tfDesc.setText(product.description != null ? product.description : "");

        // Set category dropdown
        if (product.category != null) {
            cbCategory.setSelectedItem(product.category);
        } else {
            cbCategory.setSelectedIndex(0);
        }

        tfUom.setText(product.uom != null ? product.uom : "");
        tfReorder.setText(String.valueOf(product.reorderLevel));

        selectComboItemByName(cbSupplier, product.supplierName);
        selectComboItemByName(cbLocation, product.locationName);

        cbStatus.setSelectedItem(product.status);
    }

    private void selectComboItemByName(JComboBox<Item> combo, String name) {
        if (name == null || name.isEmpty()) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Item it = combo.getItemAt(i);
            if (name.equals(it.name)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadProducts() {
        cardsPanel.removeAll();
        selectedCard = null;

        String sql = "SELECT p.product_id, p.product_name, p.description, p.category, " +
                "p.unit_of_measure, p.reorder_level, s.supplier_name, l.location_name, p.product_status, " +
                "COALESCE(SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE 0 END) - " +
                "SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END), 0) AS current_stock " +
                "FROM Product p " +
                "LEFT JOIN Supplier s ON p.supplier_id = s.supplier_id " +
                "LEFT JOIN StorageLocation l ON p.location_id = l.location_id " +
                "LEFT JOIN StockMovement sm ON p.product_id = sm.product_id " +
                "GROUP BY p.product_id " +
                "ORDER BY p.product_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                ProductData product = new ProductData(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("unit_of_measure"),
                        rs.getBigDecimal("reorder_level"),
                        rs.getString("supplier_name"),
                        rs.getString("location_name"),
                        rs.getString("product_status"),
                        rs.getBigDecimal("current_stock")
                );

                ProductCard card = new ProductCard(product);
                cardsPanel.add(card);
                cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                count++;
            }

            if (count == 0) {
                JLabel noData = new JLabel("No products found. Add your first product below!");
                noData.setFont(lexendRegular.deriveFont(14f));
                noData.setForeground(new Color(0x888888));
                noData.setAlignmentX(Component.CENTER_ALIGNMENT);
                cardsPanel.add(Box.createVerticalGlue());
                cardsPanel.add(noData);
                cardsPanel.add(Box.createVerticalGlue());
            }

        } catch (SQLException ex) {
            showError("Failed to load products.", ex);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void applyFilters() {
        String searchText = tfSearch.getText().trim().toLowerCase();
        String statusFilter = (String) cbFilterStatus.getSelectedItem();
        String categoryFilter = (String) cbFilterCategory.getSelectedItem();
        boolean lowStockOnly = chkLowStock.isSelected();

        Component[] components = cardsPanel.getComponents();

        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];

            if (comp instanceof ProductCard) {
                ProductCard card = (ProductCard) comp;
                ProductData product = card.product;

                boolean matches = true;

                // Search filter
                if (!searchText.isEmpty()) {
                    String name = product.name != null ? product.name.toLowerCase() : "";
                    String desc = product.description != null ? product.description.toLowerCase() : "";
                    String supplier = product.supplierName != null ? product.supplierName.toLowerCase() : "";

                    matches = name.contains(searchText) ||
                            desc.contains(searchText) ||
                            supplier.contains(searchText);
                }

                // Status filter
                if (matches && !"All".equals(statusFilter)) {
                    matches = statusFilter.equals(product.status);
                }

                // Category filter
                if (matches && !"All".equals(categoryFilter)) {
                    matches = categoryFilter.equals(product.category);
                }

                // Low stock filter
                if (matches && lowStockOnly) {
                    matches = product.currentStock.compareTo(product.reorderLevel) <= 0;
                }

                card.setVisible(matches);

                // Also hide/show the spacing component that follows this card
                if (i + 1 < components.length && !(components[i + 1] instanceof ProductCard)) {
                    components[i + 1].setVisible(matches);
                }
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void addProduct() {
        if (!validateProductForm()) {
            return;
        }

        if (JOptionPane.showConfirmDialog(
                this, "Add this product?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO Product " +
                "(product_name, description, category, unit_of_measure, reorder_level, supplier_id, location_id, product_status) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());

            String desc = tfDesc.getText().trim();
            ps.setString(2, desc.isEmpty() ? null : desc);

            ps.setString(3, (String) cbCategory.getSelectedItem());
            ps.setString(4, tfUom.getText().trim());
            ps.setBigDecimal(5, new BigDecimal(tfReorder.getText().trim()));

            Item selectedSupplier = (Item) cbSupplier.getSelectedItem();
            if (selectedSupplier != null) {
                ps.setInt(6, selectedSupplier.id);
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            Item selectedLocation = (Item) cbLocation.getSelectedItem();
            if (selectedLocation != null) {
                ps.setInt(7, selectedLocation.id);
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.setString(8, (String) cbStatus.getSelectedItem());
            ps.executeUpdate();

            showSuccess("Product added successfully!");
            loadProducts();
            clearForm();
        } catch (SQLException ex) {
            showError("Failed to add product.", ex);
        }
    }

    private void updateSelectedProduct() {
        if (selectedCard == null) {
            DBUtils.info("Please select a product first.");
            return;
        }

        if (!validateProductForm()) {
            return;
        }

        int id = selectedCard.product.id;

        if (JOptionPane.showConfirmDialog(
                this, "Update product #" + id + "?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE Product SET product_name=?, description=?, category=?, " +
                "unit_of_measure=?, reorder_level=?, supplier_id=?, location_id=?, product_status=? WHERE product_id=?";
        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());

            String desc = tfDesc.getText().trim();
            ps.setString(2, desc.isEmpty() ? null : desc);

            ps.setString(3, (String) cbCategory.getSelectedItem());
            ps.setString(4, tfUom.getText().trim());
            ps.setBigDecimal(5, new BigDecimal(tfReorder.getText().trim()));

            Item selectedSupplier = (Item) cbSupplier.getSelectedItem();
            if (selectedSupplier != null) {
                ps.setInt(6, selectedSupplier.id);
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            Item selectedLocation = (Item) cbLocation.getSelectedItem();
            if (selectedLocation != null) {
                ps.setInt(7, selectedLocation.id);
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.setString(8, (String) cbStatus.getSelectedItem());
            ps.setInt(9, id);
            ps.executeUpdate();

            showSuccess("Product updated successfully!");
            loadProducts();
        } catch (SQLException ex) {
            showError("Failed to update product.", ex);
        }
    }

    private void deleteSelectedProduct() {
        if (selectedCard == null) {
            DBUtils.info("Please select a product first.");
            return;
        }

        int id = selectedCard.product.id;
        String name = selectedCard.product.name;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete:\n\n" + name + " (ID: " + id + ")?\n\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM Product WHERE product_id=?";
        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            showSuccess("Product deleted successfully!");
            loadProducts();
            clearForm();
        } catch (SQLException ex) {
            showError("Failed to delete product.", ex);
        }
    }

    // ---------- PRODUCT CARD ----------
    private class ProductCard extends JPanel {
        private final ProductData product;
        private boolean selected = false;
        private final int radius = 15;

        public ProductCard(ProductData product) {
            this.product = product;
            setLayout(new BorderLayout(15, 10));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(2, 2, 2, 2),
                    new EmptyBorder(15, 15, 15, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            // Left: Category Icon
            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Determine category and colors
                    String category = product.category != null ? product.category.toLowerCase() : "";
                    Color bgColor = getCategoryBackgroundColor(category);
                    String emoji = getCategoryEmoji(category);

                    // Draw circular background
                    g2.setColor(bgColor);
                    g2.fillOval(0, 0, 80, 80);

                    // Draw emoji
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(emoji);
                    int textHeight = fm.getAscent();
                    int x = (80 - textWidth) / 2;
                    int y = (80 + textHeight) / 2 - 5;

                    g2.setColor(Color.BLACK);
                    g2.drawString(emoji, x, y);

                    g2.dispose();
                }
            };
            iconPanel.setPreferredSize(new Dimension(80, 80));
            iconPanel.setOpaque(false);
            add(iconPanel, BorderLayout.WEST);

            // Center: Main content panel
            JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
            contentPanel.setOpaque(false);

            // Top row: ID badge, Name, Supplier and Stock info
            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.setBorder(new EmptyBorder(0, 0, 0, 0));

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            leftPanel.setOpaque(false);

            // ID Badge
            JLabel lblId = new JLabel(String.format("#%d", product.id)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            lblId.setFont(lexendRegular.deriveFont(10f));
            lblId.setForeground(Color.WHITE);
            lblId.setOpaque(false);
            lblId.setBackground(new Color(0x666666));
            lblId.setBorder(new EmptyBorder(3, 8, 3, 8));
            leftPanel.add(lblId);

            // Name
            JLabel lblName = new JLabel(product.name != null ? product.name : "Unnamed Product");
            lblName.setFont(lexendBold);
            leftPanel.add(lblName);

            // Supplier
            JLabel lblSupplier = new JLabel("(" + (product.supplierName != null ? product.supplierName : "No Supplier") + ")");
            lblSupplier.setFont(lexendRegular);
            lblSupplier.setForeground(new Color(0x888888));
            leftPanel.add(lblSupplier);

            topRow.add(leftPanel, BorderLayout.WEST);

            // Stock display with warning indicator
            JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            stockPanel.setOpaque(false);

            JLabel lblStock = new JLabel(String.format("%.2f / %.2f",
                    product.currentStock,
                    product.reorderLevel));
            lblStock.setFont(lexendBold.deriveFont(14f));

            // Check if restocking is needed
            if (product.currentStock.compareTo(product.reorderLevel) <= 0) {
                lblStock.setForeground(WARNING_COLOR);
                JLabel warningIcon = new JLabel("*");
                warningIcon.setFont(lexendBold.deriveFont(16f));
                warningIcon.setForeground(WARNING_COLOR);
                warningIcon.setToolTipText("Stock below reorder level!");
                stockPanel.add(warningIcon);
            } else {
                lblStock.setForeground(Color.BLACK);
            }

            stockPanel.add(lblStock);
            topRow.add(stockPanel, BorderLayout.EAST);

            contentPanel.add(topRow, BorderLayout.NORTH);

            // Center: Description
            JPanel descPanel = new JPanel(new BorderLayout());
            descPanel.setOpaque(false);
            descPanel.setBorder(new EmptyBorder(0, 8, 0, 0));

            String description = product.description != null && !product.description.trim().isEmpty()
                    ? product.description
                    : "No description available";
            JLabel lblDesc = new JLabel(description);
            lblDesc.setFont(lexendRegular.deriveFont(12f));
            lblDesc.setForeground(new Color(0x666666));
            descPanel.add(lblDesc, BorderLayout.WEST);

            contentPanel.add(descPanel, BorderLayout.CENTER);

            // Bottom row: Status, Category, Location, UoM
            JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bottomRow.setOpaque(false);
            bottomRow.setBorder(new EmptyBorder(20, 0, 0, 0));

            // Status badge
            JLabel lblStatus = new JLabel(product.status != null ? product.status : "Unknown") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            lblStatus.setFont(lexendRegular.deriveFont(12f));
            lblStatus.setForeground(Color.WHITE);
            lblStatus.setOpaque(false);
            lblStatus.setBackground("Active".equals(product.status) ? ACTIVE_GREEN : INACTIVE_RED);
            lblStatus.setBorder(new EmptyBorder(3, 8, 3, 8));
            bottomRow.add(lblStatus);

            bottomRow.add(createInfoBadge(product.category != null ? product.category : "N/A", Color.WHITE));
            bottomRow.add(createInfoBadge(product.locationName != null ? product.locationName : "No Location", Color.WHITE));
            bottomRow.add(createInfoBadge(product.uom != null ? product.uom : "N/A", Color.WHITE));

            contentPanel.add(bottomRow, BorderLayout.SOUTH);

            // Add content panel to card
            add(contentPanel, BorderLayout.CENTER);

            // Click handler
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selected) {
                        // Deselect
                        setSelected(false);
                        selectedCard = null;
                        clearForm();
                    } else {
                        // Deselect previous
                        if (selectedCard != null) {
                            selectedCard.setSelected(false);
                        }
                        // Select this
                        setSelected(true);
                        selectedCard = ProductCard.this;
                        fillFormFromProduct(product);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(new Color(0xF5F5F5));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackground(CARD_BG);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }

        private JLabel createInfoBadge(String text, Color bgColor) {
            JLabel badge = new JLabel(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badge.setFont(lexendRegular.deriveFont(12f));
            badge.setForeground(new Color(0x666666));
            badge.setOpaque(false);
            badge.setBackground(bgColor);
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));
            return badge;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackground(new Color(0xFFE0C7));
            } else {
                setBackground(CARD_BG);
            }
            repaint();
        }

        // Category color mapping - simplified to broad categories
        private Color getCategoryBackgroundColor(String category) {
            if (category.contains("ingredient")) {
                return new Color(0xC8E6C9); // Light green
            } else if (category.contains("packaging")) {
                return new Color(0xD7CCC8); // Light brown
            } else if (category.contains("beverage")) {
                return new Color(0xB2DFDB); // Light teal
            } else if (category.contains("equipment") || category.contains("tool")) {
                return new Color(0xB0BEC5); // Light blue-gray
            } else if (category.contains("cleaning") || category.contains("supply")) {
                return new Color(0xB3E5FC); // Light cyan
            } else if (category.contains("utensil")) {
                return new Color(0xFFE0B2); // Light orange
            } else {
                return new Color(0xE0E0E0); // Light gray (Others/Default)
            }
        }

        // Category emoji mapping - simplified to broad categories
        private String getCategoryEmoji(String category) {
            if (category.contains("ingredient")) {
                return "🍗";
            } else if (category.contains("packaging")) {
                return "📦";
            } else if (category.contains("beverage")) {
                return "🥤";
            } else if (category.contains("equipment") || category.contains("tool")) {
                return "🔧";
            } else if (category.contains("cleaning") || category.contains("supply")) {
                return "🧹";
            } else if (category.contains("utensil")) {
                return "🍴";
            } else {
                return "📋"; // Others/Default
            }
        }
    }

    // ---------- PRODUCT DATA CLASS ----------
    private static class ProductData {
        int id;
        String name;
        String description;
        String category;
        String uom;
        BigDecimal reorderLevel;
        String supplierName;
        String locationName;
        String status;
        BigDecimal currentStock;

        public ProductData(int id, String name, String description, String category,
                           String uom, BigDecimal reorderLevel,
                           String supplierName, String locationName, String status,
                           BigDecimal currentStock) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.uom = uom;
            this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
            this.supplierName = supplierName;
            this.locationName = locationName;
            this.status = status;
            this.currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
        }
    }

    // ---------- CUSTOM COMPONENTS ----------
    private void addHoverEffect(RoundedButton btn, Color baseColor) {
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        Color hoverColor = baseColor.brighter();

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
            }
        });
    }

    public static class RoundedButton extends JButton {
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

    public class RoundedTextField extends JTextField {
        private int radius = 10;
        private String placeholder = "";
        private Color backgroundColor;

        public RoundedTextField(int columns, Color bgColor) {
            super(columns);
            this.backgroundColor = bgColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);

            // Draw placeholder text if field is empty
            if (getText().isEmpty() && !placeholder.isEmpty()) {
                g2.setColor(new Color(0x999999));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(placeholder, x, y);
            }

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border
        }
    }

    public static class Item {
        public int id;
        public String name;

        public Item(int i, String n) {
            id = i;
            name = n;
        }

        public String toString() {
            return name;
        }
    }
}
