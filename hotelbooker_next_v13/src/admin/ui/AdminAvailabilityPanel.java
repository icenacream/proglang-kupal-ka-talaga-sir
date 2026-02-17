package admin.ui;

import common.filehandler.TransactionFileHandler;
import common.model.Booking;
import common.model.Room;
import guest.ui.UIStyles;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

/**
 * Admin calendar-style availability view.
 * Shows rooms (rows) vs days of selected month (columns). Booked days are highlighted.
 */
public class AdminAvailabilityPanel extends JPanel {
    private JComboBox<String> hotelCombo;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JTable table;
    private AvailabilityTableModel model;
    private JLabel hint;

    public AdminAvailabilityPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        initUI();
        refresh();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel title = new JLabel("Room Availability");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        hotelCombo = new JComboBox<>();
        hotelCombo.setFont(UIStyles.FONT_PLAIN);
        hotelCombo.setPreferredSize(new Dimension(240, 30));
        controls.add(new JLabel("Hotel:"));
        controls.add(hotelCombo);

        monthCombo = new JComboBox<>(new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"});
        monthCombo.setFont(UIStyles.FONT_PLAIN);
        controls.add(new JLabel("Month:"));
        controls.add(monthCombo);

        yearCombo = new JComboBox<>();
        int y = LocalDate.now().getYear();
        for (int i = y - 2; i <= y + 3; i++) yearCombo.addItem(i);
        yearCombo.setSelectedItem(y);
        yearCombo.setFont(UIStyles.FONT_PLAIN);
        controls.add(new JLabel("Year:"));
        controls.add(yearCombo);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(UIStyles.FONT_PLAIN);
        controls.add(refreshBtn);

        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new AvailabilityTableModel();
        table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);
        table.getColumnModel().getColumn(0).setPreferredWidth(260);
        table.setDefaultRenderer(Object.class, new AvailabilityCellRenderer(model));

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        hint = new JLabel("Booked days are highlighted.");
        hint.setFont(UIStyles.FONT_PLAIN);
        hint.setForeground(UIStyles.MUTED);
        footer.add(hint, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);

        Runnable onChange = this::refresh;
        hotelCombo.addActionListener(e -> onChange.run());
        monthCombo.addActionListener(e -> onChange.run());
        yearCombo.addActionListener(e -> onChange.run());
        refreshBtn.addActionListener(e -> onChange.run());

