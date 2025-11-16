import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

/**
 * ReportPanel
 *
 * Shows 4 different reports:
 *  1) Inventory Report
 *  2) Stock Movement Report
 *  3) Supplier Delivery Report
 *  4) Sales Report
 *
 * UI:
 *  - 4 big buttons to choose the report
 *  - Year + Month filters
 *  - No "Generate" button; reports load automatically on click / filter change
 */
public class ReportPanel extends JPanel {

    private final JTable table = new JTable();
    private final DefaultTableModel model;

    private final JTextField tfYear;
    private final JComboBox<String> cbMonth;

    private final JButton btnInventory;
    private final JButton btnStockMovement;
    private final JButton btnSupplierDelivery;
    private final JButton btnSales; // Sales Report button

    // 0 = Inventory, 1 = Stock Movement, 2 = Supplier Delivery, 3 = Sales
    private int currentReportType = 0;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));

        // ====== TABLE MODEL (LOCKED) ======
        model = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Lock all cells; reports are view-only
                return false;
            }
        };

        // ====== TOP FILTER BAR (Year + Month) ======
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        tfYear = new JTextField(String.valueOf(currentYear), 4);

        cbMonth = new JComboBox<>(new String[]{
                "01 - January",
                "02 - February",
                "03 - March",
                "04 - April",
                "05 - May",
                "06 - June",
                "07 - July",
                "08 - August",
                "09 - September",
                "10 - October",
                "11 - November",
                "12 - December"
        });
        cbMonth.setSelectedIndex(currentMonth - 1);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBar.add(new JLabel("Year:"));
        filterBar.add(tfYear);
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(new JLabel("Month:"));
        filterBar.add(cbMonth);

        // ====== REPORT BUTTONS (BIG) ======
        btnInventory        = new JButton("Inventory Report");
        btnStockMovement    = new JButton("Stock Movement Report");
        btnSupplierDelivery = new JButton("Supplier Delivery Report");
        btnSales            = new JButton("Sales Report");

        Dimension bigButtonSize = new Dimension(200, 60);
        btnInventory.setPreferredSize(bigButtonSize);
        btnStockMovement.setPreferredSize(bigButtonSize);
        btnSupplierDelivery.setPreferredSize(bigButtonSize);
        btnSales.setPreferredSize(bigButtonSize);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.add(btnInventory);
        buttonPanel.add(btnStockMovement);
        buttonPanel.add(btnSupplierDelivery);
        buttonPanel.add(btnSales);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterBar, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ====== TABLE AREA ======
        table.setModel(model);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null); // table read-only

        // Disable selection entirely (display-only)
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // harmless, selection is disabled anyway

        // Styling to match other panels
        table.setRowHeight(30);
        table.setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 28));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ====== EVENT HANDLERS ======

        // Report buttons -> switch report + auto-load
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

        // Month change -> auto-refresh current report
        cbMonth.addActionListener(e -> loadReport());

        // Press Enter in Year field -> auto-refresh current report
        tfYear.addActionListener(e -> loadReport());

        // Initial visual and default report
        highlightSelectedButton();
        loadReport();
    }

    // Simple visual cue which report is active
    private void highlightSelectedButton() {
        JButton[] buttons = {btnInventory, btnStockMovement, btnSupplierDelivery, btnSales};
        for (int i = 0; i < buttons.length; i++) {
            if (i == currentReportType) {
                buttons[i].setBackground(new Color(0xCCE5FF));
            } else {
                buttons[i].setBackground(UIManager.getColor("Button.background"));
            }
        }
    }

    private int parseYear() {
        try {
            return Integer.parseInt(tfYear.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid year (e.g., 2025).",
                    "Invalid Year",
                    JOptionPane.ERROR_MESSAGE
            );
            return -1;
        }
    }

    private int getSelectedMonth() {
        return cbMonth.getSelectedIndex() + 1; // 1-12
    }

    // Decide which report to run
    private void loadReport() {
        int year = parseYear();
        if (year <= 0) return;
        int month = getSelectedMonth();

        switch (currentReportType) {
            case 0 -> loadInventoryReport(year, month);
            case 1 -> loadStockMovementReport(year, month);
            case 2 -> loadSupplierDeliveryReport(year, month);
            case 3 -> loadSalesReport(year, month);
        }
    }

    // ----------------------------------------------------
    //  Report 1: Inventory Report
    // ----------------------------------------------------
    private void loadInventoryReport(int year, int month) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
                "Product",
                "Location",
                "Stock",
                "Reorder Level",
                "Stock Status"
        });

        String sql =
                "SELECT p.product_name, l.location_name, " +
                        "       SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE -sm.quantity END) AS stock, " +
                        "       p.reorder_level " +
                        "FROM StockMovement sm " +
                        "JOIN Product p ON p.product_id = sm.product_id " +
                        "JOIN StorageLocation l ON l.location_id = sm.location_id " +
                        "WHERE YEAR(sm.movement_date) = ? AND MONTH(sm.movement_date) = ? " +
                        "GROUP BY p.product_id, l.location_id " +
                        "ORDER BY p.product_name, l.location_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal stock = rs.getBigDecimal("stock");
                    BigDecimal reorder = rs.getBigDecimal("reorder_level");
                    if (stock == null) stock = BigDecimal.ZERO;
                    if (reorder == null) reorder = BigDecimal.ZERO;

                    String status;
                    if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                        status = "OUT OF STOCK";
                    } else if (stock.compareTo(reorder) <= 0) {
                        status = "LOW";
                    } else {
                        status = "OK";
                    }

                    model.addRow(new Object[]{
                            rs.getString("product_name"),
                            rs.getString("location_name"),
                            stock,
                            reorder,
                            status
                    });
                }
            }

            alignNumericColumns();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // ----------------------------------------------------
    //  Report 2: Stock Movement Report
    // ----------------------------------------------------
    private void loadStockMovementReport(int year, int month) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
                "Product",
                "IN Movements",
                "OUT Movements",
                "Total IN Qty",
                "Total OUT Qty"
        });

        String sql =
                "SELECT p.product_name, " +
                        "       SUM(CASE WHEN sm.movement_type = 'IN'  THEN 1 ELSE 0 END) AS in_count, " +
                        "       SUM(CASE WHEN sm.movement_type = 'OUT' THEN 1 ELSE 0 END) AS out_count, " +
                        "       SUM(CASE WHEN sm.movement_type = 'IN'  THEN sm.quantity ELSE 0 END) AS in_qty, " +
                        "       SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END) AS out_qty " +
                        "FROM StockMovement sm " +
                        "JOIN Product p ON p.product_id = sm.product_id " +
                        "WHERE YEAR(sm.movement_date) = ? AND MONTH(sm.movement_date) = ? " +
                        "GROUP BY p.product_id " +
                        "ORDER BY p.product_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("product_name"),
                            rs.getInt("in_count"),
                            rs.getInt("out_count"),
                            rs.getBigDecimal("in_qty"),
                            rs.getBigDecimal("out_qty")
                    });
                }
            }

            alignNumericColumns();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // ----------------------------------------------------
    //  Report 3: Supplier Delivery Report
    // ----------------------------------------------------
    private void loadSupplierDeliveryReport(int year, int month) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
                "Supplier",
                "Delivery Count",
                "Total Quantity Delivered"
        });

        String sql =
                "SELECT s.supplier_name, " +
                        "       COUNT(*) AS delivery_count, " +
                        "       SUM(sm.quantity) AS total_qty " +
                        "FROM StockMovement sm " +
                        "JOIN Supplier s ON s.supplier_id = sm.supplier_id " +
                        "WHERE sm.movement_type = 'IN' " +
                        "  AND sm.supplier_id IS NOT NULL " +
                        "  AND YEAR(sm.movement_date) = ? AND MONTH(sm.movement_date) = ? " +
                        "GROUP BY s.supplier_id " +
                        "ORDER BY s.supplier_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("supplier_name"),
                            rs.getInt("delivery_count"),
                            rs.getBigDecimal("total_qty")
                    });
                }
            }

            alignNumericColumns();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // ----------------------------------------------------
    //  Report 4: Sales Report
    // ----------------------------------------------------
    private void loadSalesReport(int year, int month) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
                "Product",
                "Total Sold Qty",
                "Days with Sales",
                "Avg Daily Sales"
        });

        String sql =
                "SELECT p.product_name, " +
                        "       SUM(sm.quantity) AS total_sold, " +
                        "       COUNT(DISTINCT sm.movement_date) AS days_with_sales " +
                        "FROM StockMovement sm " +
                        "JOIN Product p ON p.product_id = sm.product_id " +
                        "WHERE sm.movement_type = 'OUT' " +
                        "  AND YEAR(sm.movement_date) = ? AND MONTH(sm.movement_date) = ? " +
                        "GROUP BY p.product_id " +
                        "ORDER BY p.product_name";

        try (Connection conn = DBUtils.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal totalSold = rs.getBigDecimal("total_sold");
                    int days = rs.getInt("days_with_sales");
                    if (totalSold == null) totalSold = BigDecimal.ZERO;

                    BigDecimal avgDaily = BigDecimal.ZERO;
                    if (days > 0) {
                        avgDaily = totalSold.divide(
                                BigDecimal.valueOf(days),
                                2,
                                BigDecimal.ROUND_HALF_UP
                        );
                    }

                    model.addRow(new Object[]{
                            rs.getString("product_name"),
                            totalSold,
                            days,
                            avgDaily
                    });
                }
            }

            alignNumericColumns();
        } catch (SQLException ex) {
            DBUtils.showErr(ex);
        }
    }

    // Right-align numeric columns
    private void alignNumericColumns() {
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            Class<?> colClass = table.getColumnClass(i);
            if (Number.class.isAssignableFrom(colClass)) {
                table.getColumnModel().getColumn(i).setCellRenderer(right);
            }
        }
    }

    // Optional: if main frame calls this when tab is revisited
    public void refresh() {
        loadReport();
    }
}
