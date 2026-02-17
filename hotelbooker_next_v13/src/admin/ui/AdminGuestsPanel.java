package admin.ui;

import common.model.User;
import common.service.UserService;
import common.util.CsvExportUtils;
import guest.ui.UIStyles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminGuestsPanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private JLabel status;

    public AdminGuestsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        initUI();
        refresh();
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIStyles.BG);
        top.setBorder(BorderFactory.createEmptyBorder(18, 18, 10, 18));
        JLabel title = new JLabel("Guests");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton exportBtn = new JButton("Export CSV");
        JButton refreshBtn = new JButton("Refresh");
        exportBtn.setFont(UIStyles.FONT_PLAIN);
        refreshBtn.setFont(UIStyles.FONT_PLAIN);
        actions.add(exportBtn);
        actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        refreshBtn.addActionListener(e -> refresh());
        exportBtn.addActionListener(e -> exportCsv());
        add(top, BorderLayout.NORTH);

        String[] cols = {"User ID", "Name", "Email", "Created"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIStyles.BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 18, 18, 18));
        status = new JLabel(" ");
        status.setFont(UIStyles.FONT_PLAIN);
        status.setForeground(UIStyles.MUTED);
        bottom.add(status, BorderLayout.WEST);

        JButton deleteBtn = new JButton("Delete Selected Guest");
        deleteBtn.setFont(UIStyles.FONT_PLAIN);
        bottom.add(deleteBtn, BorderLayout.EAST);
        deleteBtn.addActionListener(e -> deleteSelected());
        add(bottom, BorderLayout.SOUTH);
    }

    private void refresh() {
        model.setRowCount(0);
        List<User> users = UserService.getInstance().getAllUsers();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (User u : users) {
            model.addRow(new Object[]{
                    u.getUserId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getCreatedAt() != null ? u.getCreatedAt().format(df) : ""
            });
        }
        status.setText("Total guests: " + users.size());
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a guest first.");
            return;
        }
        String userId = (String) model.getValueAt(row, 0);
        String email = (String) model.getValueAt(row, 2);
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete guest account?\n\nUser: " + email + "\n\nNote: bookings will NOT be deleted.",
                "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        boolean success = UserService.getInstance().deleteUser(userId);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Could not delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refresh();
    }

    private void exportCsv() {
        try {
            java.util.List<User> users = UserService.getInstance().getAllUsers();
            java.nio.file.Path p = CsvExportUtils.exportGuests(users);
            JOptionPane.showMessageDialog(this, "CSV exported to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not export CSV.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
