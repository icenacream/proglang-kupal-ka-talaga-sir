package admin.ui;

import admin.service.AdminAuth;
import guest.ui.UIStyles;

import javax.swing.*;
import java.awt.*;

/**
 * Left navigation sidebar for admin UI. Fixed width and consistent styling.
 * Shows items based on staff role.
 */
public class AdminSidebarPanel extends JPanel {
    private JButton dashboardBtn;
    private JButton hotelsBtn;
    private JButton availabilityBtn;
    private JButton bookingsBtn;
    private JButton guestsBtn;
    private JButton reviewsBtn;
    private JButton promoBtn;
    private JButton reportsBtn;
    private JButton settingsBtn;
    private JButton logoutBtn;

    public AdminSidebarPanel(AdminController controller) {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        setPreferredSize(new Dimension(260, Integer.MAX_VALUE));

        AdminAuth auth = AdminAuth.getInstance();
        AdminAuth.Role role = auth.getCurrentRole();
        boolean isAdmin = auth.isAdmin();

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel title = new JLabel("HotelManager");
        title.setFont(UIStyles.FONT_TITLE);
        title.setForeground(UIStyles.PRIMARY);

        JLabel subtitle = new JLabel(isAdmin ? "Admin Portal" : "Staff Portal");
        subtitle.setFont(UIStyles.FONT_PLAIN);
        subtitle.setForeground(UIStyles.MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4,0,12,0));

        top.add(title);
        top.add(subtitle);

        // Menu
        dashboardBtn = createMenuButton("Dashboard");
        hotelsBtn = createMenuButton("Hotels");
        availabilityBtn = createMenuButton("Availability");
        bookingsBtn = createMenuButton("Bookings");
        guestsBtn = createMenuButton("Guests");
        reviewsBtn = createMenuButton("Reviews");
        promoBtn = createMenuButton("Promo Codes");
        reportsBtn = createMenuButton("Reports");
        settingsBtn = createMenuButton("Settings");

        top.add(dashboardBtn);
        top.add(Box.createVerticalStrut(8));

        // Admin-only tools
        if (isAdmin) {
            top.add(hotelsBtn);
            top.add(Box.createVerticalStrut(8));
        }

        top.add(availabilityBtn);
        top.add(Box.createVerticalStrut(8));
        top.add(bookingsBtn);
        top.add(Box.createVerticalStrut(8));

        if (isAdmin) {
            top.add(guestsBtn);
            top.add(Box.createVerticalStrut(8));
        }

        top.add(reviewsBtn);
        top.add(Box.createVerticalStrut(8));

        if (isAdmin) {
            top.add(promoBtn);
            top.add(Box.createVerticalStrut(8));
            top.add(reportsBtn);
            top.add(Box.createVerticalStrut(8));
            top.add(settingsBtn);
        }

        add(top, BorderLayout.NORTH);

        // Bottom: user info + logout
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));

        JPanel userInfo = new JPanel(new BorderLayout());
        userInfo.setOpaque(false);

        String name = auth.getCurrentUser() == null ? "" : auth.getCurrentUser();
        JLabel userLabel = new JLabel(name.isEmpty() ? "Staff" : name);
        userLabel.setFont(UIStyles.FONT_BOLD);

        JLabel userRole = new JLabel(role == null ? "STAFF" : role.name());
        userRole.setFont(UIStyles.FONT_PLAIN);
        userRole.setForeground(UIStyles.MUTED);

        userInfo.add(userLabel, BorderLayout.NORTH);
        userInfo.add(userRole, BorderLayout.SOUTH);

        logoutBtn = createMenuButton("Logout");

        bottom.add(Box.createVerticalGlue());
        bottom.add(userInfo);
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(logoutBtn);

        add(bottom, BorderLayout.SOUTH);

        // Wire navigation
        dashboardBtn.addActionListener(controller.navigate("dashboard"));
        hotelsBtn.addActionListener(controller.navigate("hotels"));
        availabilityBtn.addActionListener(controller.navigate("availability"));
        bookingsBtn.addActionListener(controller.navigate("bookings"));
        guestsBtn.addActionListener(controller.navigate("guests"));
        reviewsBtn.addActionListener(controller.navigate("reviews"));
        promoBtn.addActionListener(controller.navigate("promocodes"));
        reportsBtn.addActionListener(controller.navigate("reports"));
        settingsBtn.addActionListener(controller.navigate("settings"));
        logoutBtn.addActionListener(controller.logoutAction());
    }

    private JButton createMenuButton(String text) {
        JButton b = new JButton(text);
        b.setFont(UIStyles.FONT_PLAIN);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setBackground(UIStyles.BG);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        return b;
    }
}
