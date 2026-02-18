package guest.ui;

import common.model.User;
import common.service.UserService;
import common.session.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced Unified Login Dialog.
 *
 * Provides a premium, card-based UI with choice panel, admin/guest forms,
 * and a integrated registration flow.
 */
public class AdminLoginDialog extends JDialog {
    private boolean succeeded = false;
    private final JPanel cards = new JPanel(new CardLayout());
    private static final int CORNER_RADIUS = 16;

    public AdminLoginDialog(Frame parent) {
        super(parent, "Login", true);
        initUI(parent);
    }

    private void initUI(Frame parent) {
        setLayout(new BorderLayout());
        setUndecorated(true); // Remove default title bar for premium look
        setBackground(new Color(0, 0, 0, 0)); // Transparent background for rounded dialog

        // Main container with rounded corners
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIStyles.BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                // Subtle border
                g2.setColor(UIStyles.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        // Drag functionality for undecorated window
        WindowDragger dragger = new WindowDragger(this);
        mainContainer.addMouseListener(dragger);
        mainContainer.addMouseMotionListener(dragger);

        // Card Panel setup
        cards.setOpaque(false);
        cards.add(buildChoicePanel(), "choice");
        cards.add(buildAdminPanel(), "admin");
        cards.add(buildGuestPanel(), "guest");

        // Register panel
        GuestRegisterPanel registerPanel = new GuestRegisterPanel(u -> {
            succeeded = true;
            setVisible(false);
            dispose();
        });
        registerPanel.setBackListener(() -> showCard("guest"));
        registerPanel.setOpaque(false);
        cards.add(registerPanel, "register");

        mainContainer.add(cards, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);

        showCard("choice");
        setSize(400, 500);
        setLocationRelativeTo(parent);
    }

    private void showCard(String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
    }

    // -----------------------------------------------------------------------
    // Card 1: Choice Landing
    // -----------------------------------------------------------------------
    private JPanel buildChoicePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Close button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        closeBtn.setForeground(UIStyles.MUTED);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        top.add(closeBtn);
        // Need to add this top panel somewhere if we want it, but usually choice panel
        // doesn't have it on top of everything.

        JLabel icon = new JLabel("🏨");
        icon.setFont(icon.getFont().deriveFont(48f));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(icon);

        panel.add(Box.createVerticalStrut(20));

        JLabel title = new JLabel("Welcome back");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(22f));
        title.setForeground(UIStyles.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);

        panel.add(Box.createVerticalStrut(8));

        JLabel sub = new JLabel("How would you like to login?");
        sub.setFont(UIStyles.FONT_PLAIN);
        sub.setForeground(UIStyles.MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(sub);

        panel.add(Box.createVerticalStrut(40));

        panel.add(buildOptionButton("Guest", "I want to book rooms", "👤", e -> showCard("guest")));
        panel.add(Box.createVerticalStrut(16));
        panel.add(buildOptionButton("Admin", "Staff & Management", "🛠️", e -> showCard("admin")));

        panel.add(Box.createVerticalGlue());

        JButton cancelBtn = new JButton("Dismiss");
        cancelBtn.setFont(UIStyles.FONT_PLAIN);
        cancelBtn.setForeground(UIStyles.MUTED);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());
        panel.add(cancelBtn);

