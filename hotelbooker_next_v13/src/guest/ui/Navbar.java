package guest.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * A professional navbar component for the HotelBooker application.
 * Features a logo on the left and an admin mode button on the right.
 */
public class Navbar extends JPanel {
    private JLabel logoLabel;
    private JButton myBookingsButton;
    private JButton favoritesButton;
    private JButton cartButton;
    private JButton profileButton;
    private JButton logoutButton;
    private JLabel helloLabel;
    private JButton adminButton;
    private AdminButtonListener adminListener;
    private MyBookingsListener myBookingsListener;
    private FavoritesListener favoritesListener;
    private CartListener cartListener;

    public interface AuthListener {
        void onProfileClicked();

        void onLogoutClicked();
    }

    private AuthListener authListener;

    public interface AdminButtonListener {
        void onAdminButtonClicked();
    }

    public interface MyBookingsListener {
        void onMyBookingsClicked();
    }

    public interface CartListener {
        void onCartClicked();
    }

    public interface FavoritesListener {
        void onFavoritesClicked();
    }

    public Navbar() {
        initializeUI();
    }

    private void initializeUI() {
        // Main container styling
        setLayout(new BorderLayout(20, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 24, 12, 24));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 64));

        // Logo (Left side)
        logoLabel = createLogo();
        add(logoLabel, BorderLayout.WEST);

        // Center actions
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);

        myBookingsButton = createSecondaryButton("My Bookings");
        favoritesButton = createSecondaryButton("Favorites");
        cartButton = createSecondaryButton("Cart");
        profileButton = createSecondaryButton("My Profile");
        logoutButton = createSecondaryButton("Logout");
        helloLabel = new JLabel("");
        helloLabel.setFont(UIStyles.FONT_PLAIN);
        helloLabel.setForeground(UIStyles.MUTED);

        center.add(myBookingsButton);
        center.add(favoritesButton);
        center.add(cartButton);
        center.add(profileButton);
        center.add(helloLabel);
        center.add(logoutButton);

        add(center, BorderLayout.CENTER);

        // Right side buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        adminButton = createAdminButton();

        right.add(adminButton);
        add(right, BorderLayout.EAST);

        // Auth actions
        profileButton.addActionListener(e -> {
            if (authListener != null)
                authListener.onProfileClicked();
        });
        logoutButton.addActionListener(e -> {
            if (authListener != null)
                authListener.onLogoutClicked();
        });

        myBookingsButton.addActionListener(e -> {
            if (myBookingsListener != null)
                myBookingsListener.onMyBookingsClicked();
        });
        favoritesButton.addActionListener(e -> {
            if (favoritesListener != null)
                favoritesListener.onFavoritesClicked();
        });
        cartButton.addActionListener(e -> {
            if (cartListener != null)
                cartListener.onCartClicked();
        });
    }

    public void setFavoritesListener(FavoritesListener l) {
        this.favoritesListener = l;
    }

    /**
     * Create the HotelBooker logo label
     */
    private JLabel createLogo() {
        JLabel logo = new JLabel("HotelBooker");
        logo.setFont(UIStyles.FONT_TITLE);
        logo.setForeground(UIStyles.PRIMARY);
        return logo;
    }

    /**
     * Create the "Switch to Admin Mode" button with rounded styling
     */
    private JButton createAdminButton() {
        JButton button = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        button.setFont(UIStyles.FONT_PLAIN);
        // Use app primary blue color
        Color primary = UIStyles.PRIMARY;
        button.setBackground(primary);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new RoundedNavbarBorder(12));
        button.setPreferredSize(new Dimension(150, 42));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        // Hover and focus visual feedback
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(UIStyles.PRIMARY_DARK);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(UIStyles.PRIMARY);
                button.repaint();
            }
        });
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                button.setBackground(primary.darker());
                button.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                button.setBackground(primary);
                button.repaint();
            }
        });

        // Preserve existing navigation by delegating to adminListener
        button.addActionListener(e -> {
            if (adminListener != null)
                adminListener.onAdminButtonClicked();
        });

        return button;
    }

    private JButton createSecondaryButton(String label) {
        JButton button = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        button.setFont(UIStyles.FONT_PLAIN);
        button.setBackground(new Color(245, 246, 250));
        button.setForeground(new Color(40, 40, 40));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new RoundedNavbarBorder(12));
        button.setPreferredSize(new Dimension(140, 42));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(235, 236, 240));
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(245, 246, 250));
                button.repaint();
            }
        });

        return button;
    }

    /**
     * Set the admin button listener
     */
    public void setAdminButtonListener(AdminButtonListener listener) {
        this.adminListener = listener;
    }

    public void setMyBookingsListener(MyBookingsListener listener) {
        this.myBookingsListener = listener;
    }

    public void setCartListener(CartListener listener) {
        this.cartListener = listener;
    }

    public void setAuthListener(AuthListener listener) {
        this.authListener = listener;
    }

    /**
     * Update visibility/enablement based on login state.
     */
    public void setLoggedInState(boolean loggedIn, String displayName) {
        myBookingsButton.setEnabled(loggedIn);
        favoritesButton.setEnabled(loggedIn);
        cartButton.setEnabled(loggedIn);
        profileButton.setEnabled(loggedIn);

        logoutButton.setVisible(loggedIn);
        adminButton.setVisible(!loggedIn);
        helloLabel.setText(loggedIn ? ("Hello, " + (displayName == null ? "Guest" : displayName)) : "");
        revalidate();
        repaint();
    }

    /**
     * Get the admin button (for direct manipulation if needed)
     */
    public JButton getAdminButton() {
        return adminButton;
    }

    /**
     * Get the logo label (for customization if needed)
     */
    public JLabel getLogo() {
        return logoLabel;
    }
}

/**
 * Custom border class for the navbar button with rounded corners
 */
class RoundedNavbarBorder extends AbstractBorder {
    private int radius;

    public RoundedNavbarBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (c instanceof JButton) {
            JButton button = (JButton) c;

            // Draw only an outline so we don't occlude the button's text
            g2d.setColor(button.getBackground().darker());
            g2d.setStroke(new BasicStroke(0.9f));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(8, 15, 8, 15);
    }
}