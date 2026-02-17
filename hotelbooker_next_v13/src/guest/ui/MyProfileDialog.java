package guest.ui;

import common.model.User;
import common.service.UserService;
import common.session.SessionManager;

import javax.swing.*;
import java.awt.*;

public class MyProfileDialog extends JDialog {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField newPass;

    public MyProfileDialog(Window owner) {
        super(owner, "My Profile", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(520, 360);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        User u = SessionManager.getCurrentUser();
        if (u == null) {
            JOptionPane.showMessageDialog(getOwner(), "Please login first.");
            dispose();
            return;
        }

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        top.setBackground(Color.WHITE);
        JLabel title = new JLabel("My Profile");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        top.add(title, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        form.setBackground(Color.WHITE);

        nameField = new JTextField(u.getFullName());
        emailField = new JTextField(u.getEmail());
        newPass = new JPasswordField();
        nameField.setFont(UIStyles.FONT_PLAIN);
        emailField.setFont(UIStyles.FONT_PLAIN);
        newPass.setFont(UIStyles.FONT_PLAIN);
        emailField.setEditable(false);
        emailField.setBackground(new Color(245, 246, 250));

        form.add(label("Full name"));
        form.add(sized(nameField));
        form.add(Box.createVerticalStrut(10));
        form.add(label("Email (cannot change)"));
        form.add(sized(emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(label("New password (optional)"));
        form.add(sized(newPass));
        form.add(Box.createVerticalStrut(4));
        JLabel hint = new JLabel("Leave blank to keep your current password.");
        hint.setFont(UIStyles.FONT_PLAIN.deriveFont(12f));
        hint.setForeground(UIStyles.MUTED);
        form.add(hint);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);
        JButton close = new JButton("Close");
        JButton save = new JButton("Save");
        close.setFont(UIStyles.FONT_PLAIN);
        save.setFont(UIStyles.FONT_PLAIN);
        bottom.add(close);
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);

        close.addActionListener(e -> dispose());
        save.addActionListener(e -> doSave());
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UIStyles.FONT_PLAIN);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JComponent sized(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return c;
    }

    private void doSave() {
        User u = SessionManager.getCurrentUser();
        if (u == null) return;
        String newName = nameField.getText() == null ? "" : nameField.getText().trim();
        String newPw = new String(newPass.getPassword());
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.");
            return;
        }
        String pwOrNull = newPw.isEmpty() ? null : newPw;
        boolean ok = UserService.getInstance().updateProfile(u.getUserId(), newName, pwOrNull);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Could not save changes.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // update session copy
        u.setFullName(newName);
        if (pwOrNull != null) u.setPassword(pwOrNull);
        JOptionPane.showMessageDialog(this, "Profile updated.");
        dispose();
    }
}
