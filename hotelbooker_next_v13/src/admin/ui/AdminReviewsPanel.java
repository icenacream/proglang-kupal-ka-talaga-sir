package admin.ui;

import guest.ui.UIStyles;
import common.model.Review;
import common.model.Room;
import common.service.ReviewService;
import common.filehandler.TransactionFileHandler;
import common.ui.Toast;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin moderation for guest reviews.
 */
public class AdminReviewsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public AdminReviewsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Reviews");
        title.setFont(UIStyles.FONT_TITLE);
        title.setForeground(UIStyles.PRIMARY);

        JLabel subtitle = new JLabel("Moderate guest reviews (delete abusive/incorrect reviews)");
        subtitle.setFont(UIStyles.FONT_PLAIN);
        subtitle.setForeground(UIStyles.MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(14));

        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "Room ID", "Hotel", "User", "Rating", "Comment", "Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete Selected");

        styleButton(refreshBtn, false);
        styleButton(deleteBtn, true);

        refreshBtn.addActionListener(e -> load());
        deleteBtn.addActionListener(e -> deleteSelected());

        actions.add(refreshBtn);
        actions.add(deleteBtn);

        add(actions, BorderLayout.SOUTH);

        load();
    }

    private void styleButton(JButton b, boolean primary) {
        b.setFont(UIStyles.FONT_PLAIN);
        b.setFocusPainted(false);
        if (primary) {
            b.setBackground(UIStyles.PRIMARY);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
        }
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void load() {
        model.setRowCount(0);

        Map<String, String> roomToHotel = new HashMap<>();
        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        for (Room r : rooms) roomToHotel.put(r.getId(), r.getHotelName());

        List<Review> reviews = ReviewService.getAllReviews();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        for (Review r : reviews) {
            model.addRow(new Object[]{
                    r.getRoomId(),
                    roomToHotel.getOrDefault(r.getRoomId(), "(unknown)"),
                    r.getUserEmail(),
                    r.getRating(),
                    r.getComment(),
                    r.getDate() != null ? r.getDate().format(fmt) : ""
            });
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Toast.show(this, "Select a review first.", Toast.Type.INFO);
            return;
        }
        String roomId = String.valueOf(model.getValueAt(row, 0));
        String user = String.valueOf(model.getValueAt(row, 2));

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete this review?\nRoom: " + roomId + "\nUser: " + user,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean removed = ReviewService.delete(roomId, user);
        if (removed) {
            Toast.show(this, "Review deleted.", Toast.Type.SUCCESS);
            load();
        } else {
            Toast.show(this, "Review not found (maybe already deleted).", Toast.Type.WARNING);
        }
    }
}
