import javax.swing.*;
import java.awt.*;

public class CloudKitchenApp extends JFrame {

    public CloudKitchenApp() {
        super("Cloud Kitchen Inventory (MySQL)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        ReportPanel reportPanel = new ReportPanel();

        tabs.addTab("Products", new ProductPanel());
        tabs.addTab("Suppliers", new SupplierPanel());
        tabs.addTab("Storage Locations", new LocationPanel());
        tabs.addTab("Stock Movements", new MovementPanel(reportPanel));
        tabs.addTab("Reports", reportPanel);

        add(tabs, BorderLayout.CENTER);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new CloudKitchenApp().setVisible(true));
    }
}
