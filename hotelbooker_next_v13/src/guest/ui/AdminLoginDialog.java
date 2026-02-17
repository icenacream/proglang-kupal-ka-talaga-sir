package guest.ui;

import javax.swing.*;
import java.awt.*;

public class AdminLoginDialog extends JDialog {
    private boolean succeeded = false;

    public AdminLoginDialog(Frame parent) {
        super(parent, "Admin Login", true);
        initUI(parent);
    }

    private void initUI(Frame parent) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));
        panel.setBackground(UIStyles.BG);

        // Title
        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
        title.setFont(UIStyles.FONT_BOLD.deriveFont(16f));
        title.setForeground(UIStyles.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(6, 6, 12, 6));
        panel.add(title, BorderLayout.NORTH);

        // Center form area
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UIStyles.FONT_BOLD);
        userLabel.setForeground(UIStyles.TEXT);
        center.add(userLabel);
        center.add(Box.createVerticalStrut(6));

        RoundedTextField userField = new RoundedTextField("", 10);
        userField.setFont(UIStyles.FONT_PLAIN);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        center.add(userField);
        center.add(Box.createVerticalStrut(10));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UIStyles.FONT_BOLD);
        passLabel.setForeground(UIStyles.TEXT);
        center.add(passLabel);
        center.add(Box.createVerticalStrut(6));

        // Rounded password field with matching visuals
        RoundedPasswordField passField = new RoundedPasswordField(10);
        passField.setFont(UIStyles.FONT_PLAIN);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        center.add(passField);

        panel.add(center, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        UIStyles.styleSecondaryButton(cancel);
        cancel.setBorder(new RoundBorder(8));
        cancel.setPreferredSize(new Dimension(100, 36));

        JButton login = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        UIStyles.stylePrimaryButton(login);
        login.setBorder(new RoundBorder(8));
        login.setPreferredSize(new Dimension(110, 36));

        actions.add(cancel);
        actions.add(login);
        panel.add(actions, BorderLayout.SOUTH);

        // Wire actions (behavior preserved exactly)
        login.addActionListener(e -> {
            String user = userField.getText();
            char[] passChars = passField.getPassword();
            try {
                if (user == null || user.trim().isEmpty() || passChars == null || passChars.length == 0) {
                    JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                admin.service.AdminAuth auth = admin.service.AdminAuth.getInstance();
                boolean ok = auth.login(user, passChars);
                if (ok) {
                    succeeded = true;
                    setVisible(false);
                    dispose();
                    SwingUtilities.invokeLater(() -> new admin.ui.AdminMenu());
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Clear sensitive data
                if (passChars != null) {
                    for (int i = 0; i < passChars.length; i++) passChars[i] = '\0';
                }
                passField.setText("");
            }
        });

        cancel.addActionListener(e -> {
            succeeded = false;
            setVisible(false);
            dispose();
        });

        getRootPane().setDefaultButton(login); // Enter triggers login

        getContentPane().add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public boolean isSucceeded() { return succeeded; }

    /**
     * Small rounded password field to match rounded text fields visually.
     */
    private static class RoundedPasswordField extends JPasswordField {
        private int radius;

        public RoundedPasswordField(int radius) {
            super();
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(UIStyles.BG);
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2d.setColor(UIStyles.BORDER);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
