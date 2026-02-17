package admin.ui;

import admin.service.AdminAnalyticsService;
import guest.ui.UIStyles;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import common.util.DataBackupUtils;
import java.nio.file.Path;

/**
 * Dashboard content panel with stat cards and chart placeholders.
 */
public class AdminDashboardPanel extends JPanel {
    public AdminDashboardPanel() {
        setLayout(new BorderLayout(16,16));
        setBackground(new Color(245,245,245));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        AdminAnalyticsService analytics = new AdminAnalyticsService();

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        JLabel subtitle = new JLabel("Monitor your hotel business performance");
        subtitle.setFont(UIStyles.FONT_PLAIN);
        subtitle.setForeground(UIStyles.MUTED);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // Stats cards
        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new GridLayout(1,4,12,12));

        double revenue = analytics.getTotalRevenue();
        int bookings = analytics.getTotalBookings();
        int guests = analytics.getActiveGuests();
        int hotels = analytics.getHotelsCount();

        cards.add(new AdminStatsCard(UIManager.getIcon("OptionPane.informationIcon"), "Total Revenue", common.util.CurrencyUtil.format(revenue), "Live"));
        cards.add(new AdminStatsCard(UIManager.getIcon("FileView.directoryIcon"), "Total Bookings", String.valueOf(bookings), "Live"));
        cards.add(new AdminStatsCard(UIManager.getIcon("OptionPane.questionIcon"), "Active Guests", String.valueOf(guests), "Live"));
        cards.add(new AdminStatsCard(UIManager.getIcon("FileView.fileIcon"), "Hotels", String.valueOf(hotels), "Live"));

        add(cards, BorderLayout.CENTER);

        // Charts + quick tools area
        JPanel charts = new JPanel(new GridLayout(2,2,12,12));
        charts.setOpaque(false);

        Map<String, Double> revByMonth = analytics.getRevenueByMonth(6);
        JPanel trend = new BarChartPanel("Revenue (Last 6 Months)", revByMonth, "");

        Map<String, Integer> statusCounts = analytics.getBookingsByStatus();
        Map<String, Double> statusD = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : statusCounts.entrySet()) statusD.put(e.getKey(), e.getValue().doubleValue());
        JPanel occupancy = new BarChartPanel("Bookings by Status", statusD, "");

        Map<String, Integer> occByHotel = analytics.getOccupancyByHotel();
        Map<String, Double> occHotelD = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : occByHotel.entrySet()) occHotelD.put(e.getKey(), e.getValue().doubleValue());
        JPanel hotelOcc = new BarChartPanel("Occupied Rooms by Hotel", occHotelD, "(count)");

        JPanel tools = buildQuickToolsPanel();

        charts.add(trend);
        charts.add(occupancy);
        charts.add(hotelOcc);
        charts.add(tools);

        add(charts, BorderLayout.SOUTH);
    }

    private JPanel buildQuickToolsPanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235,235,235)),
                BorderFactory.createEmptyBorder(12,12,12,12)
        ));

        JLabel title = new JLabel("Quick Tools");
        title.setFont(UIStyles.FONT_BOLD);
        p.add(title, BorderLayout.NORTH);

        JTextArea desc = new JTextArea(
                "Backup and restore your system data files (rooms, bookings, payments, users).\n" +
                "Backups are stored as ZIP files in the /backups folder.");
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFont(UIStyles.FONT_PLAIN);
        desc.setForeground(UIStyles.TEXT);
        p.add(desc, BorderLayout.CENTER);

        JButton backupBtn = new JButton("Backup Now");
        backupBtn.setFont(UIStyles.FONT_PLAIN);
        JButton openSettingsBtn = new JButton("Open Settings");
        openSettingsBtn.setFont(UIStyles.FONT_PLAIN);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(openSettingsBtn);
        btns.add(backupBtn);
        p.add(btns, BorderLayout.SOUTH);

        backupBtn.addActionListener(e -> {
            try {
                Path zip = DataBackupUtils.createTimestampedBackupZip();
                JOptionPane.showMessageDialog(this, "Backup created:\n" + zip.toAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Find the AdminFrame and switch card
        openSettingsBtn.addActionListener(e -> {
            Container c = SwingUtilities.getAncestorOfClass(AdminFrame.class, this);
            if (c instanceof AdminFrame) {
                ((AdminFrame) c).showCard("settings");
            }
        });

        return p;
    }

    // placeholderPanel removed in favor of live charts
}
