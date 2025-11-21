import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class CloudKitchenApp extends JFrame {

    private final Color ACCENT_COLOR = new Color(0xFF914D);
    private final Color BG_COLOR = new Color(0xEBEBEB);
    private final Color SIDEBAR_BG = new Color(0xFEFEFE);
    private final Color BUTTON_HOVER = new Color(0xD8D8D8);
    private final JPanel mainPanel = new RoundedCornerPanel(20); // 30px radius
    private JButton selectedButton = null;

    Font lexendRegular = FontUtils.loadFont("/resources/fonts/lexend-regular.ttf", 14f);
    Font lexendBold = FontUtils.loadFont("/resources/fonts/lexend-bold.ttf", 14f);

    public CloudKitchenApp() {
        super("Cloud Kitchen Inventory");

        ImageIcon icon = new ImageIcon("src/resources/icons/cloudkitchenapp.png"); // path relative to project root
        setIconImage(icon.getImage());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setUndecorated(false); // remove default title bar

        // ----------------- CUSTOM TITLE BAR -----------------
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // center aligned
        titleBar.setPreferredSize(new Dimension(getWidth(), 50));
        titleBar.setBackground(SIDEBAR_BG);

        // App icon
        ImageIcon appIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/resources/icons/cloudkitchenapp.png"))
                        .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)
        );

        JLabel iconLabel = new JLabel(appIcon);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 5)); // spacing

        // App title
        JLabel titleLabel = new JLabel("Cloud Kitchen Inventory");
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(lexendBold);
        titleLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // App panel
        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        leftTitle.setOpaque(false);
        leftTitle.add(iconLabel);
        leftTitle.add(titleLabel);

        titleBar.add(leftTitle, BorderLayout.WEST);

        // Draggable window
        final Point point = new Point();
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                point.x = e.getX();
                point.y = e.getY();
            }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - point.x, e.getYOnScreen() - point.y);
            }
        });

        // ----------------- SIDEBAR -----------------
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        // Create buttons with normal and active icons
        JButton btnProducts = createSidebarButton(
                "Products",
                "/resources/icons/products.png",        // normal
                "/resources/icons/products_active.png"  // active/selected
        );

        JButton btnSuppliers = createSidebarButton(
                "Suppliers",
                "/resources/icons/suppliers.png",
                "/resources/icons/suppliers_active.png"
        );

        JButton btnLocations = createSidebarButton(
                "Storage Locations",
                "/resources/icons/locations.png",
                "/resources/icons/locations_active.png"
        );

        JButton btnMovements = createSidebarButton(
                "Stock Movements",
                "/resources/icons/movements.png",
                "/resources/icons/movements_active.png"
        );

        JButton btnReports = createSidebarButton(
                "Reports",
                "/resources/icons/reports.png",
                "/resources/icons/reports_active.png"
        );

        // Add buttons with spacing
        sidebar.add(btnProducts);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnSuppliers);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnLocations);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnMovements);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnReports);
        sidebar.add(Box.createVerticalGlue());

        // Set default view
        switchPanel(new ProductPanel(), btnProducts);

        // ----------------- MAIN PANEL -----------------
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        switchPanel(new ProductPanel(), btnProducts);

        // Button actions
        btnProducts.addActionListener(e -> switchPanel(new ProductPanel(), btnProducts));
        btnSuppliers.addActionListener(e -> switchPanel(new SupplierPanel(), btnSuppliers));
        btnLocations.addActionListener(e -> switchPanel(new LocationPanel(), btnLocations));
        btnMovements.addActionListener(e -> switchPanel(new MovementPanel(new ReportPanel()), btnMovements));
        btnReports.addActionListener(e -> switchPanel(new ReportPanel(), btnReports));

        // ----------------- LAYOUT -----------------
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titleBar, BorderLayout.NORTH);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text, String iconPathNormal, String iconPathActive) {
        JButton button = new RoundedButton(text, 20);
        button.setFocusPainted(false);
        button.setFont(lexendRegular.deriveFont(Font.PLAIN, 14f));
        button.setBackground(SIDEBAR_BG);
        button.setForeground(Color.BLACK);

        // Remove all default borders / background
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        // Icon / text alignment
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(10);
        button.setBorder(new EmptyBorder(0, 10, 0, 0)); // shifts icon + text right

        // Load & scale icons
        ImageIcon iconNormal = new ImageIcon(
                new ImageIcon(getClass().getResource(iconPathNormal))
                        .getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)
        );
        ImageIcon iconActive = new ImageIcon(
                new ImageIcon(getClass().getResource(iconPathActive))
                        .getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)
        );

        button.setIcon(iconNormal);

        // Size
        Dimension size = new Dimension(200, 60);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        button.setPreferredSize(size);

        // Store icons
        button.putClientProperty("iconNormal", iconNormal);
        button.putClientProperty("iconActive", iconActive);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    animateBackground(button, button.getBackground(), BUTTON_HOVER, 150);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    animateBackground(button, button.getBackground(), SIDEBAR_BG, 150);
                }
            }
        });

        return button;
    }

    class RoundedButton extends JButton {
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

            super.paintComponent(g);
        }
    }

    class RoundedCornerPanel extends JPanel {
        private int radius;

        public RoundedCornerPanel(int radius) {
            this.radius = radius;
            setOpaque(false); // important
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Build path with top-left rounded corner
            java.awt.geom.Path2D.Float path = new java.awt.geom.Path2D.Float();
            path.moveTo(radius, 0);       // start after top-left curve
            path.lineTo(w, 0);            // top-right
            path.lineTo(w, h);            // bottom-right
            path.lineTo(0, h);            // bottom-left
            path.lineTo(0, radius);       // left edge
            path.quadTo(0, 0, radius, 0); // top-left rounded
            path.closePath();

            // Paint background inside the path
            g2.setColor(getBackground());
            g2.fill(path);

            // Clip to the path so children respect the shape
            g2.setClip(path);

            // Paint child components
            super.paintComponent(g2);

            g2.dispose();
        }
    }

    private Color lerp(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int b2 = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, b2);
    }

    private void animateBackground(JButton btn, Color start, Color end, int duration) {
        Timer timer = new Timer(10, null);

        final long startTime = System.currentTimeMillis();

        timer.addActionListener(e -> {
            float t = (System.currentTimeMillis() - startTime) / (float) duration;
            if (t >= 1f) {
                t = 1f;
                timer.stop();
            }

            btn.setBackground(lerp(start, end, t));
            btn.repaint();
        });

        timer.start();
    }

    private void switchPanel(JPanel panel, JButton button) {
        mainPanel.removeAll();
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();

        if (selectedButton != null) {
            animateBackground(selectedButton, selectedButton.getBackground(), SIDEBAR_BG, 180);
            selectedButton.setForeground(Color.BLACK);
            selectedButton.setIcon((ImageIcon) selectedButton.getClientProperty("iconNormal"));
        }

        // Animate new selected button
        animateBackground(button, button.getBackground(), ACCENT_COLOR, 180);
        button.setForeground(new Color(0x803100));
        button.setIcon((ImageIcon) button.getClientProperty("iconActive"));

        selectedButton = button;
    }

    public class FontUtils {

        public static Font loadFont(String path, float size) {
            try {
                InputStream is = FontUtils.class.getResourceAsStream(path);
                Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                return font.deriveFont(size);
            } catch (Exception e) {
                System.err.println("Failed to load font: " + path);
                e.printStackTrace();
                return new Font("Segoe UI", Font.PLAIN, (int) size); // fallback
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CloudKitchenApp().setVisible(true));
    }
}
