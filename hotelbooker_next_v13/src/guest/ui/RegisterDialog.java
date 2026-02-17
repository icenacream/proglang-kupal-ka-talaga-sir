package guest.ui;

import common.model.User;
import common.service.UserService;
import common.session.SessionManager;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField pass1;
    private JPasswordField pass2;

    public RegisterDialog(Window owner) {
        super(owner, "Create Account", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(460, 380);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        top.setBackground(Color.WHITE);
        JLabel title = new JLabel("Create Account");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        top.add(title, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        form.setBackground(Color.WHITE);

        nameField = new JTextField();
        emailField = new JTextField();
        pass1 = new JPasswordField();
        pass2 = new JPasswordField();
        nameField.setFont(UIStyles.FONT_PLAIN);
        emailField.setFont(UIStyles.FONT_PLAIN);
        pass1.setFont(UIStyles.FONT_PLAIN);
        pass2.setFont(UIStyles.FONT_PLAIN);

        form.add(label("Full name"));
        form.add(sized(nameField));
        form.add(Box.createVerticalStrut(10));
        form.add(label("Email"));
        form.add(sized(emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(label("Password"));
        form.add(sized(pass1));
        form.add(Box.createVerticalStrut(10));
        form.add(label("Confirm password"));
        form.add(sized(pass2));

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);
        JButton cancel = new JButton("Cancel");
        JButton create = new JButton("Create");
        cancel.setFont(UIStyles.FONT_PLAIN);
        create.setFont(UIStyles.FONT_PLAIN);
        bottom.add(cancel);
        bottom.add(create);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());
        create.addActionListener(e -> doRegister());
        pass2.addActionListener(e -> doRegister());
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

    private void doRegister() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String p1 = new String(pass1.getPassword());
        String p2 = new String(pass2.getPassword());

        if (name.isEmpty() || email.isEmpty() || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        if (UserService.getInstance().emailExists(email)) {
            JOptionPane.showMessageDialog(this, "That email is already registered.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User u = UserService.getInstance().register(name, email, p1);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Could not create account.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SessionManager.login(u);
        dispose();
    }
}
