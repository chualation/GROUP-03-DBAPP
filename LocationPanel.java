import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.math.BigDecimal;

public class LocationPanel extends JPanel {

    // Fonts
    private final Font lexendRegular = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-regular.ttf", 12f);
    private final Font lexendBold = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-bold.ttf", 14f);

    // Colors
    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR = new Color(0xEBEBEB);
    private final Color CARD_BG = Color.WHITE;
    private final Color ACTIVE_GREEN = new Color(0x4CAF50);
    private final Color WARNING_ORANGE = new Color(0xFF9800);

    // Components
    private JPanel locationsPanel;
    private JPanel productsPanel;
    private JScrollPane locationsScrollPane;
    private JScrollPane productsScrollPane;
    private JComboBox<LocationItem> cbLocationFilter;
    private JLabel lblProductCount;
    private JLabel lblLowStockCount;

    // Form fields
    private final RoundedTextField tfName = new RoundedTextField(15, new Color(0xEBEBEB));
    private final RoundedTextField tfAreaDesc = new RoundedTextField(15, new Color(0xEBEBEB));
    private final RoundedTextField tfCapacity = new RoundedTextField(10, new Color(0xEBEBEB));
    private final JComboBox<String> cbTemp = new JComboBox<>(new String[]{"None", "Refrigerated", "Freezer"});

    private LocationCard selectedLocationCard = null;

