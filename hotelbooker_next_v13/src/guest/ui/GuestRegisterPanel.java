package guest.ui;

import common.model.User;
import common.service.UserService;
import common.session.SessionManager;

import javax.swing.*;
import java.awt.*;

public class GuestRegisterPanel extends JPanel {
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JPasswordField passConfirm = new JPasswordField();
    private final JButton registerButton = new JButton("Register");
    private final JButton backButton = new JButton("Back to Login");

    public interface RegisterListener { void onRegistered(User u); }

    public GuestRegisterPanel(RegisterListener listener) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(UIStyles.BG);

        JLabel title = new JLabel("Create Account");
        title.setFont(UIStyles.FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(label("Full Name"));
        nameField.setFont(UIStyles.FONT_PLAIN);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        nameField.setBackground(UIStyles.BG);
        nameField.setForeground(UIStyles.TEXT);
        form.add(nameField);
        form.add(Box.createVerticalStrut(10));

        form.add(label("Email"));
        emailField.setFont(UIStyles.FONT_PLAIN);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        emailField.setBackground(UIStyles.BG);
        emailField.setForeground(UIStyles.TEXT);
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));

        form.add(label("Username"));
        usernameField.setFont(UIStyles.FONT_PLAIN);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        usernameField.setBackground(UIStyles.BG);
        usernameField.setForeground(UIStyles.TEXT);
        form.add(usernameField);
        form.add(Box.createVerticalStrut(10));

        form.add(label("Password"));
        passField.setFont(UIStyles.FONT_PLAIN);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passField.setBackground(UIStyles.BG);
        passField.setForeground(UIStyles.TEXT);
        passField.setCaretColor(UIStyles.TEXT);
        form.add(passField);
        form.add(Box.createVerticalStrut(10));

        form.add(label("Confirm Password"));
        passConfirm.setFont(UIStyles.FONT_PLAIN);
        passConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passConfirm.setBackground(UIStyles.BG);
        passConfirm.setForeground(UIStyles.TEXT);
        passConfirm.setCaretColor(UIStyles.TEXT);
        form.add(passConfirm);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bottom.setOpaque(false);
        UIStyles.stylePrimaryButton(registerButton);
        UIStyles.styleSecondaryButton(backButton);
        registerButton.setFont(UIStyles.FONT_PLAIN);
        backButton.setFont(UIStyles.FONT_PLAIN);
        bottom.add(backButton);
        bottom.add(registerButton);
        add(bottom, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            if (backListener != null) backListener.onBack();
        });

        registerButton.addActionListener(e -> doRegister(listener));
        passConfirm.addActionListener(e -> doRegister(listener));
    }

    public interface BackListener { void onBack(); }

    private BackListener backListener;

    public void setBackListener(BackListener l) { this.backListener = l; }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UIStyles.FONT_PLAIN);
        l.setForeground(UIStyles.TEXT);
        l.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
        return l;
    }

    private void doRegister(RegisterListener listener) {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p1 = new String(passField.getPassword());
        String p2 = new String(passConfirm.getPassword());

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Register", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Register", JOptionPane.ERROR_MESSAGE);
            return;
        }

        registerButton.setEnabled(false);
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                // enforce username uniqueness loosely by checking existing fullName/email
                for (User u : UserService.getInstance().getAllUsers()) {
                    if (u.getEmail().equalsIgnoreCase(email) || u.getFullName().equalsIgnoreCase(username)) {
                        return null; // signal exists
                    }
                }
                return UserService.getInstance().register(name, email, p1);
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                try {
                    User u = get();
                    if (u == null) {
                        JOptionPane.showMessageDialog(GuestRegisterPanel.this, "Username or email already exists or registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // login and notify
                    SessionManager.login(u);
                    if (listener != null) listener.onRegistered(u);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GuestRegisterPanel.this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
