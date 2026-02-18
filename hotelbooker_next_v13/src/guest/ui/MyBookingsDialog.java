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
 * Guest "My Bookings" panel — embedded inside GuestMenu via CardLayout.
 * No JDialog / JFrame; the panel is swapped in-place inside the main window.
 */
public class MyBookingsDialog extends JPanel {

    private final GuestBookingService service;
    private final Runnable onBack;          // called when user clicks ← Back

    private JTextField nameField;
    private JComboBox<String> statusFilter;
    private JTextField fromField;
    private JTextField toField;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private User sessionUser;

    public MyBookingsDialog(GuestBookingService service, Runnable onBack) {
        this.service = service;
        this.onBack  = onBack;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
    }

    // ── UI construction ────────────────────────────────────────────────────────

    private void initUI() {

        // ── TOP PANEL ─────────────────────────────────────────────────────────
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        // Row 1 – back button + title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JButton backBtn = makeSecondaryButton("\u2190 Back");
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        JLabel title = new JLabel("My Bookings");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(22f));
        title.setForeground(UIStyles.PRIMARY);

        titleRow.add(backBtn);
        titleRow.add(Box.createHorizontalStrut(8));
        titleRow.add(title);
        top.add(titleRow);
        top.add(Box.createVerticalStrut(12));

