package admin.ui;

import javax.swing.*;
import java.awt.*;
import admin.service.AdminAuth;

public class AdminMenu extends JFrame {
    
    public AdminMenu() {
        // Protect access: only allow construction when logged in
        if (!admin.service.AdminAuth.getInstance().isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Access denied. Please login as admin.", "Unauthorized", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        // Launch the modern AdminFrame and close this placeholder frame
        SwingUtilities.invokeLater(() -> new AdminFrame());
        dispose();
    }

    @SuppressWarnings("unused")
    private void initializeUI() {
        setTitle("HotelBooker - Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Create admin panel
        JPanel adminPanel = createAdminPanel();
        add(adminPanel);

        setVisible(true);
    }

    private JPanel createAdminPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(26, 115, 232));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("  Admin Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Navigation Panel
        JPanel navPanel = new JPanel();
        navPanel.setBackground(Color.WHITE);
        navPanel.setPreferredSize(new Dimension(200, 0));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] navItems = {"Dashboard", "Manage Rooms", "View Bookings", "Transactions", "Settings", "Logout"};
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        for (String item : navItems) {
            JButton navButton = new JButton(item);
            navButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            navButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            navButton.setMaximumSize(new Dimension(180, 40));
            navPanel.add(navButton);
            navPanel.add(Box.createVerticalStrut(10));

            // Wire simple actions: update content area or perform logout
            navButton.addActionListener(ae -> {
                switch (item) {
                    case "Logout":
                        AdminAuth.getInstance().logout();
                        // Close admin window and leave guest UI running
                        SwingUtilities.invokeLater(() -> {
                            Window w = SwingUtilities.getWindowAncestor(navButton);
                            if (w != null) w.dispose();
                        });
                        break;
                    default:
                        contentArea.removeAll();
                        JLabel lbl = new JLabel("" + item, SwingConstants.LEFT);
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
                        contentArea.add(lbl, BorderLayout.NORTH);
                        contentArea.revalidate();
                        contentArea.repaint();
                        break;
                }
            });
        }

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Default content area (will be updated by nav actions)
        JLabel welcomeLabel = new JLabel("Welcome to Admin Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        contentPanel.add(welcomeLabel, BorderLayout.NORTH);
        // place the dynamic content area created earlier
        contentPanel.add(contentArea, BorderLayout.CENTER);

        // Assemble main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(navPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }
}