        return panel;
    }

    private JPanel buildOptionButton(String title, String desc, String emo, java.awt.event.ActionListener action) {
        JPanel btn = new JPanel(new BorderLayout(15, 0)) {
            private boolean hover = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        action.actionPerformed(null);
                    }
                });
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? new Color(245, 247, 250) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(hover ? UIStyles.PRIMARY : UIStyles.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(320, 70));
        btn.setMaximumSize(new Dimension(320, 70));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel emoLbl = new JLabel(emo);
        emoLbl.setFont(emoLbl.getFont().deriveFont(24f));
        btn.add(emoLbl, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UIStyles.FONT_BOLD);
        t.setForeground(UIStyles.TEXT);
        JLabel d = new JLabel(desc);
        d.setFont(UIStyles.FONT_PLAIN.deriveFont(11f));
        d.setForeground(UIStyles.MUTED);
        text.add(t);
        text.add(d);
        btn.add(text, BorderLayout.CENTER);

        return btn;
    }

    // -----------------------------------------------------------------------
    // Card 2: Admin Login
    // -----------------------------------------------------------------------
    private JPanel buildAdminPanel() {
        return buildFormPanel("Admin Login", "Staff credentials required",
                "Username", "Password", "admin", (user, pass) -> {
                    admin.service.AdminAuth auth = admin.service.AdminAuth.getInstance();
                    if (auth.login(user, pass.toCharArray())) {
                        succeeded = true;
                        setVisible(false);
                        dispose();
                        SwingUtilities.invokeLater(() -> new admin.ui.AdminMenu());
                        return true;
                    }
                    return false;
                });
    }

    private JPanel buildGuestPanel() {
        JPanel panel = buildFormPanel("Guest Login", "Welcome traveler",
                "Email Address", "Password", "guest", (user, pass) -> {
                    User u = UserService.getInstance().authenticate(user, pass);
                    if (u != null) {
                        succeeded = true;
                        SessionManager.login(u);
                        setVisible(false);
                        dispose();
                        return true;
                    }
                    return false;
                });

        // Add "Register" link at bottom
        JPanel footer = (JPanel) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel reg = new JLabel("Join us? Create account");
        reg.setFont(UIStyles.FONT_PLAIN.deriveFont(12f));
        reg.setForeground(UIStyles.PRIMARY);
        reg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reg.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCard("register");
            }
        });
        left.add(reg);
        footer.add(left, BorderLayout.WEST);

        return panel;
    }

    private JPanel buildFormPanel(String titleStr, String subTitle, String uLabel, String pLabel, String type,
            AuthAction action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel(titleStr);
        t.setFont(UIStyles.FONT_BOLD.deriveFont(20f));
        t.setForeground(UIStyles.TEXT);
        header.add(t, BorderLayout.NORTH);
        JLabel s = new JLabel(subTitle);
        s.setFont(UIStyles.FONT_PLAIN.deriveFont(12f));
        s.setForeground(UIStyles.MUTED);
        header.add(s, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // Center: Inputs
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(Box.createVerticalStrut(30));

        center.add(new JLabel(uLabel) {
            {
                setFont(UIStyles.FONT_BOLD.deriveFont(12f));
                setForeground(UIStyles.TEXT);
            }
        });
        center.add(Box.createVerticalStrut(8));
        RoundedTextField uField = new RoundedTextField("", 12);
        uField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        center.add(uField);

        center.add(Box.createVerticalStrut(20));

        center.add(new JLabel(pLabel) {
            {
                setFont(UIStyles.FONT_BOLD.deriveFont(12f));
                setForeground(UIStyles.TEXT);
            }
        });
        center.add(Box.createVerticalStrut(8));
        RoundedPasswordField pField = new RoundedPasswordField(12);
        pField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        center.add(pField);

        panel.add(center, BorderLayout.CENTER);

        // Footer: Buttons
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(Box.createVerticalStrut(60), BorderLayout.CENTER); // Spacer

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton back = new JButton("Back");
        UIStyles.styleSecondaryButton(back);
        back.addActionListener(e -> showCard("choice"));

        JButton login = new JButton("Sign In");
        UIStyles.stylePrimaryButton(login);
        login.setPreferredSize(new Dimension(100, 38));
        login.addActionListener(e -> {
            if (action.perform(uField.getText(), new String(pField.getPassword()))) {
                // Done
            } else {
                JOptionPane.showMessageDialog(this, "The credentials you entered are incorrect.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                pField.setText("");
            }
        });

        btns.add(back);
        btns.add(login);
        footer.add(btns, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    interface AuthAction {
        boolean perform(String u, String p);
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    /** Helper for dragging window. */
    private static class WindowDragger extends java.awt.event.MouseAdapter {
        private final JDialog dialog;
        private Point startPoint;

        public WindowDragger(JDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            startPoint = e.getPoint();
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            Point current = e.getLocationOnScreen();
            dialog.setLocation(current.x - startPoint.x, current.y - startPoint.y);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private final int radius;

        public RoundedPasswordField(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setFont(UIStyles.FONT_PLAIN);
            setCaretColor(UIStyles.PRIMARY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(isFocusOwner() ? UIStyles.PRIMARY : UIStyles.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
