import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MovementPanel extends JPanel {
    private final ReportPanel reportPanel;
    private final Font lexendRegular = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-regular.ttf", 12f);
    private final Font lexendBold = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-bold.ttf", 14f);
    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR = new Color(0xEBEBEB);
    private final Color BTN_HOVER = new Color(0xFFE0C7);
    private final Color CARD_BG = Color.WHITE;
    private JPanel formPanel;
    private CardLayout formCardLayout;
    private final RoundedButton btnRestock = new RoundedButton("Product Restock", 15);
    private final RoundedButton btnProductReturn = new RoundedButton("Product Return", 15);
    private final RoundedButton btnSupplierReturn = new RoundedButton("Supplier Return", 15);
    private final RoundedButton btnSalesTransaction = new RoundedButton("Sales Transaction", 15);
    private JPanel productsPanel;
    private JScrollPane productsScrollPane;
    private ProductCard selectedProductCard = null;
    private final JComboBox<Item> cbRestockProduct = new JComboBox<>();
    private final JComboBox<Item> cbRestockSupplier = new JComboBox<>();
    private final JComboBox<Item> cbRestockLocation = new JComboBox<>();
    private final RoundedTextField tfRestockQty = new RoundedTextField(10, new Color(0xEBEBEB));
    private final DatePicker dpRestockDate = new DatePicker();
    private final JComboBox<Item> cbPReturnProduct = new JComboBox<>();
    private final JComboBox<Item> cbPReturnLocation = new JComboBox<>();
    private final RoundedTextField tfPReturnQty = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfPReturnReason = new RoundedTextField(10, new Color(0xEBEBEB));
    private final DatePicker dpPReturnDate = new DatePicker();
    private final JComboBox<Item> cbSReturnProduct = new JComboBox<>();
    private final JComboBox<Item> cbSReturnSupplier = new JComboBox<>();
    private final JComboBox<Item> cbSReturnLocation = new JComboBox<>();
    private final RoundedTextField tfSReturnQty = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfSReturnReason = new RoundedTextField(10, new Color(0xEBEBEB));
    private final DatePicker dpSReturnDate = new DatePicker();
    private final JComboBox<Item> cbSalesProduct = new JComboBox<>();
    private final JComboBox<Item> cbSalesLocation = new JComboBox<>();
    private final RoundedTextField tfSalesQty = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfSalesAmount = new RoundedTextField(10, new Color(0xEBEBEB));
    private final DatePicker dpSalesDate = new DatePicker();
    private String currentTransactionType = "";
    private Color UNSELECTED_COLOR = new Color(0xBDBDBD);
    private Color restockColor = new Color(0x4CAF50);
    private Color productReturnColor = new Color(0x2196F3);
    private Color supplierReturnColor = new Color(0xFF9800);
    private Color salesColor = new Color(0x9C27B0);
    private RoundedTextField searchField;

    public MovementPanel(ReportPanel rp) {
        this.reportPanel = rp;
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        typePanel.setBackground(BG_COLOR);

        // Set consistent size for all buttons
        Dimension btnSize = new Dimension(180, 50);
        btnRestock.setPreferredSize(btnSize);
        btnProductReturn.setPreferredSize(btnSize);
        btnSupplierReturn.setPreferredSize(btnSize);
        btnSalesTransaction.setPreferredSize(btnSize);

        styleTypeButton(btnRestock);
        styleTypeButton(btnProductReturn);
        styleTypeButton(btnSupplierReturn);
        styleTypeButton(btnSalesTransaction);

        btnRestock.setBackground(UNSELECTED_COLOR);
        btnProductReturn.setBackground(UNSELECTED_COLOR);
        btnSupplierReturn.setBackground(UNSELECTED_COLOR);
        btnSalesTransaction.setBackground(UNSELECTED_COLOR);

        typePanel.add(btnRestock);
        typePanel.add(btnProductReturn);
        typePanel.add(btnSupplierReturn);
        typePanel.add(btnSalesTransaction);

        // Create products panel and center panel
        createProductsPanel();

        // Create center panel with products and forms
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.add(productsScrollPane, BorderLayout.NORTH);

        formCardLayout = new CardLayout();
        formPanel = new JPanel(formCardLayout);
        formPanel.setBackground(BG_COLOR);
        formPanel.add(createEmptyCard(), "EMPTY");
        formPanel.add(createRestockForm(), "RESTOCK");
        formPanel.add(createProductReturnForm(), "PRETURN");
        formPanel.add(createSupplierReturnForm(), "SRETURN");
        formPanel.add(createSalesForm(), "SALES");
        centerPanel.add(formPanel, BorderLayout.CENTER);

        add(typePanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        btnRestock.addActionListener(e -> showForm("RESTOCK"));
        btnProductReturn.addActionListener(e -> showForm("PRETURN"));
        btnSupplierReturn.addActionListener(e -> showForm("SRETURN"));
        btnSalesTransaction.addActionListener(e -> showForm("SALES"));
        loadComboData();
        showForm("EMPTY");
    }

    private void createProductsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
        wrapper.setBackground(BG_COLOR);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BG_COLOR);

        searchField = new RoundedTextField(20);
        searchField.setPlaceholder("Search products...");
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterProducts(searchField.getText());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterProducts(searchField.getText());
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterProducts(searchField.getText());
            }
        });

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(lexendRegular);
        searchPanel.add(lblSearch);
        searchPanel.add(searchField);

        wrapper.add(searchPanel, BorderLayout.NORTH);

        // Products panel
        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBackground(BG_COLOR);
        productsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(productsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Select a Product"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(0, 300));

        wrapper.add(scroll, BorderLayout.CENTER);

        productsScrollPane = new JScrollPane(wrapper);
        productsScrollPane.setBorder(null);
        productsScrollPane.setPreferredSize(new Dimension(0, 350)); // overall height
        productsScrollPane.setVisible(false);
    }

    private void loadProductCards() {
        productsPanel.removeAll();
        selectedProductCard = null;
        String sql = "SELECT p.product_id, p.product_name, p.category, p.unit_of_measure, p.reorder_level, s.supplier_id, s.supplier_name, l.location_id, l.location_name, COALESCE(SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE 0 END) - SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END), 0) AS current_stock FROM Product p LEFT JOIN Supplier s ON p.supplier_id = s.supplier_id LEFT JOIN StorageLocation l ON p.location_id = l.location_id LEFT JOIN StockMovement sm ON p.product_id = sm.product_id WHERE p.product_status = 'Active' GROUP BY p.product_id ORDER BY p.product_name";
        try (Connection conn = DBUtils.getConn(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductData product = new ProductData(rs.getInt("product_id"), rs.getString("product_name"), rs.getString("category"), rs.getString("unit_of_measure"), rs.getBigDecimal("reorder_level"), rs.getInt("supplier_id"), rs.getString("supplier_name"), rs.getInt("location_id"), rs.getString("location_name"), rs.getBigDecimal("current_stock"));
                ProductCard card = new ProductCard(product);
                productsPanel.add(card);
                productsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void filterProducts(String searchText) {
        searchText = searchText.trim().toLowerCase();
        Component[] components = productsPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof ProductCard) {
                ProductCard card = (ProductCard) comp;
                ProductData product = card.product;
                boolean matches = true;
                if (!searchText.isEmpty()) {
                    String name = product.name != null ? product.name.toLowerCase() : "";
                    String supplier = product.supplierName != null ? product.supplierName.toLowerCase() : "";
                    matches = name.contains(searchText) || supplier.contains(searchText);
                }
                card.setVisible(matches);
            }
        }
        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private JPanel createEmptyCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        JLabel label = new JLabel("Please select a transaction type above to begin");
        label.setFont(lexendRegular.deriveFont(14f));
        label.setForeground(new Color(0x888888));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(30, 0, 30, 0));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRestockForm() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4CAF50), 0),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("Product Restock Transaction");
        title.setFont(lexendBold.deriveFont(16f));
        title.setForeground(new Color(0x4CAF50));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);
        form.add(createLabel("Product:*"));
        cbRestockProduct.setEnabled(false);
        form.add(cbRestockProduct);
        form.add(createLabel("Supplier:*"));
        cbRestockSupplier.setEnabled(false);
        form.add(cbRestockSupplier);
        form.add(createLabel("Storage Location:*"));
        cbRestockLocation.setEnabled(false);
        form.add(cbRestockLocation);
        form.add(createLabel("Quantity:*"));
        form.add(tfRestockQty);
        form.add(createLabel("Date:*"));
        form.add(dpRestockDate);

        RoundedButton btnSubmit = new RoundedButton("Submit Restock", 15);
        addHoverEffect(btnSubmit, new Color(0x4CAF50));
        btnSubmit.addActionListener(e -> submitRestock());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(btnSubmit);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProductReturnForm() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2196F3), 0),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("Product Return Transaction");
        title.setFont(lexendBold.deriveFont(16f));
        title.setForeground(new Color(0x2196F3));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);
        form.add(createLabel("Product:*"));
        cbPReturnProduct.setEnabled(false);
        form.add(cbPReturnProduct);
        form.add(createLabel("Storage Location:*"));
        cbPReturnLocation.setEnabled(false);
        form.add(cbPReturnLocation);
        form.add(createLabel("Quantity:*"));
        form.add(tfPReturnQty);
        form.add(createLabel("Return Reason:*"));
        form.add(tfPReturnReason);
        form.add(createLabel("Date:*"));
        form.add(dpPReturnDate);

        RoundedButton btnSubmit = new RoundedButton("Submit Return", 15);
        addHoverEffect(btnSubmit, new Color(0x2196F3));
        btnSubmit.addActionListener(e -> submitProductReturn());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(btnSubmit);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSupplierReturnForm() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFF9800), 0),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("Supplier Return Transaction");
        title.setFont(lexendBold.deriveFont(16f));
        title.setForeground(new Color(0xFF9800));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);
        form.add(createLabel("Product:*"));
        cbSReturnProduct.setEnabled(false);
        form.add(cbSReturnProduct);
        form.add(createLabel("Supplier:*"));
        cbSReturnSupplier.setEnabled(false);
        form.add(cbSReturnSupplier);
        form.add(createLabel("Storage Location:*"));
        cbSReturnLocation.setEnabled(false);
        form.add(cbSReturnLocation);
        form.add(createLabel("Quantity:*"));
        form.add(tfSReturnQty);
        form.add(createLabel("Return Reason:*"));
        form.add(tfSReturnReason);
        form.add(createLabel("Date:*"));
        form.add(dpSReturnDate);

        RoundedButton btnSubmit = new RoundedButton("Submit Return", 15);
        addHoverEffect(btnSubmit, new Color(0xFF9800));
        btnSubmit.addActionListener(e -> submitSupplierReturn());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(btnSubmit);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSalesForm() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x9C27B0), 0),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("Sales Transaction");
        title.setFont(lexendBold.deriveFont(16f));
        title.setForeground(new Color(0x9C27B0));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);
        form.add(createLabel("Product:*"));
        cbSalesProduct.setEnabled(false);
        form.add(cbSalesProduct);
        form.add(createLabel("Storage Location:*"));
        cbSalesLocation.setEnabled(false);
        form.add(cbSalesLocation);
        form.add(createLabel("Quantity:*"));
        form.add(tfSalesQty);
        form.add(createLabel("Total Amount:*"));
        form.add(tfSalesAmount);
        form.add(createLabel("Date:*"));
        form.add(dpSalesDate);

        RoundedButton btnSubmit = new RoundedButton("Submit Sale", 15);
        addHoverEffect(btnSubmit, new Color(0x9C27B0));
        btnSubmit.addActionListener(e -> submitSales());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(btnSubmit);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(lexendRegular);
        return label;
    }

    private void showForm(String formName) {
        currentTransactionType = formName;
        formCardLayout.show(formPanel, formName);
        btnRestock.setBackground(UNSELECTED_COLOR);
        btnProductReturn.setBackground(UNSELECTED_COLOR);
        btnSupplierReturn.setBackground(UNSELECTED_COLOR);
        btnSalesTransaction.setBackground(UNSELECTED_COLOR);

        // Show products for all transaction types except EMPTY
        boolean showProducts = formName.equals("RESTOCK") ||
                formName.equals("PRETURN") ||
                formName.equals("SRETURN") ||
                formName.equals("SALES");

        productsScrollPane.setVisible(showProducts);
        if (showProducts) {
            loadProductCards();
        }

        switch (formName) {
            case "RESTOCK": btnRestock.setBackground(restockColor); break;
            case "PRETURN": btnProductReturn.setBackground(productReturnColor); break;
            case "SRETURN": btnSupplierReturn.setBackground(supplierReturnColor); break;
            case "SALES": btnSalesTransaction.setBackground(salesColor); break;
        }
    }

    private void submitRestock() {
        try {
            if (cbRestockProduct.getSelectedItem() == null ||
                    cbRestockSupplier.getSelectedItem() == null ||
                    cbRestockLocation.getSelectedItem() == null ||
                    tfRestockQty.getText().trim().isEmpty() ||
                    dpRestockDate.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal qty;
            try {
                qty = new BigDecimal(tfRestockQty.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (JOptionPane.showConfirmDialog(this, "Submit restock transaction?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            Connection conn = DBUtils.getConn();
            conn.setAutoCommit(false);
            try {
                int productId = ((Item) cbRestockProduct.getSelectedItem()).id;
                int supplierId = ((Item) cbRestockSupplier.getSelectedItem()).id;
                int locationId = ((Item) cbRestockLocation.getSelectedItem()).id;
                Date date = dpRestockDate.getDate();

                String sql = "INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason) VALUES (?, ?, ?, ?, 'IN', ?, 'Product Restock')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, productId);
                ps.setInt(2, locationId);
                ps.setInt(3, supplierId);
                ps.setBigDecimal(4, qty);
                ps.setDate(5, date);
                ps.executeUpdate();
                ps.close();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Restock transaction recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearRestockForm();
                loadProductCards();
                reportPanel.refresh();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void submitProductReturn() {
        try {
            if (cbPReturnProduct.getSelectedItem() == null || cbPReturnLocation.getSelectedItem() == null || tfPReturnQty.getText().trim().isEmpty() || tfPReturnReason.getText().trim().isEmpty() || dpPReturnDate.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal qty = new BigDecimal(tfPReturnQty.getText().trim());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Submit product return?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Connection conn = DBUtils.getConn();
            conn.setAutoCommit(false);
            try {
                int productId = ((Item) cbPReturnProduct.getSelectedItem()).id;
                int locationId = ((Item) cbPReturnLocation.getSelectedItem()).id;
                Date date = dpPReturnDate.getDate();
                String reason = tfPReturnReason.getText().trim();
                String sql = "INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason) VALUES (?, ?, NULL, ?, 'IN', ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, productId);
                ps.setInt(2, locationId);
                ps.setBigDecimal(3, qty);
                ps.setDate(4, date);
                ps.setString(5, "Product Return: " + reason);
                ps.executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "Product return recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearProductReturnForm();
                loadProductCards();
                reportPanel.refresh();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception ex) {
            DBUtils.showErr((SQLException) ex);
        }
    }

    private void submitSupplierReturn() {
        try {
            if (cbSReturnProduct.getSelectedItem() == null || cbSReturnSupplier.getSelectedItem() == null || cbSReturnLocation.getSelectedItem() == null || tfSReturnQty.getText().trim().isEmpty() || tfSReturnReason.getText().trim().isEmpty() || dpSReturnDate.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal qty = new BigDecimal(tfSReturnQty.getText().trim());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int productId = ((Item) cbSReturnProduct.getSelectedItem()).id;
            BigDecimal currentStock = getCurrentStock(productId);
            if (currentStock.compareTo(qty) < 0) {
                JOptionPane.showMessageDialog(this, String.format("Insufficient stock! Current: %.2f, Requested: %.2f", currentStock, qty), "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Submit supplier return?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Connection conn = DBUtils.getConn();
            conn.setAutoCommit(false);
            try {
                int supplierId = ((Item) cbSReturnSupplier.getSelectedItem()).id;
                int locationId = ((Item) cbSReturnLocation.getSelectedItem()).id;
                Date date = dpSReturnDate.getDate();
                String reason = tfSReturnReason.getText().trim();
                String sql = "INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason) VALUES (?, ?, ?, ?, 'OUT', ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, productId);
                ps.setInt(2, locationId);
                ps.setInt(3, supplierId);
                ps.setBigDecimal(4, qty);
                ps.setDate(5, date);
                ps.setString(6, "Supplier Return: " + reason);
                ps.executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "Supplier return recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearSupplierReturnForm();
                loadProductCards();
                reportPanel.refresh();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception ex) {
            DBUtils.showErr((SQLException) ex);
        }
    }

    private void submitSales() {
        try {
            if (cbSalesProduct.getSelectedItem() == null || cbSalesLocation.getSelectedItem() == null || tfSalesQty.getText().trim().isEmpty() || tfSalesAmount.getText().trim().isEmpty() || dpSalesDate.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal qty = new BigDecimal(tfSalesQty.getText().trim());
            BigDecimal amount = new BigDecimal(tfSalesAmount.getText().trim());
            if (qty.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity and amount must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int productId = ((Item) cbSalesProduct.getSelectedItem()).id;
            BigDecimal currentStock = getCurrentStock(productId);
            if (currentStock.compareTo(qty) < 0) {
                JOptionPane.showMessageDialog(this, String.format("Insufficient stock! Current: %.2f, Requested: %.2f", currentStock, qty), "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Submit sales transaction?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Connection conn = DBUtils.getConn();
            conn.setAutoCommit(false);
            try {
                int locationId = ((Item) cbSalesLocation.getSelectedItem()).id;
                Date date = dpSalesDate.getDate();
                String sql = "INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason) VALUES (?, ?, NULL, ?, 'OUT', ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, productId);
                ps.setInt(2, locationId);
                ps.setBigDecimal(3, qty);
                ps.setDate(4, date);
                ps.setString(5, "Sales - Amount: " + amount);
                ps.executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "Sales transaction recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearSalesForm();
                reportPanel.refresh();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception ex) {
            DBUtils.showErr((SQLException) ex);
        }
    }

    private BigDecimal getCurrentStock(int productId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(CASE WHEN movement_type = 'IN' THEN quantity ELSE 0 END) - SUM(CASE WHEN movement_type = 'OUT' THEN quantity ELSE 0 END), 0) AS current_stock FROM StockMovement WHERE product_id = ?";
        try (Connection conn = DBUtils.getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("current_stock");
            }
        }
        return BigDecimal.ZERO;
    }

    private void clearRestockForm() {
        if (selectedProductCard != null) {
            selectedProductCard.setSelected(false);
            selectedProductCard = null;
        }
        cbRestockProduct.removeAllItems();
        cbRestockSupplier.removeAllItems();
        cbRestockLocation.removeAllItems();
        tfRestockQty.setText("");
        dpRestockDate.setDate(new Date(System.currentTimeMillis()));
    }

    private void clearProductReturnForm() {
        if (selectedProductCard != null) {
            selectedProductCard.setSelected(false);
            selectedProductCard = null;
        }
        cbPReturnProduct.removeAllItems();
        cbPReturnLocation.removeAllItems();
        tfPReturnQty.setText("");
        tfPReturnReason.setText("");
        dpPReturnDate.setDate(new Date(System.currentTimeMillis()));
    }

    private void clearSupplierReturnForm() {
        if (selectedProductCard != null) {
            selectedProductCard.setSelected(false);
            selectedProductCard = null;
        }
        cbSReturnProduct.removeAllItems();
        cbSReturnSupplier.removeAllItems();
        cbSReturnLocation.removeAllItems();
        tfSReturnQty.setText("");
        tfSReturnReason.setText("");
        dpSReturnDate.setDate(new Date(System.currentTimeMillis()));
    }

    private void clearSalesForm() {
        if (selectedProductCard != null) {
            selectedProductCard.setSelected(false);
            selectedProductCard = null;
        }
        cbSalesProduct.removeAllItems();
        cbSalesLocation.removeAllItems();
        tfSalesQty.setText("");
        tfSalesAmount.setText("");
        dpSalesDate.setDate(new Date(System.currentTimeMillis()));
    }

    private void loadComboData() {
        try (Connection conn = DBUtils.getConn()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT location_id, location_name FROM StorageLocation ORDER BY location_name");
            while (rs.next()) {
                Item item = new Item(rs.getInt(1), rs.getString(2));
            }
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private class ProductCard extends JPanel {
        private final ProductData product;
        private boolean selected = false;
        private final int radius = 10;

        public ProductCard(ProductData product) {
            this.product = product;
            setLayout(new BorderLayout(10, 8));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            JPanel leftPanel = new JPanel(new BorderLayout(5, 2));
            leftPanel.setOpaque(false);
            JLabel lblName = new JLabel(product.name);
            lblName.setFont(lexendBold.deriveFont(13f));
            JLabel lblCategory = new JLabel(product.category + " • " + product.uom);
            lblCategory.setFont(lexendRegular.deriveFont(11f));
            lblCategory.setForeground(new Color(0x666666));
            leftPanel.add(lblName, BorderLayout.NORTH);
            leftPanel.add(lblCategory, BorderLayout.CENTER);
            JPanel rightPanel = new JPanel(new BorderLayout(5, 2));
            rightPanel.setOpaque(false);
            JLabel lblStock = new JLabel(String.format("Stock: %.2f", product.currentStock));
            lblStock.setFont(lexendBold.deriveFont(12f));
            lblStock.setHorizontalAlignment(SwingConstants.RIGHT);
            if (product.currentStock.compareTo(product.reorderLevel) <= 0) {
                lblStock.setForeground(new Color(0xF44336));
            } else {
                lblStock.setForeground(new Color(0x4CAF50));
            }
            JLabel lblSupplier = new JLabel(product.supplierName != null ? product.supplierName : "No Supplier");
            lblSupplier.setFont(lexendRegular.deriveFont(11f));
            lblSupplier.setForeground(new Color(0x666666));
            lblSupplier.setHorizontalAlignment(SwingConstants.RIGHT);
            rightPanel.add(lblStock, BorderLayout.NORTH);
            rightPanel.add(lblSupplier, BorderLayout.CENTER);
            add(leftPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selected) {
                        setSelected(false);
                        selectedProductCard = null;
                        clearFormFieldsForCurrentType();
                    } else {
                        if (selectedProductCard != null) {
                            selectedProductCard.setSelected(false);
                        }
                        setSelected(true);
                        selectedProductCard = ProductCard.this;
                        fillFormFromProduct(product);
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
    }

    private void clearFormFieldsForCurrentType() {
        switch (currentTransactionType) {
            case "RESTOCK":
                cbRestockProduct.removeAllItems();
                cbRestockSupplier.removeAllItems();
                cbRestockLocation.removeAllItems();
                tfRestockQty.setText("");
                break;
            case "PRETURN":
                cbPReturnProduct.removeAllItems();
                cbPReturnLocation.removeAllItems();
                tfPReturnQty.setText("");
                tfPReturnReason.setText("");
                break;
            case "SRETURN":
                cbSReturnProduct.removeAllItems();
                cbSReturnSupplier.removeAllItems();
                cbSReturnLocation.removeAllItems();
                tfSReturnQty.setText("");
                tfSReturnReason.setText("");
                break;
            case "SALES": // Add this case
                cbSalesProduct.removeAllItems();
                cbSalesLocation.removeAllItems();
                tfSalesQty.setText("");
                tfSalesAmount.setText("");
                break;
        }
    }

    private void fillFormFromProduct(ProductData product) {
        switch (currentTransactionType) {
            case "RESTOCK":
                cbRestockProduct.removeAllItems();
                cbRestockProduct.addItem(new Item(product.id, product.name));
                cbRestockSupplier.removeAllItems();
                if (product.supplierId > 0) {
                    cbRestockSupplier.addItem(new Item(product.supplierId, product.supplierName));
                }
                cbRestockLocation.removeAllItems();
                if (product.locationId > 0) {
                    cbRestockLocation.addItem(new Item(product.locationId, product.locationName));
                }
                break;
            case "PRETURN":
                cbPReturnProduct.removeAllItems();
                cbPReturnProduct.addItem(new Item(product.id, product.name));
                cbPReturnLocation.removeAllItems();
                if (product.locationId > 0) {
                    cbPReturnLocation.addItem(new Item(product.locationId, product.locationName));
                }
                break;
            case "SRETURN":
                cbSReturnProduct.removeAllItems();
                cbSReturnProduct.addItem(new Item(product.id, product.name));
                cbSReturnSupplier.removeAllItems();
                if (product.supplierId > 0) {
                    cbSReturnSupplier.addItem(new Item(product.supplierId, product.supplierName));
                }
                cbSReturnLocation.removeAllItems();
                if (product.locationId > 0) {
                    cbSReturnLocation.addItem(new Item(product.locationId, product.locationName));
                }
                break;
            case "SALES": // Add this case
                cbSalesProduct.removeAllItems();
                cbSalesProduct.addItem(new Item(product.id, product.name));
                cbSalesLocation.removeAllItems();
                if (product.locationId > 0) {
                    cbSalesLocation.addItem(new Item(product.locationId, product.locationName));
                }
                break;
        }
    }

    private static class ProductData {
        int id;
        String name;
        String category;
        String uom;
        BigDecimal reorderLevel;
        int supplierId;
        String supplierName;
        int locationId;
        String locationName;
        BigDecimal currentStock;

        public ProductData(int id, String name, String category, String uom, BigDecimal reorderLevel, int supplierId, String supplierName, int locationId, String locationName, BigDecimal currentStock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.uom = uom;
            this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.locationId = locationId;
            this.locationName = locationName;
            this.currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
        }
    }

    private void styleTypeButton(RoundedButton btn) {
        btn.setForeground(Color.WHITE);
        btn.setFont(lexendBold.deriveFont(13f)); // Match ReportPanel font size
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
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

    public class DatePicker extends JPanel {
        private final JTextField textField;
        private final JButton button;
        private Date selectedDate;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public DatePicker() {
            setLayout(new BorderLayout(5, 0));
            setBackground(CARD_BG);
            textField = new JTextField(10);
            textField.setEditable(false);
            textField.setFont(lexendRegular);
            button = new JButton("📅");
            button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            button.setPreferredSize(new Dimension(35, 25));
            button.setFocusPainted(false);
            add(textField, BorderLayout.CENTER);
            add(button, BorderLayout.EAST);
            setDate(new Date(System.currentTimeMillis()));
            button.addActionListener(e -> showCalendar());
        }

        public void setDate(Date date) {
            this.selectedDate = date;
            if (date != null) {
                textField.setText(dateFormat.format(date));
            } else {
                textField.setText("");
            }
        }

        public Date getDate() {
            return selectedDate;
        }

        private void showCalendar() {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
            dialog.setLayout(new BorderLayout(10, 10));
            Calendar cal = Calendar.getInstance();
            if (selectedDate != null) {
                cal.setTime(selectedDate);
            }
            CalendarPanel calendarPanel = new CalendarPanel(cal, dialog);
            dialog.add(calendarPanel, BorderLayout.CENTER);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        private class CalendarPanel extends JPanel {
            private final Calendar calendar;
            private final JDialog parentDialog;
            private JLabel monthYearLabel;
            private JPanel daysPanel;

            public CalendarPanel(Calendar cal, JDialog dialog) {
                this.calendar = (Calendar) cal.clone();
                this.parentDialog = dialog;
                setLayout(new BorderLayout(5, 5));
                setBorder(new EmptyBorder(10, 10, 10, 10));
                setBackground(Color.WHITE);
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setBackground(Color.WHITE);
                JButton prevMonth = new JButton("◀");
                prevMonth.setFont(new Font("Dialog", Font.PLAIN, 10));
                prevMonth.addActionListener(e -> changeMonth(-1));
                monthYearLabel = new JLabel("", SwingConstants.CENTER);
                monthYearLabel.setFont(lexendBold.deriveFont(14f));
                JButton nextMonth = new JButton("▶");
                nextMonth.setFont(new Font("Dialog", Font.PLAIN, 10));
                nextMonth.addActionListener(e -> changeMonth(1));
                topPanel.add(prevMonth, BorderLayout.WEST);
                topPanel.add(monthYearLabel, BorderLayout.CENTER);
                topPanel.add(nextMonth, BorderLayout.EAST);
                add(topPanel, BorderLayout.NORTH);
                daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
                daysPanel.setBackground(Color.WHITE);
                add(daysPanel, BorderLayout.CENTER);
                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                bottomPanel.setBackground(Color.WHITE);
                JButton todayButton = new JButton("Today");
                todayButton.setFont(lexendRegular);
                todayButton.addActionListener(e -> {
                    calendar.setTime(new java.util.Date(System.currentTimeMillis()));
                    updateCalendar();
                });
                bottomPanel.add(todayButton);
                add(bottomPanel, BorderLayout.SOUTH);
                updateCalendar();
            }

            private void changeMonth(int amount) {
                calendar.add(Calendar.MONTH, amount);
                updateCalendar();
            }

            private void updateCalendar() {
                SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy");
                monthYearLabel.setText(monthYearFormat.format(calendar.getTime()));
                daysPanel.removeAll();
                String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                for (String day : dayNames) {
                    JLabel label = new JLabel(day, SwingConstants.CENTER);
                    label.setFont(lexendBold.deriveFont(10f));
                    label.setForeground(new Color(0x666666));
                    daysPanel.add(label);
                }
                Calendar temp = (Calendar) calendar.clone();
                temp.set(Calendar.DAY_OF_MONTH, 1);
                int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
                int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
                int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                boolean isCurrentMonth = (temp.get(Calendar.MONTH) == currentMonth && temp.get(Calendar.YEAR) == currentYear);
                for (int i = 1; i < firstDayOfWeek; i++) {
                    daysPanel.add(new JLabel(""));
                }
                for (int day = 1; day <= daysInMonth; day++) {
                    final int dayOfMonth = day;
                    JButton dayButton = new JButton(String.valueOf(day));
                    dayButton.setFont(lexendRegular.deriveFont(11f));
                    dayButton.setFocusPainted(false);
                    dayButton.setBackground(Color.WHITE);
                    dayButton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
                    if (isCurrentMonth && day == today) {
                        dayButton.setBackground(new Color(0xE3F2FD));
                    }
                    dayButton.addActionListener(e -> {
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        Date newDate = new Date(calendar.getTimeInMillis());
                        setDate(newDate);
                        parentDialog.dispose();
                    });
                    dayButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (!dayButton.getBackground().equals(new Color(0xE3F2FD))) {
                                dayButton.setBackground(new Color(0xF5F5F5));
                            }
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (!dayButton.getBackground().equals(new Color(0xE3F2FD))) {
                                dayButton.setBackground(Color.WHITE);
                            }
                        }
                    });
                    daysPanel.add(dayButton);
                }
                daysPanel.revalidate();
                daysPanel.repaint();
            }
        }
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

        public RoundedTextField(int columns) {
            this(columns, Color.WHITE);
        }

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
