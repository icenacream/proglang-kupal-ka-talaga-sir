package admin.ui;

import guest.ui.UIStyles;
import javax.swing.*;
import java.awt.*;

/**
 * Main admin window. Hosts sidebar, top bar and content area (CardLayout).
 */
public class AdminFrame extends JFrame {
    private JPanel cards;
    private CardLayout cardLayout;

    public AdminFrame() {
        setTitle("HotelManager - Admin Portal");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        AdminController controller = new AdminController(this);

        // Sidebar
        AdminSidebarPanel sidebar = new AdminSidebarPanel(controller);
        add(sidebar, BorderLayout.WEST);

        // Top bar
        AdminTopBarPanel topBar = new AdminTopBarPanel();
        add(topBar, BorderLayout.NORTH);

        // Cards area
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(UIStyles.BG);

        // Dashboard default
        AdminDashboardPanel dashboard = new AdminDashboardPanel();
        cards.add(dashboard, "dashboard");

        // Rooms + bookings management
        cards.add(new AdminRoomsPanel(), "hotels");
        cards.add(new AdminAvailabilityPanel(), "availability");
        cards.add(new AdminBookingsPanel(), "bookings");
        cards.add(new AdminGuestsPanel(), "guests");
        cards.add(new AdminReviewsPanel(), "reviews");
        cards.add(new AdminPromoCodesPanel(), "promocodes");
        cards.add(new AdminReportsPanel(), "reports");
        cards.add(new AdminSettingsPanel(), "settings");

        add(cards, BorderLayout.CENTER);

        // Show dashboard by default
        showCard("dashboard");
    }

    // simplePanel removed in favor of real Settings panel

    public void showCard(String name) {
        if (cardLayout != null) cardLayout.show(cards, name);
        cards.revalidate();
        cards.repaint();
    }
}
