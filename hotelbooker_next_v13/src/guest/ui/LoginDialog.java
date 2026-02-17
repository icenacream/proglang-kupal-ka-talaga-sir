package guest.ui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JPanel cards = new JPanel(new CardLayout());

    public LoginDialog(Window owner) {
        super(owner, "Guest Authentication", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        // Size will be determined by content; use pack() after adding components
        setResizable(true);
        setLayout(new BorderLayout());

        // container card layout
        GuestLoginPanel loginPanel = new GuestLoginPanel(u -> {
            // on success close dialog
            dispose();
        });

        GuestRegisterPanel registerPanel = new GuestRegisterPanel(u -> {
            // after successful registration, show login and notify
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, "login");
            JOptionPane.showMessageDialog(this, "Account created successfully! You are now logged in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        // wire switch action
        loginPanel.getSwitchToRegisterLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                CardLayout cl = (CardLayout) cards.getLayout();
                cl.show(cards, "register");
            }
        });

        // register panel back action -> show login card
        registerPanel.setBackListener(() -> {
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, "login");
        });

        // Add cards and make scrollable to support small viewports
        cards.add(loginPanel, "login");
        cards.add(registerPanel, "register");

        JScrollPane scroll = new JScrollPane(cards, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // Allow the dialog to size to content; scrolling only appears when viewport is smaller
        add(scroll, BorderLayout.CENTER);

        // Let layout compute preferred sizes, then pack; user may resize and scroll appears as needed
        pack();
        setLocationRelativeTo(getOwner());

        // no global listeners needed; panels are wired via callbacks
    }
}