    public LocationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);

        // ===== LEFT: LOCATIONS LIST =====
        JPanel leftPanel = createLocationsPanel();

        // ===== RIGHT: PRODUCTS IN SELECTED LOCATION =====
        JPanel rightPanel = createProductsPanel();

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // ===== BOTTOM: FORM =====
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.SOUTH);

        loadLocations();
    }

    private JPanel createLocationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 10, 10));

        // Cards panel
        locationsPanel = new JPanel();
        locationsPanel.setLayout(new BoxLayout(locationsPanel, BoxLayout.Y_AXIS));
        locationsPanel.setBackground(BG_COLOR);
        locationsPanel.setBorder(new EmptyBorder(0, 0, 5, 5));

        locationsScrollPane = new JScrollPane(locationsPanel);
        locationsScrollPane.setBorder(null);
        locationsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(locationsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top: Filter and stats
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(BG_COLOR);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(BG_COLOR);

        cbLocationFilter = new JComboBox<>();
        cbLocationFilter.setPreferredSize(new Dimension(200, 30));
        cbLocationFilter.setFont(lexendRegular);
        cbLocationFilter.addActionListener(e -> loadProductsForLocation());

        filterPanel.add(createLabel("Products Located:"));
        filterPanel.setBorder(new EmptyBorder(0, -5, 0, 0));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        statsPanel.setBackground(BG_COLOR);

        lblProductCount = new JLabel("Products: 0");
        lblProductCount.setFont(lexendRegular.deriveFont(12f));

        lblLowStockCount = new JLabel("Low Stock: 0");
        lblLowStockCount.setFont(lexendBold.deriveFont(12f));
        lblLowStockCount.setForeground(Color.RED);

        statsPanel.add(lblProductCount);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblLowStockCount);

        JPanel filterStatsPanel = new JPanel(new BorderLayout());
        filterStatsPanel.setBackground(BG_COLOR);
        filterStatsPanel.add(filterPanel, BorderLayout.WEST);
        filterStatsPanel.add(statsPanel, BorderLayout.EAST);

        topPanel.add(filterStatsPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Products cards
        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBackground(BG_COLOR);
        productsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        productsScrollPane = new JScrollPane(productsPanel);
        productsScrollPane.setBorder(null);
        productsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(productsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 0),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Add / Update Storage Location");
        title.setFont(lexendBold.deriveFont(14f));
        title.setForeground(ACCENT_COLOR);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);

        form.add(createLabel("Location Name:*"));
        form.add(tfName);
        form.add(createLabel("Area Description:"));
        form.add(tfAreaDesc);
        form.add(createLabel("Capacity:*"));
        form.add(tfCapacity);
        form.add(createLabel("Temperature Control:"));
        form.add(cbTemp);

        RoundedButton btnAdd = new RoundedButton("Add", 15);
        RoundedButton btnUpdate = new RoundedButton("Update Selected", 15);
        RoundedButton btnDelete = new RoundedButton("Delete Selected", 15);
        RoundedButton btnClear = new RoundedButton("Clear", 15);

        addHoverEffect(btnAdd, ACCENT_COLOR);
        addHoverEffect(btnUpdate, ACCENT_COLOR);
        addHoverEffect(btnDelete, new Color(0xF44336));
        addHoverEffect(btnClear, new Color(0x888888));

        btnAdd.addActionListener(e -> addLocation());
        btnUpdate.addActionListener(e -> updateSelectedLocation());
        btnDelete.addActionListener(e -> deleteSelectedLocation());
        btnClear.addActionListener(e -> clearForm());

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

    private void loadLocations() {
        locationsPanel.removeAll();
        cbLocationFilter.removeAllItems();
        cbLocationFilter.addItem(new LocationItem(0, "Select Location", "", BigDecimal.ZERO, ""));

        String sql = "SELECT l.location_id, l.location_name, l.area_description, l.capacity, l.temperature_control, " +
                "COUNT(p.product_id) as product_count " +
                "FROM StorageLocation l " +
                "LEFT JOIN Product p ON l.location_id = p.location_id " +
                "GROUP BY l.location_id " +
                "ORDER BY l.location_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocationData location = new LocationData(
                        rs.getInt("location_id"),
                        rs.getString("location_name"),
                        rs.getString("area_description"),
                        rs.getBigDecimal("capacity"),
                        rs.getString("temperature_control"),
                        rs.getInt("product_count")
                );

                LocationCard card = new LocationCard(location);
                locationsPanel.add(card);
                locationsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

                cbLocationFilter.addItem(new LocationItem(
                        location.id,
                        location.name,
                        location.areaDesc,
                        location.capacity,
                        location.tempControl
                ));
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        locationsPanel.revalidate();
        locationsPanel.repaint();
    }

    private void loadProductsForLocation() {
        productsPanel.removeAll();

        LocationItem selected = (LocationItem) cbLocationFilter.getSelectedItem();
        if (selected == null || selected.id == 0) {
            JLabel noSelection = new JLabel("Select a location to view products");
            noSelection.setFont(lexendRegular.deriveFont(14f));
            noSelection.setForeground(new Color(0x888888));
            noSelection.setAlignmentX(Component.CENTER_ALIGNMENT);
            productsPanel.add(Box.createVerticalGlue());
            productsPanel.add(noSelection);
            productsPanel.add(Box.createVerticalGlue());
            lblProductCount.setText("Products: 0");
            lblLowStockCount.setText("Low Stock: 0");
            productsPanel.revalidate();
            productsPanel.repaint();
            return;
        }

        String sql = "SELECT p.product_id, p.product_name, p.category, p.unit_of_measure, " +
                "p.reorder_level, s.supplier_name, " +
                "COALESCE(SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE 0 END) - " +
                "SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END), 0) AS current_stock " +
                "FROM Product p " +
                "LEFT JOIN Supplier s ON p.supplier_id = s.supplier_id " +
                "LEFT JOIN StockMovement sm ON p.product_id = sm.product_id " +
                "WHERE p.location_id = ? " +
                "GROUP BY p.product_id " +
                "ORDER BY p.product_name";

        int productCount = 0;
        int lowStockCount = 0;

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductData product = new ProductData(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getString("unit_of_measure"),
                            rs.getBigDecimal("reorder_level"),
                            rs.getString("supplier_name"),
                            rs.getBigDecimal("current_stock")
                    );

                    ProductCard card = new ProductCard(product);
                    productsPanel.add(card);
                    productsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

                    productCount++;
                    if (product.currentStock.compareTo(product.reorderLevel) <= 0) {
                        lowStockCount++;
                    }
                }
            }

            lblProductCount.setText("Products: " + productCount);
            lblLowStockCount.setText("Low Stock: " + lowStockCount);

            if (productCount == 0) {
                JLabel noProducts = new JLabel("No products in this location");
                noProducts.setFont(lexendRegular.deriveFont(14f));
                noProducts.setForeground(new Color(0x888888));
                noProducts.setAlignmentX(Component.CENTER_ALIGNMENT);
                productsPanel.add(Box.createVerticalGlue());
                productsPanel.add(noProducts);
                productsPanel.add(Box.createVerticalGlue());
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void addLocation() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Add this location?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "INSERT INTO StorageLocation (location_name, area_description, capacity, temperature_control) VALUES (?,?,?,?)";
        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfAreaDesc.getText().trim());
            ps.setBigDecimal(3, DBUtils.toDecimal(tfCapacity.getText()));
            ps.setString(4, (String) cbTemp.getSelectedItem());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Location added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadLocations();
            clearForm();

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedLocation() {
        if (selectedLocationCard == null) {
            JOptionPane.showMessageDialog(this, "Please select a location first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = selectedLocationCard.location.id;

        if (JOptionPane.showConfirmDialog(this, "Update location #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE StorageLocation SET location_name=?, area_description=?, capacity=?, temperature_control=? WHERE location_id=?";
        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfAreaDesc.getText().trim());
            ps.setBigDecimal(3, DBUtils.toDecimal(tfCapacity.getText()));
            ps.setString(4, (String) cbTemp.getSelectedItem());
            ps.setInt(5, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Location updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadLocations();
            loadProductsForLocation();

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedLocation() {
        if (selectedLocationCard == null) {
            JOptionPane.showMessageDialog(this, "Please select a location first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = selectedLocationCard.location.id;

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this location?\nThis action cannot be undone.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM StorageLocation WHERE location_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Location deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadLocations();
            clearForm();
            productsPanel.removeAll();
            productsPanel.revalidate();
            productsPanel.repaint();

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void clearForm() {
        tfName.setText("");
        tfAreaDesc.setText("");
        tfCapacity.setText("0");
        cbTemp.setSelectedIndex(0);
        if (selectedLocationCard != null) {
            selectedLocationCard.setSelected(false);
            selectedLocationCard = null;
        }
    }

    // ===== LOCATION CARD =====
    private class LocationCard extends JPanel {
        private final LocationData location;
        private boolean selected = false;
        private final int radius = 10;

        public LocationCard(LocationData location) {
            this.location = location;
            setLayout(new BorderLayout(12, 8));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            // Left: Icon
            JLabel lblIcon = new JLabel(getTempIcon(location.tempControl));
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

            // Center: Info
            JPanel centerPanel = new JPanel(new BorderLayout(5, 3));
            centerPanel.setOpaque(false);

            JLabel lblName = new JLabel(location.name);
            lblName.setFont(lexendBold.deriveFont(14f));

            JLabel lblDesc = new JLabel(location.areaDesc != null && !location.areaDesc.isEmpty() ? location.areaDesc : "No description");
            lblDesc.setFont(lexendRegular.deriveFont(11f));
            lblDesc.setForeground(new Color(0x666666));

            JPanel bottomInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bottomInfo.setOpaque(false);

            JLabel lblTemp = new JLabel(location.tempControl);
            lblTemp.setFont(lexendRegular.deriveFont(10f));
            lblTemp.setForeground(new Color(0x555555));

            JLabel lblCapacity = new JLabel("Capacity: " + location.capacity);
            lblCapacity.setFont(lexendRegular.deriveFont(10f));
            lblCapacity.setForeground(new Color(0x555555));

            bottomInfo.add(lblTemp);
            bottomInfo.add(lblCapacity);

            centerPanel.add(lblName, BorderLayout.NORTH);
            centerPanel.add(lblDesc, BorderLayout.CENTER);
            centerPanel.add(bottomInfo, BorderLayout.SOUTH);

            // Right: Product count
            JLabel lblCount = new JLabel(String.valueOf(location.productCount));
            lblCount.setFont(lexendBold.deriveFont(20f));
            lblCount.setForeground(location.productCount > 0 ? ACTIVE_GREEN : new Color(0x888888));
            lblCount.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.setPreferredSize(new Dimension(60, 60));
            rightPanel.add(lblCount, BorderLayout.CENTER);

            JLabel lblCountLabel = new JLabel("products");
            lblCountLabel.setFont(lexendRegular.deriveFont(9f));
            lblCountLabel.setForeground(new Color(0x888888));
            lblCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rightPanel.add(lblCountLabel, BorderLayout.SOUTH);

            add(lblIcon, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selected) {
                        setSelected(false);
                        selectedLocationCard = null;
                        clearForm();
                    } else {
                        if (selectedLocationCard != null) {
                            selectedLocationCard.setSelected(false);
                        }
                        setSelected(true);
                        selectedLocationCard = LocationCard.this;
                        fillFormFromLocation(location);

                        // Also select in dropdown
                        for (int i = 0; i < cbLocationFilter.getItemCount(); i++) {
                            LocationItem item = cbLocationFilter.getItemAt(i);
                            if (item.id == location.id) {
                                cbLocationFilter.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(new Color(0xF5F5F5));
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackground(CARD_BG);
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
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

        private String getTempIcon(String tempControl) {
            if (tempControl == null) return "📦";
            switch (tempControl.toLowerCase()) {
                case "refrigerated": return "❄️";
                case "freezer": return "🧊";
                default: return "📦";
            }
        }
    }

    // ===== PRODUCT CARD =====
    private class ProductCard extends JPanel {
        private final ProductData product;
        private final int radius = 10;

        public ProductCard(ProductData product) {
            this.product = product;
            setLayout(new BorderLayout(10, 5));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            setOpaque(false);

            // Left: Name and category
            JPanel leftPanel = new JPanel(new BorderLayout(3, 2));
            leftPanel.setOpaque(false);

            JLabel lblName = new JLabel(product.name);
            lblName.setFont(lexendBold.deriveFont(13f));

            JLabel lblCategory = new JLabel(product.category + " • " + product.uom);
            lblCategory.setFont(lexendRegular.deriveFont(10f));
            lblCategory.setForeground(new Color(0x666666));

            leftPanel.add(lblName, BorderLayout.NORTH);
            leftPanel.add(lblCategory, BorderLayout.CENTER);

            // Right: Stock info
            JPanel rightPanel = new JPanel(new BorderLayout(5, 2));
            rightPanel.setOpaque(false);
            rightPanel.setPreferredSize(new Dimension(100, 50));

            JLabel lblStock = new JLabel(String.format("%.2f", product.currentStock));
            lblStock.setFont(lexendBold.deriveFont(16f));
            lblStock.setHorizontalAlignment(SwingConstants.RIGHT);

            if (product.currentStock.compareTo(BigDecimal.ZERO) <= 0) {
                lblStock.setForeground(new Color(0xF44336));
            } else if (product.currentStock.compareTo(product.reorderLevel) <= 0) {
                lblStock.setForeground(WARNING_ORANGE);
            } else {
                lblStock.setForeground(ACTIVE_GREEN);
            }

            JLabel lblSupplier = new JLabel(product.supplierName != null ? product.supplierName : "No Supplier");
            lblSupplier.setFont(lexendRegular.deriveFont(10f));
            lblSupplier.setForeground(new Color(0x666666));
            lblSupplier.setHorizontalAlignment(SwingConstants.RIGHT);

            rightPanel.add(lblStock, BorderLayout.NORTH);
            rightPanel.add(lblSupplier, BorderLayout.CENTER);

            add(leftPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void fillFormFromLocation(LocationData location) {
        tfName.setText(location.name);
        tfAreaDesc.setText(location.areaDesc != null ? location.areaDesc : "");
        tfCapacity.setText(location.capacity.toString());
        cbTemp.setSelectedItem(location.tempControl);
    }

    // ===== DATA CLASSES =====
    private static class LocationData {
        int id;
        String name;
        String areaDesc;
        BigDecimal capacity;
        String tempControl;
        int productCount;

        public LocationData(int id, String name, String areaDesc, BigDecimal capacity, String tempControl, int productCount) {
            this.id = id;
            this.name = name;
            this.areaDesc = areaDesc;
            this.capacity = capacity != null ? capacity : BigDecimal.ZERO;
            this.tempControl = tempControl;
            this.productCount = productCount;
        }
    }

    private static class ProductData {
        int id;
        String name;
        String category;
        String uom;
        BigDecimal reorderLevel;
        String supplierName;
        BigDecimal currentStock;

        public ProductData(int id, String name, String category, String uom, BigDecimal reorderLevel, String supplierName, BigDecimal currentStock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.uom = uom;
            this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
            this.supplierName = supplierName;
            this.currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
        }
    }

    private static class LocationItem {
        int id;
        String name;
        String areaDesc;
        BigDecimal capacity;
        String tempControl;

        public LocationItem(int id, String name, String areaDesc, BigDecimal capacity, String tempControl) {
            this.id = id;
            this.name = name;
            this.areaDesc = areaDesc;
            this.capacity = capacity;
            this.tempControl = tempControl;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // ===== CUSTOM COMPONENTS =====
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(lexendRegular);
        return label;
    }

    private void addHoverEffect(RoundedButton btn, Color baseColor) {
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        Color hoverColor = baseColor.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(baseColor); }
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
        }
    }
}
