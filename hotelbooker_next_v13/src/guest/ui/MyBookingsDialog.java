package guest.ui;

import common.model.Booking;
import common.model.Room;
import guest.service.GuestBookingService;
import common.session.SessionManager;
import common.model.User;
import common.util.PdfReceiptGenerator;
import common.util.TextReceiptGenerator;
import common.util.PrintUtils;
import common.ui.DatePickerDialog;

import java.nio.file.Path;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Guest "My Bookings" dialog.
 * Lets guest search bookings by name and optionally cancel a confirmed booking.
 */
public class MyBookingsDialog extends JDialog {
    private final GuestBookingService service;
    private JTextField nameField;
    private JComboBox<String> statusFilter;
    private JTextField fromField;
    private JTextField toField;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private User sessionUser;

    public MyBookingsDialog(Window owner, GuestBookingService service) {
        super(owner, "My Bookings", ModalityType.APPLICATION_MODAL);
        this.service = service;
        initUI();
    }

    private void initUI() {
        setSize(980, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        top.setBackground(Color.WHITE);

        JLabel title = new JLabel("My Bookings");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(22f));
        top.add(title, BorderLayout.WEST);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        search.setOpaque(false);
        nameField = new JTextField(22);
        nameField.setFont(UIStyles.FONT_PLAIN);

        statusFilter = new JComboBox<>(new String[]{"All", "CONFIRMED", "CANCELLED"});
        statusFilter.setFont(UIStyles.FONT_PLAIN);
        fromField = new JTextField(10);
        fromField.setFont(UIStyles.FONT_PLAIN);
        toField = new JTextField(10);
        toField.setFont(UIStyles.FONT_PLAIN);

        JButton fromPickBtn = new JButton("\uD83D\uDCC5"); // calendar icon
        fromPickBtn.setFont(UIStyles.FONT_PLAIN);
        fromPickBtn.setToolTipText("Pick start date");
        JButton toPickBtn = new JButton("\uD83D\uDCC5");
        toPickBtn.setFont(UIStyles.FONT_PLAIN);
        toPickBtn.setToolTipText("Pick end date");

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(UIStyles.FONT_PLAIN);
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(UIStyles.FONT_PLAIN);
        search.add(new JLabel("Guest name:"));
        search.add(nameField);

        search.add(new JLabel("Status:"));
        search.add(statusFilter);
        search.add(new JLabel("From (YYYY-MM-DD):"));
        search.add(fromField);
        search.add(fromPickBtn);
        search.add(new JLabel("To (YYYY-MM-DD):"));
        search.add(toField);
        search.add(toPickBtn);

        search.add(searchBtn);
        search.add(closeBtn);
        top.add(search, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Table
        String[] cols = {"Booking ID", "Guest", "Hotel", "Location", "Check-in", "Check-out", "Guests", "Total", "Status"};
        model = new DefaultTableModel(cols, 0) {
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
        sp.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        add(sp, BorderLayout.CENTER);

        // Bottom actions
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
        bottom.setBackground(Color.WHITE);

        statusLabel = new JLabel("Enter your name then click Search.");
        statusLabel.setFont(UIStyles.FONT_PLAIN);
        statusLabel.setForeground(UIStyles.MUTED);
        bottom.add(statusLabel, BorderLayout.WEST);

        JButton cancelBtn = new JButton("Cancel Selected Booking");
        cancelBtn.setFont(UIStyles.FONT_PLAIN);

        JButton reschedBtn = new JButton("Reschedule");
        reschedBtn.setFont(UIStyles.FONT_PLAIN);

        JButton receiptPdfBtn = new JButton("Export Receipt (PDF)");
        receiptPdfBtn.setFont(UIStyles.FONT_PLAIN);
        JButton receiptTxtBtn = new JButton("Export Receipt (TXT)");
        receiptTxtBtn.setFont(UIStyles.FONT_PLAIN);
        JButton printBtn = new JButton("Print Receipt");
        printBtn.setFont(UIStyles.FONT_PLAIN);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(printBtn);
        rightBtns.add(receiptTxtBtn);
        rightBtns.add(receiptPdfBtn);
        rightBtns.add(reschedBtn);
        rightBtns.add(cancelBtn);
        bottom.add(rightBtns, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        searchBtn.addActionListener(e -> refresh());
        nameField.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> refresh());
        closeBtn.addActionListener(e -> dispose());
        cancelBtn.addActionListener(e -> cancelSelected());
        reschedBtn.addActionListener(e -> rescheduleSelected());
        receiptPdfBtn.addActionListener(e -> exportReceiptPdf());
        receiptTxtBtn.addActionListener(e -> exportReceiptTxt());
        printBtn.addActionListener(e -> printReceipt());

        fromPickBtn.addActionListener(e -> {
            java.time.LocalDate init = null;
            try {
                String v = fromField.getText() == null ? "" : fromField.getText().trim();
                if (!v.isEmpty()) init = java.time.LocalDate.parse(v);
            } catch (Exception ignore) {}
            java.time.LocalDate picked = DatePickerDialog.pick(this, init);
            if (picked != null) {
                fromField.setText(picked.toString());
                refresh();
            }
        });

        toPickBtn.addActionListener(e -> {
            java.time.LocalDate init = null;
            try {
                String v = toField.getText() == null ? "" : toField.getText().trim();
                if (!v.isEmpty()) init = java.time.LocalDate.parse(v);
            } catch (Exception ignore) {}
            java.time.LocalDate picked = DatePickerDialog.pick(this, init);
            if (picked != null) {
                toField.setText(picked.toString());
                refresh();
            }
        });

        // If logged in, lock the name and auto-load
        sessionUser = SessionManager.getCurrentUser();
        if (sessionUser != null) {
            nameField.setText(sessionUser.getFullName());
            nameField.setEditable(false);
            nameField.setBackground(new Color(245, 246, 250));
            refresh();
        }
    }

    private void refresh() {
        model.setRowCount(0);
        String guestName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (guestName.isEmpty()) {
            statusLabel.setText("Please enter a guest name.");
            return;
        }

        String statusSel = String.valueOf(statusFilter.getSelectedItem());

        java.time.LocalDate from = null;
        java.time.LocalDate to = null;
        try {
            String f = fromField.getText() == null ? "" : fromField.getText().trim();
            String t = toField.getText() == null ? "" : toField.getText().trim();
            if (!f.isEmpty()) from = java.time.LocalDate.parse(f);
            if (!t.isEmpty()) to = java.time.LocalDate.parse(t);
        } catch (Exception ex) {
            statusLabel.setText("Invalid date filter. Use YYYY-MM-DD.");
            return;
        }

        List<Booking> bookings = service.getBookingsByGuestName(guestName);
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

        int shown = 0;
        for (Booking b : bookings) {
            if (!"All".equalsIgnoreCase(statusSel)) {
                if (b.getStatus() == null || !b.getStatus().equalsIgnoreCase(statusSel)) continue;
            }
            if (from != null && b.getCheckInDate() != null && b.getCheckInDate().isBefore(from)) continue;
            if (to != null && b.getCheckInDate() != null && b.getCheckInDate().isAfter(to)) continue;

            Room r = service.getRoomById(b.getRoomId());
            String hotel = r != null ? r.getHotelName() : "(Unknown)";
            String loc = r != null ? r.getLocation() : "";
            model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getGuestName(),
                    hotel,
                    loc,
                    b.getCheckInDate().format(df),
                    b.getCheckOutDate().format(df),
                    b.getNumberOfGuests(),
                    common.util.CurrencyUtil.format(b.getTotalPrice()),
                    b.getStatus()
            });
            shown++;
        }

        statusLabel.setText(shown == 0 ? "No bookings found for '" + guestName + "' (with current filters)." : ("Showing " + shown + " booking(s)."));
    }

