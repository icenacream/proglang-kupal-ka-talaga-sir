package guest.ui;

import common.model.User;
import common.service.UserService;
import common.session.SessionManager;

import javax.swing.*;
import java.awt.*;

public class GuestLoginPanel extends JPanel {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JButton loginButton = new JButton("Login");
    private final JButton cancelButton = new JButton("Cancel");
    private final JLabel switchToRegister = new JLabel("Don't have an account? Sign Up");

    public interface LoginListener { void onLoginSuccess(User u); }

    public GuestLoginPanel(LoginListener listener) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(UIStyles.BG);

        JLabel title = new JLabel("Guest Login");
        title.setFont(UIStyles.FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(label("Email"));
        emailField.setFont(UIStyles.FONT_PLAIN);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        emailField.setBackground(UIStyles.BG);
        emailField.setForeground(UIStyles.TEXT);
        form.add(emailField);
        form.add(Box.createVerticalStrut(12));

        form.add(label("Password"));
        passField.setFont(UIStyles.FONT_PLAIN);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passField.setBackground(UIStyles.BG);
        passField.setForeground(UIStyles.TEXT);
        passField.setCaretColor(UIStyles.TEXT);
        form.add(passField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        loginButton.setFont(UIStyles.FONT_PLAIN);
        cancelButton.setFont(UIStyles.FONT_PLAIN);
        UIStyles.stylePrimaryButton(loginButton);
        UIStyles.styleSecondaryButton(cancelButton);

        buttons.add(cancelButton);
        buttons.add(loginButton);
        bottom.add(buttons, BorderLayout.EAST);

        switchToRegister.setFont(UIStyles.FONT_PLAIN);
        switchToRegister.setForeground(UIStyles.PRIMARY);
        switchToRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        switchPanel.setOpaque(false);
        switchPanel.add(switchToRegister);
        bottom.add(switchPanel, BorderLayout.WEST);

        add(bottom, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });

        loginButton.addActionListener(e -> doLogin(listener));
        passField.addActionListener(e -> doLogin(listener));
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIStyles.FONT_PLAIN);
        l.setForeground(UIStyles.TEXT);
        l.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
        return l;
    }

    private void doLogin(LoginListener listener) {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = new String(passField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password.", "Login", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // perform on background thread
        loginButton.setEnabled(false);
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return UserService.getInstance().authenticate(email, pass);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User u = get();
                    if (u == null) {
                        JOptionPane.showMessageDialog(GuestLoginPanel.this, "Invalid email or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SessionManager.login(u);
                    if (listener != null) listener.onLoginSuccess(u);
                    Window w = SwingUtilities.getWindowAncestor(GuestLoginPanel.this);
                    if (w != null) w.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GuestLoginPanel.this, "Login error.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public JLabel getSwitchToRegisterLabel() { return switchToRegister; }
}
