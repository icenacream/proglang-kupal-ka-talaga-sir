package admin.ui;

import common.filehandler.TransactionFileHandler;
import common.model.Booking;
import common.model.Room;
import common.util.PdfReceiptGenerator;
import common.util.TextReceiptGenerator;
import common.util.PrintUtils;
import common.util.CsvExportUtils;
import guest.service.GuestBookingService;
import common.ui.DatePickerDialog;
import guest.ui.UIStyles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.util.List;

/**
 * Admin panel to view and manage bookings.
 */
public class AdminBookingsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JLabel info;

    public AdminBookingsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        initUI();
        refresh();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        JLabel title = new JLabel("Bookings");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton receiptPdf = new JButton("Export Receipt (PDF)");
        JButton receiptTxt = new JButton("Export Receipt (TXT)");
        JButton print = new JButton("Print Receipt");
        JButton resched = new JButton("Reschedule");
        JButton cancel = new JButton("Cancel Selected");
        JButton exportCsv = new JButton("Export CSV");
        JButton refresh = new JButton("Refresh");
        receiptPdf.setFont(UIStyles.FONT_PLAIN);
        receiptTxt.setFont(UIStyles.FONT_PLAIN);
        print.setFont(UIStyles.FONT_PLAIN);
        resched.setFont(UIStyles.FONT_PLAIN);
        cancel.setFont(UIStyles.FONT_PLAIN);
        exportCsv.setFont(UIStyles.FONT_PLAIN);
        refresh.setFont(UIStyles.FONT_PLAIN);
        actions.add(print);
        actions.add(receiptTxt);
        actions.add(receiptPdf);
        actions.add(resched);
        actions.add(cancel);
        actions.add(exportCsv);
        actions.add(refresh);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Guest", "Room", "Hotel", "Check-in", "Check-out", "Guests", "Total", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        info = new JLabel(" ");
        info.setFont(UIStyles.FONT_PLAIN);
        info.setForeground(UIStyles.MUTED);
        footer.add(info, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refresh());
        cancel.addActionListener(e -> cancelSelected());
        resched.addActionListener(e -> rescheduleSelected());
        exportCsv.addActionListener(e -> exportCsv());
        receiptPdf.addActionListener(e -> exportReceiptPdf());
        receiptTxt.addActionListener(e -> exportReceiptTxt());
        print.addActionListener(e -> printReceipt());
    }

    private Booking getSelectedConfirmedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.");
            return null;
        }
        String bookingId = String.valueOf(model.getValueAt(row, 0));
        String status = String.valueOf(model.getValueAt(row, 8));
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only CONFIRMED bookings have receipts.");
            return null;
        }

        Booking b = TransactionFileHandler.readBookingsFromFile().stream()
                .filter(x -> bookingId.equalsIgnoreCase(x.getBookingId()))
                .findFirst().orElse(null);
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return b;
    }

    private Room getRoomFor(Booking b) {
        Room r = TransactionFileHandler.readRoomsFromFile().stream()
                .filter(x -> x.getId().equalsIgnoreCase(b.getRoomId()))
                .findFirst().orElse(null);
        return r;
    }

    private void exportReceiptPdf() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = getRoomFor(b);
        try {
            Path p = PdfReceiptGenerator.generateReceipt(b, r, null);
            JOptionPane.showMessageDialog(this, "Receipt saved to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create receipt.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReceiptTxt() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = getRoomFor(b);
        try {
            Path p = TextReceiptGenerator.generateEmailStyleReceipt(b, r, null);
            JOptionPane.showMessageDialog(this, "Receipt saved to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create TXT receipt.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReceipt() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = getRoomFor(b);
        String txt = TextReceiptGenerator.buildEmailStyleReceiptText(b, r, null);
        PrintUtils.printText(this, "Receipt " + b.getBookingId(), txt);
    }

    private void rescheduleSelected() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;
        Room r = getRoomFor(b);
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GuestBookingService svc = new GuestBookingService();

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Reschedule " + b.getBookingId(), Dialog.ModalityType.APPLICATION_MODAL);
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

        gbc.gridx = 0; gbc.gridy++;
        JLabel info = new JLabel("Room: " + r.getHotelName() + " • " + r.getLocation() + "  (Units: " + r.getUnits() + ")");
        info.setFont(UIStyles.FONT_PLAIN);
        gbc.gridwidth = 3;
        form.add(info, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy++;
        JLabel l1 = new JLabel("Check-in (YYYY-MM-DD)");
        l1.setFont(UIStyles.FONT_PLAIN);
        form.add(l1, gbc);
        gbc.gridx = 1;
        form.add(inField, gbc);
        gbc.gridx = 2;
        form.add(inPick, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel l2 = new JLabel("Check-out (YYYY-MM-DD)");
        l2.setFont(UIStyles.FONT_PLAIN);
        form.add(l2, gbc);
        gbc.gridx = 1;
        form.add(outField, gbc);
        gbc.gridx = 2;
        form.add(outPick, gbc);

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
                boolean ok = svc.rescheduleBooking(b.getBookingId(), ni, no, ng);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Booking updated.", "Done", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(dlg, svc.getLastError(), "Cannot reschedule", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Please check your inputs.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setVisible(true);
    }

    private void exportCsv() {
        try {
            java.nio.file.Path p = CsvExportUtils.exportBookings(TransactionFileHandler.readBookingsFromFile(), TransactionFileHandler.readRoomsFromFile());
            JOptionPane.showMessageDialog(this, "CSV exported to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not export CSV.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        model.setRowCount(0);
        List<Booking> bookings = TransactionFileHandler.readBookingsFromFile();
        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

        for (Booking b : bookings) {
            Room r = rooms.stream().filter(x -> x.getId().equalsIgnoreCase(b.getRoomId())).findFirst().orElse(null);
            String hotel = r != null ? r.getHotelName() : "(Unknown)";
            model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getGuestName(),
                    b.getRoomId(),
                    hotel,
                    b.getCheckInDate().format(df),
                    b.getCheckOutDate().format(df),
                    b.getNumberOfGuests(),
                    common.util.CurrencyUtil.format(b.getTotalPrice()),
                    b.getStatus()
            });
        }
        info.setText("Bookings: " + bookings.size());
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = String.valueOf(model.getValueAt(row, 0));
        String status = String.valueOf(model.getValueAt(row, 8));
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only CONFIRMED bookings can be canceled.", "Not allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Cancel booking " + bookingId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // cancel booking (does not toggle room availability; conflicts are date-based)
        List<Booking> bookings = TransactionFileHandler.readBookingsFromFile();
        Booking target = bookings.stream().filter(b -> bookingId.equalsIgnoreCase(b.getBookingId())).findFirst().orElse(null);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        target.setStatus("CANCELLED");
        TransactionFileHandler.updateBooking(target);
        refresh();
    }
}