    private Booking getSelectedConfirmedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking.");
            return null;
        }
        String status = (String) model.getValueAt(row, 8);
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only CONFIRMED bookings have receipts.");
            return null;
        }
        String bookingId = (String) model.getValueAt(row, 0);
        Booking b = service.getBookingById(bookingId);
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return b;
    }

    private void exportReceiptPdf() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = service.getRoomById(b.getRoomId());
        try {
            Path p = PdfReceiptGenerator.generateReceipt(b, r, SessionManager.getCurrentUser());
            JOptionPane.showMessageDialog(this, "Receipt saved to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create receipt.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReceiptTxt() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = service.getRoomById(b.getRoomId());
        try {
            Path p = TextReceiptGenerator.generateEmailStyleReceipt(b, r, SessionManager.getCurrentUser());
            JOptionPane.showMessageDialog(this, "Receipt saved to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create TXT receipt.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReceipt() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = service.getRoomById(b.getRoomId());
        String txt = TextReceiptGenerator.buildEmailStyleReceiptText(b, r, SessionManager.getCurrentUser());
        PrintUtils.printText(this, "Receipt " + b.getBookingId(), txt);
    }

    private void rescheduleSelected() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;

        // Basic access control: if logged in, only allow rescheduling your own booking.
        User u = SessionManager.getCurrentUser();
        if (u != null && b.getGuestName() != null && !b.getGuestName().equalsIgnoreCase(u.getFullName())) {
            JOptionPane.showMessageDialog(this, "You can only reschedule your own bookings.", "Not allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Room r = service.getRoomById(b.getRoomId());
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "Reschedule " + b.getBookingId(), ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField inField = new JTextField(10);
        JTextField outField = new JTextField(10);
        JTextField guestsField = new JTextField(6);
        inField.setFont(UIStyles.FONT_PLAIN);
        outField.setFont(UIStyles.FONT_PLAIN);
        guestsField.setFont(UIStyles.FONT_PLAIN);
        inField.setText(b.getCheckInDate().toString());
        outField.setText(b.getCheckOutDate().toString());
        guestsField.setText(String.valueOf(b.getNumberOfGuests()));

        JButton inPick = new JButton("\uD83D\uDCC5");
        JButton outPick = new JButton("\uD83D\uDCC5");
        inPick.setFont(UIStyles.FONT_PLAIN);
        outPick.setFont(UIStyles.FONT_PLAIN);

        // Row: Hotel info
        gbc.gridx = 0; gbc.gridy++;
        JLabel info = new JLabel("Room: " + r.getHotelName() + " (" + r.getLocation() + ")  •  Capacity " + r.getCapacity());
        info.setFont(UIStyles.FONT_PLAIN);
        gbc.gridwidth = 3;
        form.add(info, gbc);
        gbc.gridwidth = 1;

        // Check-in
        gbc.gridx = 0; gbc.gridy++;
        JLabel l1 = new JLabel("Check-in (YYYY-MM-DD)");
        l1.setFont(UIStyles.FONT_PLAIN);
        form.add(l1, gbc);
        gbc.gridx = 1;
        form.add(inField, gbc);
        gbc.gridx = 2;
        form.add(inPick, gbc);

        // Check-out
        gbc.gridx = 0; gbc.gridy++;
        JLabel l2 = new JLabel("Check-out (YYYY-MM-DD)");
        l2.setFont(UIStyles.FONT_PLAIN);
        form.add(l2, gbc);
        gbc.gridx = 1;
        form.add(outField, gbc);
        gbc.gridx = 2;
        form.add(outPick, gbc);

        // Guests
        gbc.gridx = 0; gbc.gridy++;
        JLabel l3 = new JLabel("Guests");
        l3.setFont(UIStyles.FONT_PLAIN);
        form.add(l3, gbc);
        gbc.gridx = 1;
        form.add(guestsField, gbc);

        dlg.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.setFont(UIStyles.FONT_PLAIN);
        cancel.setFont(UIStyles.FONT_PLAIN);
        bottom.add(cancel);
        bottom.add(save);
        dlg.add(bottom, BorderLayout.SOUTH);

        inPick.addActionListener(e -> {
            java.time.LocalDate init = null;
            try { init = java.time.LocalDate.parse(inField.getText().trim()); } catch (Exception ignore) {}
            java.time.LocalDate picked = DatePickerDialog.pick(dlg, init);
            if (picked != null) inField.setText(picked.toString());
        });
        outPick.addActionListener(e -> {
            java.time.LocalDate init = null;
            try { init = java.time.LocalDate.parse(outField.getText().trim()); } catch (Exception ignore) {}
            java.time.LocalDate picked = DatePickerDialog.pick(dlg, init);
            if (picked != null) outField.setText(picked.toString());
        });

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                java.time.LocalDate ni = java.time.LocalDate.parse(inField.getText().trim());
                java.time.LocalDate no = java.time.LocalDate.parse(outField.getText().trim());
                int ng = Integer.parseInt(guestsField.getText().trim());
                boolean ok = service.rescheduleBooking(b.getBookingId(), ni, no, ng);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Booking updated.\n(New total will be reflected in receipts.)", "Done", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(dlg, service.getLastError(), "Cannot reschedule", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Please check your inputs.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setVisible(true);
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = (String) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 8);

        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only CONFIRMED bookings can be canceled.", "Not allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel booking " + bookingId + "?\nThis will make the room available again.",
                "Confirm cancellation",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean success = service.cancelBooking(bookingId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Booking canceled.", "Done", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Could not cancel booking (not found).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