        // Default month = current
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
    }

    private void refresh() {
        // hotels
        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        Set<String> hotels = new TreeSet<>();
        for (Room r : rooms) {
            if (r != null && r.getHotelName() != null && !r.getHotelName().isBlank()) hotels.add(r.getHotelName());
        }
        String prev = hotelCombo.getItemCount() > 0 ? (String) hotelCombo.getSelectedItem() : null;
        hotelCombo.removeAllItems();
        hotelCombo.addItem("All Hotels");
        for (String h : hotels) hotelCombo.addItem(h);
        if (prev != null) hotelCombo.setSelectedItem(prev);

        int year = (Integer) yearCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        YearMonth ym = YearMonth.of(year, month);

        String hotelFilter = (String) hotelCombo.getSelectedItem();
        if (hotelFilter == null) hotelFilter = "All Hotels";

        model.setData(rooms, TransactionFileHandler.readBookingsFromFile(), ym, hotelFilter);

        // adjust column widths (Room column already set)
        for (int c = 1; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(36);
        }
        hint.setText("Showing " + ym + " - " + hotelFilter + " - Cells show remaining/units (FULL highlighted)");
        revalidate();
        repaint();
    }

    // ================== Model + Renderer ==================

    private static class AvailabilityTableModel extends AbstractTableModel {
        private List<Room> rooms = List.of();
        private YearMonth ym = YearMonth.now();
        private String hotelFilter = "All Hotels";
        private final Map<String, int[]> bookedUnitsByRoom = new HashMap<>();

        void setData(List<Room> allRooms, List<Booking> bookings, YearMonth ym, String hotelFilter) {
            this.ym = ym;
            this.hotelFilter = hotelFilter == null ? "All Hotels" : hotelFilter;

            // filter rooms
            List<Room> filtered = new ArrayList<>();
            for (Room r : allRooms) {
                if (r == null) continue;
                if (!"All Hotels".equalsIgnoreCase(this.hotelFilter)) {
                    if (r.getHotelName() == null || !r.getHotelName().equalsIgnoreCase(this.hotelFilter)) continue;
                }
                filtered.add(r);
            }
            this.rooms = filtered;

            // compute booked map
            bookedUnitsByRoom.clear();
            int days = ym.lengthOfMonth();
            for (Room r : this.rooms) {
                bookedUnitsByRoom.put(r.getId(), new int[days]);
            }

            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEndExclusive = ym.plusMonths(1).atDay(1);

            for (Booking b : bookings) {
                if (b == null) continue;
                if (b.getStatus() == null || !b.getStatus().equalsIgnoreCase("CONFIRMED")) continue;
                if (!bookedUnitsByRoom.containsKey(b.getRoomId())) continue;

                // overlap with selected month
                LocalDate start = b.getCheckInDate();
                LocalDate end = b.getCheckOutDate();
                if (start == null || end == null) continue;
                if (!start.isBefore(monthEndExclusive) || !end.isAfter(monthStart)) continue;

                LocalDate cur = start.isBefore(monthStart) ? monthStart : start;
                LocalDate last = end.isAfter(monthEndExclusive) ? monthEndExclusive : end;
                while (cur.isBefore(last)) {
                    int idx = cur.getDayOfMonth() - 1;
                    int[] arr = bookedUnitsByRoom.get(b.getRoomId());
                    if (arr != null && idx >= 0 && idx < arr.length) arr[idx]++;
                    cur = cur.plusDays(1);
                }
            }

            fireTableStructureChanged();
        }

        @Override
        public int getRowCount() {
            return rooms == null ? 0 : rooms.size();
        }

        @Override
        public int getColumnCount() {
            return 1 + ym.lengthOfMonth();
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) return "Room";
            return String.valueOf(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Room r = rooms.get(rowIndex);
            if (columnIndex == 0) {
                return r.getId() + " - " + safe(r.getHotelName());
            }
            int[] arr = bookedUnitsByRoom.get(r.getId());
            if (arr == null) return "";
            int booked = arr[columnIndex - 1];
            int units = Math.max(1, r.getUnits());
            int remaining = Math.max(0, units - booked);
            return remaining + "/" + units;
        }

        boolean isFull(int row, int dayIndex0) {
            if (rooms == null || row < 0 || row >= rooms.size()) return false;
            Room r = rooms.get(row);
            int[] arr = bookedUnitsByRoom.get(r.getId());
            if (arr == null || dayIndex0 < 0 || dayIndex0 >= arr.length) return false;
            int booked = arr[dayIndex0];
            return booked >= Math.max(1, r.getUnits());
        }

        private static String safe(String s) {
            return s == null ? "" : s;
        }
    }

    private static class AvailabilityCellRenderer extends DefaultTableCellRenderer {
        private final AvailabilityTableModel model;

        AvailabilityCellRenderer(AvailabilityTableModel model) {
            this.model = model;
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 0) {
                setHorizontalAlignment(SwingConstants.LEFT);
                c.setBackground(Color.WHITE);
                c.setForeground(UIStyles.TEXT);
                return c;
            }
            setHorizontalAlignment(SwingConstants.CENTER);

            boolean full = model.isFull(row, column - 1);
            if (isSelected) {
                c.setBackground(new Color(220, 235, 255));
            } else if (full) {
                c.setBackground(new Color(255, 230, 230));
            } else {
                c.setBackground(Color.WHITE);
            }
            c.setForeground(full ? new Color(140, 0, 0) : UIStyles.MUTED);
            return c;
        }
    }
}
