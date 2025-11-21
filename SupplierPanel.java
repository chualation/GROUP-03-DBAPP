import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * SupplierPanel with split view:
 * - Left: Supplier cards
 * - Right: Purchase/Stock-in transactions for selected supplier
 */
public class SupplierPanel extends JPanel {

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

    // Components
    private JPanel suppliersPanel;
    private JPanel transactionsPanel;
    private JScrollPane suppliersScrollPane;
    private JScrollPane transactionsScrollPane;
    private SupplierCard selectedCard = null;

    private JLabel lblTransactionCount;
    private JLabel lblTotalAmount;

    // Search field
    private final RoundedTextField tfSearch = new RoundedTextField(20, Color.WHITE);

    // Form fields
    private final RoundedTextField tfName = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfContactPerson = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfContactNo = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfEmail = new RoundedTextField(10, new Color(0xEBEBEB));
    private final RoundedTextField tfAddress = new RoundedTextField(10, new Color(0xEBEBEB));
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    // Buttons
    private final RoundedButton btnAdd = new RoundedButton("Add", 15);
    private final RoundedButton btnUpdate = new RoundedButton("Update Selected", 15);
    private final RoundedButton btnDelete = new RoundedButton("Delete Selected", 15);
    private final RoundedButton btnClear = new RoundedButton("Clear Form", 15);

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);

        // ===== LEFT: SUPPLIERS PANEL =====
        JPanel leftPanel = createSuppliersPanel();

        // ===== RIGHT: TRANSACTIONS PANEL =====
        JPanel rightPanel = createTransactionsPanel();

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // ===== BOTTOM: FORM =====
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.SOUTH);

        loadSuppliers();
    }

    private JPanel createSuppliersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 10, 10));

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BG_COLOR);

        tfSearch.setPlaceholder("Search suppliers...");
        tfSearch.setPreferredSize(new Dimension(200, 40));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
        });

        JLabel lblTitle = new JLabel();

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(lblTitle, BorderLayout.NORTH);
        titlePanel.add(tfSearch, BorderLayout.CENTER);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Cards panel
        suppliersPanel = new JPanel();
        suppliersPanel.setLayout(new BoxLayout(suppliersPanel, BoxLayout.Y_AXIS));
        suppliersPanel.setBackground(BG_COLOR);
        suppliersPanel.setBorder(new EmptyBorder(5, 0, 5, 5));

        suppliersScrollPane = new JScrollPane(suppliersPanel);
        suppliersScrollPane.setBorder(null);
        suppliersScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(suppliersScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top: Title and stats
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(BG_COLOR);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        statsPanel.setBackground(BG_COLOR);

        lblTransactionCount = new JLabel("Transactions: 0");
        lblTransactionCount.setFont(lexendRegular.deriveFont(12f));

        statsPanel.add(lblTransactionCount);

        topPanel.add(statsPanel, BorderLayout.WEST);
        topPanel.setBorder(new EmptyBorder(0, -10, 0, 0));

        panel.add(topPanel, BorderLayout.NORTH);

        // Transactions cards
        transactionsPanel = new JPanel();
        transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
        transactionsPanel.setBackground(BG_COLOR);
        transactionsPanel.setBorder(new EmptyBorder(10, 5, 5, 5));

        transactionsScrollPane = new JScrollPane(transactionsPanel);
        transactionsScrollPane.setBorder(null);
        transactionsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(transactionsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 0),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Add / Update Supplier");
        title.setFont(lexendBold.deriveFont(14f));
        title.setForeground(ACCENT_COLOR);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(CARD_BG);

        form.add(createLabel("Name:*"));
        form.add(tfName);
        form.add(createLabel("Contact Person:"));
        form.add(tfContactPerson);
        form.add(createLabel("Contact Number:"));
        form.add(tfContactNo);
        form.add(createLabel("Email:"));
        form.add(tfEmail);
        form.add(createLabel("Address:"));
        form.add(tfAddress);
        form.add(createLabel("Status:"));
        form.add(cbStatus);

        addHoverEffect(btnAdd, ACCENT_COLOR);
        addHoverEffect(btnUpdate, ACCENT_COLOR);
        addHoverEffect(btnDelete, INACTIVE_RED);
        addHoverEffect(btnClear, new Color(0x888888));

        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSelectedSupplier());
        btnDelete.addActionListener(e -> deleteSelectedSupplier());
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

    private void applySearch() {
        String searchText = tfSearch.getText().trim().toLowerCase();
        Component[] components = suppliersPanel.getComponents();

        for (Component comp : components) {
            if (comp instanceof SupplierCard) {
                SupplierCard card = (SupplierCard) comp;
                SupplierData supplier = card.supplier;

                boolean matches = true;
                if (!searchText.isEmpty()) {
                    String name = supplier.name != null ? supplier.name.toLowerCase() : "";
                    String contactPerson = supplier.contactPerson != null ? supplier.contactPerson.toLowerCase() : "";
                    String email = supplier.email != null ? supplier.email.toLowerCase() : "";
                    String contactNo = supplier.contactNo != null ? supplier.contactNo.toLowerCase() : "";

                    matches = name.contains(searchText) ||
                            contactPerson.contains(searchText) ||
                            email.contains(searchText) ||
                            contactNo.contains(searchText);
                }
                card.setVisible(matches);
            }
        }

        suppliersPanel.revalidate();
        suppliersPanel.repaint();
    }

    private void loadSuppliers() {
        suppliersPanel.removeAll();
        selectedCard = null;

        String sql = "SELECT s.supplier_id, s.supplier_name, s.contact_person, s.contact_number, " +
                "s.email, s.address, s.supplier_status, " +
                "COUNT(sm.movement_id) AS transaction_count " +
                "FROM Supplier s " +
                "LEFT JOIN StockMovement sm ON s.supplier_id = sm.supplier_id " +
                "AND sm.movement_type = 'IN' " +
                "GROUP BY s.supplier_id " +
                "ORDER BY s.supplier_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                SupplierData supplier = new SupplierData(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_person"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("supplier_status"),
                        rs.getInt("transaction_count")
                );

                SupplierCard card = new SupplierCard(supplier);
                suppliersPanel.add(card);
                suppliersPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                count++;
            }

            if (count == 0) {
                JLabel noData = new JLabel("No suppliers found");
                noData.setFont(lexendRegular.deriveFont(14f));
                noData.setForeground(new Color(0x888888));
                noData.setAlignmentX(Component.CENTER_ALIGNMENT);
                suppliersPanel.add(Box.createVerticalGlue());
                suppliersPanel.add(noData);
                suppliersPanel.add(Box.createVerticalGlue());
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        suppliersPanel.revalidate();
        suppliersPanel.repaint();

        // Clear transactions panel
        transactionsPanel.removeAll();
        showEmptyTransactionsMessage();
    }

    private void loadTransactionsForSupplier(int supplierId, String supplierName) {
        transactionsPanel.removeAll();

        String sql = "SELECT sm.movement_id, sm.movement_date, sm.quantity, sm.reason, " +
                "p.product_name, p.unit_of_measure, l.location_name " +
                "FROM StockMovement sm " +
                "JOIN Product p ON sm.product_id = p.product_id " +
                "LEFT JOIN StorageLocation l ON sm.location_id = l.location_id " +
                "WHERE sm.supplier_id = ? AND sm.movement_type = 'IN' " +
                "ORDER BY sm.movement_date DESC, sm.movement_id DESC";

        int transactionCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TransactionData transaction = new TransactionData(
                            rs.getInt("movement_id"),
                            rs.getDate("movement_date"),
                            rs.getString("product_name"),
                            rs.getBigDecimal("quantity"),
                            rs.getString("unit_of_measure"),
                            rs.getString("location_name"),
                            rs.getString("reason")
                    );

                    TransactionCard card = new TransactionCard(transaction);
                    transactionsPanel.add(card);
                    transactionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

                    transactionCount++;

                    // Extract amount from reason if available
                    BigDecimal amount = extractAmount(transaction.reason);
                    if (amount != null) {
                        totalAmount = totalAmount.add(amount);
                    }
                }

                lblTransactionCount.setText("Transactions: " + transactionCount);

                if (transactionCount == 0) {
                    JLabel noData = new JLabel("No purchase transactions for " + supplierName);
                    noData.setFont(lexendRegular.deriveFont(14f));
                    noData.setForeground(new Color(0x888888));
                    noData.setAlignmentX(Component.CENTER_ALIGNMENT);
                    transactionsPanel.add(Box.createVerticalGlue());
                    transactionsPanel.add(noData);
                    transactionsPanel.add(Box.createVerticalGlue());
                }
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        transactionsPanel.revalidate();
        transactionsPanel.repaint();
    }

    private BigDecimal extractAmount(String reason) {
        // Try to extract amount from reason string
        // Example: "Product Restock - Amount: 1500.00"
        if (reason != null && reason.contains("Amount:")) {
            try {
                String[] parts = reason.split("Amount:");
                if (parts.length > 1) {
                    String amountStr = parts[1].trim().replaceAll("[^0-9.]", "");
                    return new BigDecimal(amountStr);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        return null;
    }

    private void showEmptyTransactionsMessage() {
        JLabel emptyMsg = new JLabel("Select a supplier to view transactions");
        emptyMsg.setFont(lexendRegular.deriveFont(14f));
        emptyMsg.setForeground(new Color(0x888888));
        emptyMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        transactionsPanel.add(Box.createVerticalGlue());
        transactionsPanel.add(emptyMsg);
        transactionsPanel.add(Box.createVerticalGlue());
        lblTransactionCount.setText("Transactions: 0");
        transactionsPanel.revalidate();
        transactionsPanel.repaint();
    }

    private void clearForm() {
        tfName.setText("");
        tfContactPerson.setText("");
        tfContactNo.setText("");
        tfEmail.setText("");
        tfAddress.setText("");
        cbStatus.setSelectedIndex(0);

        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
        }
    }

    private void fillFormFromSupplier(SupplierData supplier) {
        tfName.setText(supplier.name != null ? supplier.name : "");
        tfContactPerson.setText(supplier.contactPerson != null ? supplier.contactPerson : "");
        tfContactNo.setText(supplier.contactNo != null ? supplier.contactNo : "");
        tfEmail.setText(supplier.email != null ? supplier.email : "");
        tfAddress.setText(supplier.address != null ? supplier.address : "");
        cbStatus.setSelectedItem(supplier.status);
    }

    private void addSupplier() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Add this supplier?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "INSERT INTO Supplier (supplier_name, contact_person, contact_number, email, address, supplier_status) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfContactPerson.getText().trim());
            ps.setString(3, tfContactNo.getText().trim());
            ps.setString(4, tfEmail.getText().trim());
            ps.setString(5, tfAddress.getText().trim());
            ps.setString(6, (String) cbStatus.getSelectedItem());

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSuppliers();
            clearForm();

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedSupplier() {
        if (selectedCard == null) {
            JOptionPane.showMessageDialog(this, "Please select a supplier first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = selectedCard.supplier.id;

        if (JOptionPane.showConfirmDialog(this, "Update supplier #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE Supplier SET supplier_name=?, contact_person=?, contact_number=?, email=?, address=?, supplier_status=? WHERE supplier_id=?";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfContactPerson.getText().trim());
            ps.setString(3, tfContactNo.getText().trim());
            ps.setString(4, tfEmail.getText().trim());
            ps.setString(5, tfAddress.getText().trim());
            ps.setString(6, (String) cbStatus.getSelectedItem());
            ps.setInt(7, id);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSuppliers();

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedSupplier() {
        if (selectedCard == null) {
            JOptionPane.showMessageDialog(this, "Please select a supplier first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = selectedCard.supplier.id;
        String name = selectedCard.supplier.name;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete:\n\n" + name + " (ID: " + id + ")?\n\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Supplier WHERE supplier_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSuppliers();
            clearForm();

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // ===== SUPPLIER CARD =====
    private class SupplierCard extends JPanel {
        private final SupplierData supplier;
        private boolean selected = false;
        private final int radius = 10;

        public SupplierCard(SupplierData supplier) {
            this.supplier = supplier;
            setLayout(new BorderLayout(10, 8));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            // Left: Icon
            JLabel lblIcon = new JLabel("🏪");
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

            // Center: Info
            JPanel centerPanel = new JPanel(new BorderLayout(5, 3));
            centerPanel.setOpaque(false);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            topRow.setOpaque(false);

            JLabel lblId = new JLabel(String.format("#%d", supplier.id)) {
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

            JLabel lblName = new JLabel(supplier.name != null ? supplier.name : "Unnamed");
            lblName.setFont(lexendBold.deriveFont(14f));

            topRow.add(lblId);
            topRow.add(lblName);

            JLabel lblContact = new JLabel(supplier.contactPerson != null ? supplier.contactPerson : "No contact");
            lblContact.setFont(lexendRegular.deriveFont(12f));
            lblContact.setForeground(new Color(0x666666));
            lblContact.setBorder(new EmptyBorder(3, 8, 3, 8));

            JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bottomRow.setOpaque(false);

            JLabel lblPhone = new JLabel(supplier.contactNo != null ? supplier.contactNo : "N/A");
            lblPhone.setFont(lexendRegular.deriveFont(12f));
            lblPhone.setForeground(new Color(0x555555));

            JLabel lblTrans = new JLabel(supplier.transactionCount + " purchases");
            lblTrans.setFont(lexendRegular.deriveFont(12f));
            lblTrans.setForeground(new Color(0x555555));

            bottomRow.add(lblPhone);
            bottomRow.add(lblTrans);

            centerPanel.add(topRow, BorderLayout.NORTH);
            centerPanel.add(lblContact, BorderLayout.CENTER);
            centerPanel.add(bottomRow, BorderLayout.SOUTH);

            // Right: Status badge
            JLabel lblStatus = new JLabel(supplier.status != null ? supplier.status : "Unknown") {
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
            lblStatus.setFont(lexendRegular.deriveFont(10f));
            lblStatus.setForeground(Color.WHITE);
            lblStatus.setOpaque(false);
            lblStatus.setBackground("Active".equals(supplier.status) ? ACTIVE_GREEN : INACTIVE_RED);
            lblStatus.setBorder(new EmptyBorder(3, 8, 3, 8));

            add(lblIcon, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
            add(lblStatus, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selected) {
                        setSelected(false);
                        selectedCard = null;
                        clearForm();
                        showEmptyTransactionsMessage();
                    } else {
                        if (selectedCard != null) {
                            selectedCard.setSelected(false);
                        }
                        setSelected(true);
                        selectedCard = SupplierCard.this;
                        fillFormFromSupplier(supplier);
                        loadTransactionsForSupplier(supplier.id, supplier.name);
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

    // ===== TRANSACTION CARD =====
    private class TransactionCard extends JPanel {
        private final TransactionData transaction;
        private final int radius = 10;

        public TransactionCard(TransactionData transaction) {
            this.transaction = transaction;
            setLayout(new BorderLayout(10, 8));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setOpaque(false);

            // Left: Date icon and date
            JPanel leftPanel = new JPanel(new BorderLayout(5, 3));
            leftPanel.setOpaque(false);
            leftPanel.setPreferredSize(new Dimension(100, 60));

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            String dateStr = transaction.date != null ? sdf.format(transaction.date) : "N/A";

            JLabel lblDate = new JLabel(dateStr);
            lblDate.setFont(lexendBold.deriveFont(11f));
            lblDate.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel lblId = new JLabel("#" + transaction.id);
            lblId.setFont(lexendRegular.deriveFont(9f));
            lblId.setForeground(new Color(0x888888));
            lblId.setHorizontalAlignment(SwingConstants.CENTER);

            leftPanel.add(lblDate, BorderLayout.CENTER);
            leftPanel.add(lblId, BorderLayout.SOUTH);

            // Center: Product info
            JPanel centerPanel = new JPanel(new BorderLayout(5, 3));
            centerPanel.setOpaque(false);

            JLabel lblProduct = new JLabel(transaction.productName);
            lblProduct.setFont(lexendBold.deriveFont(13f));

            JLabel lblLocation = new JLabel((transaction.locationName != null ? transaction.locationName : "Unknown Location"));
            lblLocation.setFont(lexendRegular.deriveFont(11f));
            lblLocation.setForeground(new Color(0x666666));

            centerPanel.add(lblProduct, BorderLayout.NORTH);
            centerPanel.add(lblLocation, BorderLayout.CENTER);

            // Right: Quantity
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.setPreferredSize(new Dimension(100, 60));

            JLabel lblQty = new JLabel(String.format("%.2f", transaction.quantity));
            lblQty.setFont(lexendBold.deriveFont(16f));
            lblQty.setForeground(ACTIVE_GREEN);
            lblQty.setHorizontalAlignment(SwingConstants.RIGHT);

            JLabel lblUnit = new JLabel(transaction.uom);
            lblUnit.setFont(lexendRegular.deriveFont(10f));
            lblUnit.setForeground(new Color(0x666666));
            lblUnit.setHorizontalAlignment(SwingConstants.RIGHT);

            rightPanel.add(lblQty, BorderLayout.CENTER);
            rightPanel.add(lblUnit, BorderLayout.SOUTH);

            add(leftPanel, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
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

    // ===== DATA CLASSES =====
    private static class SupplierData {
        int id;
        String name;
        String contactPerson;
        String contactNo;
        String email;
        String address;
        String status;
        int transactionCount;

        public SupplierData(int id, String name, String contactPerson, String contactNo,
                            String email, String address, String status, int transactionCount) {
            this.id = id;
            this.name = name;
            this.contactPerson = contactPerson;
            this.contactNo = contactNo;
            this.email = email;
            this.address = address;
            this.status = status;
            this.transactionCount = transactionCount;
        }
    }

    private static class TransactionData {
        int id;
        Date date;
        String productName;
        BigDecimal quantity;
        String uom;
        String locationName;
        String reason;

        public TransactionData(int id, Date date, String productName, BigDecimal quantity,
                               String uom, String locationName, String reason) {
            this.id = id;
            this.date = date;
            this.productName = productName;
            this.quantity = quantity;
            this.uom = uom;
            this.locationName = locationName;
            this.reason = reason;
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
}