        // Row 2 – search / filter controls
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);

        nameField = new JTextField(18);
        nameField.setFont(UIStyles.FONT_PLAIN);

        statusFilter = new JComboBox<>(new String[]{"All", "CONFIRMED", "CANCELLED"});
        statusFilter.setFont(UIStyles.FONT_PLAIN);

        fromField = new JTextField(10);
        fromField.setFont(UIStyles.FONT_PLAIN);
        toField = new JTextField(10);
        toField.setFont(UIStyles.FONT_PLAIN);

        JButton fromPickBtn = makeIconButton("\uD83D\uDCC5", "Pick start date");
        JButton toPickBtn   = makeIconButton("\uD83D\uDCC5", "Pick end date");
        JButton searchBtn   = makeSecondaryButton("Search");

        searchRow.add(makeLabel("Guest:"));
        searchRow.add(nameField);
        searchRow.add(Box.createHorizontalStrut(4));
        searchRow.add(makeLabel("Status:"));
        searchRow.add(statusFilter);
        searchRow.add(Box.createHorizontalStrut(4));
        searchRow.add(makeLabel("From:"));
        searchRow.add(fromField);
        searchRow.add(fromPickBtn);
        searchRow.add(makeLabel("To:"));
        searchRow.add(toField);
        searchRow.add(toPickBtn);
        searchRow.add(Box.createHorizontalStrut(8));
        searchRow.add(searchBtn);
        top.add(searchRow);

        // Separator below top panel
        JSeparator sep = new JSeparator();
        sep.setForeground(UIStyles.BORDER);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(Color.WHITE);
        topWrapper.add(top, BorderLayout.CENTER);
        topWrapper.add(sep, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        // ── TABLE ─────────────────────────────────────────────────────────────
        String[] cols = {"Booking ID", "Guest", "Hotel", "Location",
                         "Check-in", "Check-out", "Guests", "Total", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);
        table.setGridColor(UIStyles.BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        add(sp, BorderLayout.CENTER);

        // ── BOTTOM ACTION BAR ─────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyles.BORDER),
                BorderFactory.createEmptyBorder(10, 28, 14, 28)));
        bottom.setBackground(Color.WHITE);

        statusLabel = new JLabel("Enter your name then click Search.");
        statusLabel.setFont(UIStyles.FONT_PLAIN);
        statusLabel.setForeground(UIStyles.MUTED);
        bottom.add(statusLabel, BorderLayout.WEST);

        JButton printBtn      = makeSecondaryButton("Print Receipt");
        JButton receiptTxtBtn = makeSecondaryButton("Export Receipt (TXT)");
        JButton receiptPdfBtn = makeSecondaryButton("Export Receipt (PDF)");
        JButton reschedBtn    = makeSecondaryButton("Reschedule");
        JButton cancelBtn     = makePrimaryButton("Cancel Selected Booking");

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(printBtn);
        rightBtns.add(receiptTxtBtn);
        rightBtns.add(receiptPdfBtn);
        rightBtns.add(reschedBtn);
        rightBtns.add(cancelBtn);
        bottom.add(rightBtns, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // ── ACTIONS ───────────────────────────────────────────────────────────
        searchBtn.addActionListener(e -> refresh());
        nameField.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> refresh());
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
            java.time.LocalDate picked = DatePickerDialog.pick(
                    SwingUtilities.getWindowAncestor(this), init);
            if (picked != null) { fromField.setText(picked.toString()); refresh(); }
        });

        toPickBtn.addActionListener(e -> {
            java.time.LocalDate init = null;
            try {
                String v = toField.getText() == null ? "" : toField.getText().trim();
                if (!v.isEmpty()) init = java.time.LocalDate.parse(v);
            } catch (Exception ignore) {}
            java.time.LocalDate picked = DatePickerDialog.pick(
                    SwingUtilities.getWindowAncestor(this), init);
            if (picked != null) { toField.setText(picked.toString()); refresh(); }
        });
    }

    /**
     * Called by GuestMenu each time the bookings card is shown,
     * so the panel re-reads the current session user and refreshes data.
     */
    public void onShow() {
        sessionUser = SessionManager.getCurrentUser();
        if (sessionUser != null) {
            nameField.setText(sessionUser.getFullName());
            nameField.setEditable(false);
            nameField.setBackground(new Color(245, 246, 250));
            refresh();
        } else {
            nameField.setText("");
            nameField.setEditable(true);
            nameField.setBackground(Color.WHITE);
            model.setRowCount(0);
            statusLabel.setText("Enter your name then click Search.");
        }
    }

    // ── Helper factories ───────────────────────────────────────────────────────

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIStyles.FONT_PLAIN);
        l.setForeground(UIStyles.TEXT);
        return l;
    }

    private JButton makeSecondaryButton(String label) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(UIStyles.FONT_PLAIN);
        btn.setBackground(new Color(245, 246, 250));
        btn.setForeground(new Color(40, 40, 40));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 212, 220), 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(235, 236, 240)); btn.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(245, 246, 250)); btn.repaint();
            }
        });
        return btn;
    }

    private JButton makePrimaryButton(String label) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(UIStyles.FONT_BOLD);
        btn.setBackground(UIStyles.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyles.PRIMARY_DARK, 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(UIStyles.PRIMARY_DARK); btn.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(UIStyles.PRIMARY); btn.repaint();
            }
        });
        return btn;
    }

    private JButton makeIconButton(String icon, String tooltip) {
        JButton btn = makeSecondaryButton(icon);
        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 212, 220), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return btn;
    }

    // ── Data / business logic (unchanged) ─────────────────────────────────────

    private void refresh() {
        model.setRowCount(0);
        String guestName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (guestName.isEmpty()) {
            statusLabel.setText("Please enter a guest name.");
            return;
        }

        String statusSel = String.valueOf(statusFilter.getSelectedItem());

        java.time.LocalDate from = null;
        java.time.LocalDate to   = null;
        try {
            String f = fromField.getText() == null ? "" : fromField.getText().trim();
            String t = toField.getText()   == null ? "" : toField.getText().trim();
            if (!f.isEmpty()) from = java.time.LocalDate.parse(f);
            if (!t.isEmpty()) to   = java.time.LocalDate.parse(t);
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
            if (to   != null && b.getCheckInDate() != null && b.getCheckInDate().isAfter(to))   continue;

            Room r    = service.getRoomById(b.getRoomId());
            String hotel = r != null ? r.getHotelName() : "(Unknown)";
            String loc   = r != null ? r.getLocation()  : "";
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

        statusLabel.setText(shown == 0
                ? "No bookings found for '" + guestName + "' (with current filters)."
                : "Showing " + shown + " booking(s).");
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
        Room r   = service.getRoomById(b.getRoomId());
        String txt = TextReceiptGenerator.buildEmailStyleReceiptText(b, r, SessionManager.getCurrentUser());
        PrintUtils.printText(SwingUtilities.getWindowAncestor(this), "Receipt " + b.getBookingId(), txt);
    }

    private void rescheduleSelected() {
        Booking b = getSelectedConfirmedBooking();
        if (b == null) return;

        User u = SessionManager.getCurrentUser();
        if (u != null && b.getGuestName() != null
                && !b.getGuestName().equalsIgnoreCase(u.getFullName())) {
            JOptionPane.showMessageDialog(this,
                    "You can only reschedule your own bookings.", "Not allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Room r = service.getRoomById(b.getRoomId());
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Reschedule " + b.getBookingId(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 260);
        dlg.setLocationRelativeTo(owner);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField inField     = new JTextField(10);
        JTextField outField    = new JTextField(10);
        JTextField guestsField = new JTextField(6);
        inField.setFont(UIStyles.FONT_PLAIN);
        outField.setFont(UIStyles.FONT_PLAIN);
        guestsField.setFont(UIStyles.FONT_PLAIN);
        inField.setText(b.getCheckInDate().toString());
        outField.setText(b.getCheckOutDate().toString());
        guestsField.setText(String.valueOf(b.getNumberOfGuests()));

        JButton inPick  = new JButton("\uD83D\uDCC5");
        JButton outPick = new JButton("\uD83D\uDCC5");
        inPick.setFont(UIStyles.FONT_PLAIN);
        outPick.setFont(UIStyles.FONT_PLAIN);

        gbc.gridx = 0; gbc.gridy++;
        JLabel info = new JLabel("Room: " + r.getHotelName() + " (" + r.getLocation()
                + ")  \u2022  Capacity " + r.getCapacity());
        info.setFont(UIStyles.FONT_PLAIN);
        gbc.gridwidth = 3;
        form.add(info, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy++;
        JLabel l1 = new JLabel("Check-in (YYYY-MM-DD)");
        l1.setFont(UIStyles.FONT_PLAIN);
        form.add(l1, gbc); gbc.gridx = 1; form.add(inField, gbc);
        gbc.gridx = 2; form.add(inPick, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel l2 = new JLabel("Check-out (YYYY-MM-DD)");
        l2.setFont(UIStyles.FONT_PLAIN);
        form.add(l2, gbc); gbc.gridx = 1; form.add(outField, gbc);
        gbc.gridx = 2; form.add(outPick, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel l3 = new JLabel("Guests");
        l3.setFont(UIStyles.FONT_PLAIN);
        form.add(l3, gbc); gbc.gridx = 1; form.add(guestsField, gbc);

        dlg.add(form, BorderLayout.CENTER);

        JPanel dlgBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        dlgBottom.setBackground(Color.WHITE);
        JButton save   = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.setFont(UIStyles.FONT_PLAIN);
        cancel.setFont(UIStyles.FONT_PLAIN);
        dlgBottom.add(cancel);
        dlgBottom.add(save);
        dlg.add(dlgBottom, BorderLayout.SOUTH);

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
                    JOptionPane.showMessageDialog(dlg,
                            "Booking updated.\n(New total will be reflected in receipts.)",
                            "Done", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(dlg, service.getLastError(),
                            "Cannot reschedule", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Please check your inputs.",
                        "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setVisible(true);
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.",
                    "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = (String) model.getValueAt(row, 0);
        String status    = (String) model.getValueAt(row, 8);

        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                    "Only CONFIRMED bookings can be canceled.", "Not allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel booking " + bookingId + "?\nThis will make the room available again.",
                "Confirm cancellation", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean success = service.cancelBooking(bookingId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Booking canceled.", "Done",
                    JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Could not cancel booking (not found).",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
