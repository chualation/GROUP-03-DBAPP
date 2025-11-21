import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

/**
 * ReportPanel - Shows 4 different reports with modern card-based UI
 * 1) Inventory Report
 * 2) Stock Movement Report
 * 3) Supplier Delivery Report
 * 4) Sales Report
 */
public class ReportPanel extends JPanel {

    // Fonts
    private final Font lexendRegular = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-regular.ttf", 12f);
    private final Font lexendBold = CloudKitchenApp.FontUtils.loadFont("/resources/fonts/lexend-bold.ttf", 14f);

    // Colors
    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR = new Color(0xEBEBEB);
    private final Color CARD_BG = Color.WHITE;
    private final Color ACTIVE_GREEN = new Color(0x4CAF50);
    private final Color INACTIVE_RED = new Color(0xF44336);
    private final Color WARNING_ORANGE = new Color(0xFF9800);
    private final Color UNSELECTED_COLOR = new Color(0xBDBDBD);

    // Report type colors
    private final Color inventoryColor = new Color(0x4CAF50);
    private final Color movementColor = new Color(0x2196F3);
    private final Color supplierColor = new Color(0xFF9800);
    private final Color salesColor = new Color(0x9C27B0);

    // Components
    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private final JComboBox<Integer> cbYear;
    private final JComboBox<String> cbMonth;
    private final RoundedButton btnInventory;
    private final RoundedButton btnStockMovement;
    private final RoundedButton btnSupplierDelivery;
    private final RoundedButton btnSales;

    // 0 = Inventory, 1 = Stock Movement, 2 = Supplier Delivery, 3 = Sales
    private int currentReportType = 0;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);

        // ===== TOP: FILTERS AND BUTTONS =====
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(10, 0, 10, 10));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(BG_COLOR);

        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(lexendRegular.deriveFont(13f));

        int currentYear = LocalDate.now().getYear();

        // For only past 10 years + current year:
        Integer[] years = new Integer[11];
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - 10 + i;
        }

        cbYear = new JComboBox<>(years);
        cbYear.setSelectedItem(currentYear); // Select current year
        cbYear.setPreferredSize(new Dimension(100, 30));
        cbYear.setFont(lexendRegular);
        cbYear.addActionListener(e -> loadReport());

        JLabel lblMonth = new JLabel("Month:");
        lblMonth.setFont(lexendRegular.deriveFont(13f));

        int currentMonth = LocalDate.now().getMonthValue();
        cbMonth = new JComboBox<>(new String[]{
                "01 - January", "02 - February", "03 - March", "04 - April",
                "05 - May", "06 - June", "07 - July", "08 - August",
                "09 - September", "10 - October", "11 - November", "12 - December"
        });
        cbMonth.setSelectedIndex(currentMonth - 1);
        cbMonth.setPreferredSize(new Dimension(150, 30));
        cbMonth.setFont(lexendRegular);
        cbMonth.addActionListener(e -> loadReport());

        filterBar.add(lblYear);
        filterBar.add(cbYear);
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(lblMonth);
        filterBar.add(cbMonth);

        // Report type buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);

        btnInventory = new RoundedButton("Inventory", 15);
        btnStockMovement = new RoundedButton("Stock Movement", 15);
        btnSupplierDelivery = new RoundedButton("Supplier Delivery", 15);
        btnSales = new RoundedButton("Sales", 15);

        Dimension btnSize = new Dimension(180, 50);
        btnInventory.setPreferredSize(btnSize);
        btnStockMovement.setPreferredSize(btnSize);
        btnSupplierDelivery.setPreferredSize(btnSize);
        btnSales.setPreferredSize(btnSize);

        styleReportButton(btnInventory);
        styleReportButton(btnStockMovement);
        styleReportButton(btnSupplierDelivery);
        styleReportButton(btnSales);

        buttonPanel.add(btnInventory);
        buttonPanel.add(btnStockMovement);
        buttonPanel.add(btnSupplierDelivery);
        buttonPanel.add(btnSales);

        topPanel.add(filterBar, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: CARDS PANEL =====
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(BG_COLOR);
        cardsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // ===== EVENT HANDLERS =====
        btnInventory.addActionListener(e -> {
            currentReportType = 0;
            highlightSelectedButton();
            loadReport();
        });

        btnStockMovement.addActionListener(e -> {
            currentReportType = 1;
            highlightSelectedButton();
            loadReport();
        });

        btnSupplierDelivery.addActionListener(e -> {
            currentReportType = 2;
            highlightSelectedButton();
            loadReport();
        });

        btnSales.addActionListener(e -> {
            currentReportType = 3;
            highlightSelectedButton();
            loadReport();
        });

        highlightSelectedButton();
        loadReport();
    }

    private void highlightSelectedButton() {
        btnInventory.setBackground(UNSELECTED_COLOR);
        btnStockMovement.setBackground(UNSELECTED_COLOR);
        btnSupplierDelivery.setBackground(UNSELECTED_COLOR);
        btnSales.setBackground(UNSELECTED_COLOR);

        switch (currentReportType) {
            case 0 -> btnInventory.setBackground(inventoryColor);
            case 1 -> btnStockMovement.setBackground(movementColor);
            case 2 -> btnSupplierDelivery.setBackground(supplierColor);
            case 3 -> btnSales.setBackground(salesColor);
        }
    }

    private int getSelectedYear() {
        return (Integer) cbYear.getSelectedItem();
    }

    private int getSelectedMonth() {
        return cbMonth.getSelectedIndex() + 1; // 1-12
    }

    private void loadReport() {
        int year = getSelectedYear(); // Changed from parseYear()
        int month = getSelectedMonth();

        switch (currentReportType) {
            case 0 -> loadInventoryReport(year, month);
            case 1 -> loadStockMovementReport(year, month);
            case 2 -> loadSupplierDeliveryReport(year, month);
            case 3 -> loadSalesReport(year, month);
        }
    }

    // ===== REPORT 1: INVENTORY REPORT =====
    // Shows current stock levels per product and location as of the end of the selected month/year
    private void loadInventoryReport(int year, int month) {
        cardsPanel.removeAll();

        // Add date range header
        addDateRangeHeader("Inventory Report", year, month, "Stock levels as of end of month");

        // Calculate stock up to and including the last day of the selected month
        String sql = "SELECT p.product_name, l.location_name, " +
                "COALESCE(SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity " +
                "WHEN sm.movement_type = 'OUT' THEN -sm.quantity ELSE 0 END), 0) AS stock, " +
                "p.reorder_level " +
                "FROM Product p " +
                "LEFT JOIN StorageLocation l ON p.location_id = l.location_id " +
                "LEFT JOIN StockMovement sm ON p.product_id = sm.product_id " +
                "AND sm.movement_date <= LAST_DAY(CONCAT(?, '-', LPAD(?, 2, '0'), '-01')) " +
                "WHERE p.product_status = 'Active' " +
                "GROUP BY p.product_id, l.location_id " +
                "HAVING stock > 0 OR p.reorder_level > 0 " +
                "ORDER BY p.product_name, l.location_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    BigDecimal stock = rs.getBigDecimal("stock");
                    BigDecimal reorder = rs.getBigDecimal("reorder_level");
                    if (stock == null) stock = BigDecimal.ZERO;
                    if (reorder == null) reorder = BigDecimal.ZERO;

                    InventoryReportCard card = new InventoryReportCard(
                            rs.getString("product_name"),
                            rs.getString("location_name") != null ? rs.getString("location_name") : "No Location",
                            stock,
                            reorder
                    );
                    cardsPanel.add(card);
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }

                if (!hasData) {
                    showNoDataMessage();
                }
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // ===== REPORT 2: STOCK MOVEMENT REPORT =====
    // Shows the number of stock movements and total quantities moved per product for the selected month/year
    private void loadStockMovementReport(int year, int month) {
        cardsPanel.removeAll();

        // Add date range header
        addDateRangeHeader("Stock Movement Report", year, month, "Movements during the month");

        String sql = "SELECT p.product_name, " +
                "SUM(CASE WHEN sm.movement_type = 'IN' THEN 1 ELSE 0 END) AS in_count, " +
                "SUM(CASE WHEN sm.movement_type = 'OUT' THEN 1 ELSE 0 END) AS out_count, " +
                "COALESCE(SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE 0 END), 0) AS in_qty, " +
                "COALESCE(SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END), 0) AS out_qty " +
                "FROM StockMovement sm " +
                "JOIN Product p ON p.product_id = sm.product_id " +
                "WHERE YEAR(sm.movement_date) = ? AND MONTH(sm.movement_date) = ? " +
                "GROUP BY p.product_id " +
                "HAVING in_count > 0 OR out_count > 0 " +
                "ORDER BY p.product_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    StockMovementReportCard card = new StockMovementReportCard(
                            rs.getString("product_name"),
                            rs.getInt("in_count"),
                            rs.getInt("out_count"),
                            rs.getBigDecimal("in_qty"),
                            rs.getBigDecimal("out_qty")
                    );
                    cardsPanel.add(card);
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }

                if (!hasData) {
                    showNoDataMessage();
                }
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // ===== REPORT 3: SUPPLIER DELIVERY REPORT =====
    // Shows the number of deliveries and total quantities received per supplier for the selected month/year
    private void loadSupplierDeliveryReport(int year, int month) {
        cardsPanel.removeAll();

        // Add date range header
        addDateRangeHeader("Supplier Delivery Report", year, month, "Deliveries received during the month");

        String sql = "SELECT s.supplier_name, " +
                "COUNT(*) AS delivery_count, " +
                "COALESCE(SUM(sm.quantity), 0) AS total_qty " +
                "FROM StockMovement sm " +
                "JOIN Supplier s ON s.supplier_id = sm.supplier_id " +
                "WHERE sm.movement_type = 'IN' " +
                "AND sm.supplier_id IS NOT NULL " +
                "AND YEAR(sm.movement_date) = ? " +
                "AND MONTH(sm.movement_date) = ? " +
                "GROUP BY s.supplier_id " +
                "HAVING delivery_count > 0 " +
                "ORDER BY s.supplier_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    SupplierDeliveryReportCard card = new SupplierDeliveryReportCard(
                            rs.getString("supplier_name"),
                            rs.getInt("delivery_count"),
                            rs.getBigDecimal("total_qty")
                    );
                    cardsPanel.add(card);
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }

                if (!hasData) {
                    showNoDataMessage();
                }
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // ===== REPORT 4: SALES REPORT =====
    // Shows average daily sales per product for the selected month/year
    private void loadSalesReport(int year, int month) {
        cardsPanel.removeAll();

        // Add date range header
        addDateRangeHeader("Sales Report", year, month, "Average daily sales for the month");

        String sql = "SELECT p.product_name, " +
                "COALESCE(SUM(sm.quantity), 0) AS total_sold, " +
                "COUNT(DISTINCT sm.movement_date) AS days_with_sales, " +
                "DAY(LAST_DAY(CONCAT(?, '-', LPAD(?, 2, '0'), '-01'))) AS days_in_month " +
                "FROM Product p " +
                "LEFT JOIN StockMovement sm ON p.product_id = sm.product_id " +
                "AND sm.movement_type = 'OUT' " +
                "AND YEAR(sm.movement_date) = ? " +
                "AND MONTH(sm.movement_date) = ? " +
                "WHERE p.product_status = 'Active' " +
                "GROUP BY p.product_id " +
                "HAVING total_sold > 0 " +
                "ORDER BY p.product_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set parameters for days_in_month calculation
            ps.setInt(1, year);
            ps.setInt(2, month);
            // Set parameters for WHERE clause
            ps.setInt(3, year);
            ps.setInt(4, month);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    BigDecimal totalSold = rs.getBigDecimal("total_sold");
                    int daysWithSales = rs.getInt("days_with_sales");
                    int daysInMonth = rs.getInt("days_in_month");
                    if (totalSold == null) totalSold = BigDecimal.ZERO;

                    // Calculate average daily sales across all days in the month
                    BigDecimal avgDaily = BigDecimal.ZERO;
                    if (daysInMonth > 0 && totalSold.compareTo(BigDecimal.ZERO) > 0) {
                        avgDaily = totalSold.divide(BigDecimal.valueOf(daysInMonth), 2, BigDecimal.ROUND_HALF_UP);
                    }

                    SalesReportCard card = new SalesReportCard(
                            rs.getString("product_name"),
                            totalSold,
                            daysWithSales,
                            avgDaily
                    );
                    cardsPanel.add(card);
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }

                if (!hasData) {
                    showNoDataMessage();
                }
            }

        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void showNoDataMessage() {
        JLabel noData = new JLabel("No data available for the selected period");
        noData.setFont(lexendRegular.deriveFont(14f));
        noData.setForeground(new Color(0x888888));
        noData.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardsPanel.add(Box.createVerticalGlue());
        cardsPanel.add(noData);
        cardsPanel.add(Box.createVerticalGlue());
    }

    private void addDateRangeHeader(String reportTitle, int year, int month, String description) {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lblTitle = new JLabel(reportTitle);
        lblTitle.setFont(lexendBold.deriveFont(16f));
        lblTitle.setForeground(ACCENT_COLOR);

        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String monthName = monthNames[month - 1];

        JLabel lblPeriod = new JLabel("Period: " + monthName + " " + year);
        lblPeriod.setFont(lexendBold.deriveFont(13f));
        lblPeriod.setForeground(new Color(0x333333));

        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(lexendRegular.deriveFont(11f));
        lblDesc.setForeground(new Color(0x666666));

        JPanel leftPanel = new JPanel(new BorderLayout(5, 3));
        leftPanel.setOpaque(false);
        leftPanel.add(lblTitle, BorderLayout.NORTH);
        leftPanel.add(lblDesc, BorderLayout.CENTER);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(lblPeriod, BorderLayout.EAST);

        cardsPanel.add(headerPanel);
        cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    // ===== INVENTORY REPORT CARD =====
    private class InventoryReportCard extends JPanel {
        public InventoryReportCard(String product, String location, BigDecimal stock, BigDecimal reorder) {
            setLayout(new BorderLayout(15, 10));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            setOpaque(true);

            // Left: Product info
            JPanel leftPanel = new JPanel(new BorderLayout(5, 3));
            leftPanel.setOpaque(false);

            JLabel lblProduct = new JLabel(product);
            lblProduct.setFont(lexendBold.deriveFont(14f));

            JLabel lblLocation = new JLabel(location);
            lblLocation.setFont(lexendRegular.deriveFont(12f));
            lblLocation.setForeground(new Color(0x666666));

            leftPanel.add(lblProduct, BorderLayout.NORTH);
            leftPanel.add(lblLocation, BorderLayout.CENTER);

            // Right: Stock info
            JPanel rightPanel = new JPanel(new GridLayout(2, 2, 15, 5));
            rightPanel.setOpaque(false);

            JLabel lblStockLabel = new JLabel("Current Stock:");
            lblStockLabel.setFont(lexendRegular.deriveFont(11f));
            lblStockLabel.setForeground(new Color(0x666666));

            JLabel lblStockValue = new JLabel(String.format("%.2f", stock));
            lblStockValue.setFont(lexendBold.deriveFont(18f));
            lblStockValue.setHorizontalAlignment(SwingConstants.RIGHT);

            String status;
            if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                status = "OUT OF STOCK";
                lblStockValue.setForeground(INACTIVE_RED);
            } else if (stock.compareTo(reorder) <= 0) {
                status = "LOW STOCK";
                lblStockValue.setForeground(WARNING_ORANGE);
            } else {
                status = "IN STOCK";
                lblStockValue.setForeground(ACTIVE_GREEN);
            }

            JLabel lblReorderLabel = new JLabel("Reorder Level:");
            lblReorderLabel.setFont(lexendRegular.deriveFont(11f));
            lblReorderLabel.setForeground(new Color(0x666666));

            JLabel lblReorderValue = new JLabel(String.format("%.2f", reorder));
            lblReorderValue.setFont(lexendRegular.deriveFont(12f));
            lblReorderValue.setHorizontalAlignment(SwingConstants.RIGHT);

            rightPanel.add(lblStockLabel);
            rightPanel.add(lblStockValue);
            rightPanel.add(lblReorderLabel);
            rightPanel.add(lblReorderValue);

            // Status badge
            JLabel lblStatus = createStatusBadge(status);

            JPanel rightContainer = new JPanel(new BorderLayout(10, 5));
            rightContainer.setOpaque(false);
            rightContainer.add(rightPanel, BorderLayout.CENTER);
            rightContainer.add(lblStatus, BorderLayout.EAST);

            add(leftPanel, BorderLayout.CENTER);
            add(rightContainer, BorderLayout.EAST);
        }
    }

    // ===== STOCK MOVEMENT REPORT CARD =====
    private class StockMovementReportCard extends JPanel {
        public StockMovementReportCard(String product, int inCount, int outCount, BigDecimal inQty, BigDecimal outQty) {
            setLayout(new BorderLayout(15, 10));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            setOpaque(true);

            // Left: Product name
            JLabel lblProduct = new JLabel(product);
            lblProduct.setFont(lexendBold.deriveFont(14f));

            // Right: Movement stats
            JPanel statsPanel = new JPanel(new GridLayout(2, 4, 10, 5));
            statsPanel.setOpaque(false);

            addStat(statsPanel, "IN Movements:", String.valueOf(inCount), ACTIVE_GREEN);
            addStat(statsPanel, "OUT Movements:", String.valueOf(outCount), INACTIVE_RED);
            addStat(statsPanel, "Total IN Qty:", String.format("%.2f", inQty != null ? inQty : BigDecimal.ZERO), ACTIVE_GREEN);
            addStat(statsPanel, "Total OUT Qty:", String.format("%.2f", outQty != null ? outQty : BigDecimal.ZERO), INACTIVE_RED);

            add(lblProduct, BorderLayout.WEST);
            add(statsPanel, BorderLayout.CENTER);
        }
    }

    // ===== SUPPLIER DELIVERY REPORT CARD =====
    private class SupplierDeliveryReportCard extends JPanel {
        public SupplierDeliveryReportCard(String supplier, int deliveryCount, BigDecimal totalQty) {
            setLayout(new BorderLayout(15, 10));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setOpaque(true);

            // Left: Supplier name
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);

            JLabel lblSupplier = new JLabel(supplier);
            lblSupplier.setFont(lexendBold.deriveFont(14f));

            leftPanel.add(lblSupplier, BorderLayout.CENTER);

            // Right: Delivery stats
            JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 5));
            statsPanel.setOpaque(false);

            addStat(statsPanel, "Deliveries:", String.valueOf(deliveryCount), new Color(0x2196F3));
            addStat(statsPanel, "Total Quantity:", String.format("%.2f", totalQty != null ? totalQty : BigDecimal.ZERO), ACTIVE_GREEN);

            add(leftPanel, BorderLayout.WEST);
            add(statsPanel, BorderLayout.CENTER);
        }
    }

    // ===== SALES REPORT CARD =====
    private class SalesReportCard extends JPanel {
        public SalesReportCard(String product, BigDecimal totalSold, int daysWithSales, BigDecimal avgDaily) {
            setLayout(new BorderLayout(15, 10));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            setOpaque(true);

            // Left: Product name
            JLabel lblProduct = new JLabel(product);
            lblProduct.setFont(lexendBold.deriveFont(14f));

            // Right: Sales stats
            JPanel statsPanel = new JPanel(new GridLayout(1, 6, 10, 5));
            statsPanel.setOpaque(false);

            addStat(statsPanel, "Total Sold:", String.format("%.2f", totalSold), salesColor);
            addStat(statsPanel, "Days with Sales:", String.valueOf(daysWithSales), new Color(0x2196F3));
            addStat(statsPanel, "Avg Daily Sales:", String.format("%.2f", avgDaily), ACTIVE_GREEN);

            add(lblProduct, BorderLayout.WEST);
            add(statsPanel, BorderLayout.CENTER);
        }
    }

    private void addStat(JPanel panel, String label, String value, Color valueColor) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(lexendRegular.deriveFont(10f));
        lblLabel.setForeground(new Color(0x666666));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(lexendBold.deriveFont(13f));
        lblValue.setForeground(valueColor);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(lblLabel);
        panel.add(lblValue);
    }

    private JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(status) {
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
        badge.setFont(lexendBold.deriveFont(10f));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        if (status.equals("OUT OF STOCK")) {
            badge.setBackground(INACTIVE_RED);
        } else if (status.equals("LOW STOCK")) {
            badge.setBackground(WARNING_ORANGE);
        } else {
            badge.setBackground(ACTIVE_GREEN);
        }

        return badge;
    }

    private void styleReportButton(RoundedButton btn) {
        btn.setForeground(Color.WHITE);
        btn.setFont(lexendBold.deriveFont(13f));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
    }

    // ===== CUSTOM COMPONENTS =====
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
        private Color backgroundColor;

        public RoundedTextField(int columns, Color bgColor) {
            super(columns);
            this.backgroundColor = bgColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setFont(lexendRegular);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
        }
    }

    public void refresh() {
        loadReport();
    }
}
