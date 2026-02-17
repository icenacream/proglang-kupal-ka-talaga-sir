package guest.ui;

import common.model.Booking;
import common.model.CartItem;
import common.model.Room;
import common.model.User;
import common.session.SessionManager;
import common.ui.Toast;
import common.util.PdfReceiptGenerator;
import common.util.CurrencyUtil;
import common.service.PromoCodeService;
import guest.service.CartService;
import guest.service.GuestBookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Multi-room booking cart. */
public class CartDialog extends JDialog {
    private final GuestBookingService bookingService;
    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> paymentMethod;
    private final JTextField promoField;
    private final JLabel promoHint;

    public CartDialog(Window owner, GuestBookingService bookingService) {
        super(owner, "Cart", ModalityType.APPLICATION_MODAL);
        this.bookingService = bookingService;

        setSize(950, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Cart (Multi-room Booking)");
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Room ID", "Hotel", "Location", "Check-in", "Check-out", "Nights", "Guests", "Est. Total"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton remove = new JButton("Remove Selected");
        remove.addActionListener(e -> removeSelected());
        JButton clear = new JButton("Clear Cart");
        clear.addActionListener(e -> { CartService.get().clear(); refresh(); Toast.show(this, "Cart cleared.", Toast.Type.INFO); });
        left.add(remove);
        left.add(clear);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        paymentMethod = new JComboBox<>(new String[]{"GCash", "Credit Card", "Debit Card", "Cash"});

        promoField = new JTextField(10);
        promoField.setPreferredSize(new Dimension(140, 30));
        promoHint = new JLabel(" ");
        promoHint.setFont(promoHint.getFont().deriveFont(11f));
        promoHint.setForeground(new Color(110, 110, 110));

        promoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                double pct = PromoCodeService.getDiscountPercent(promoField.getText());
                promoHint.setText(pct > 0 ? ("Discount: " + (int)pct + "%") : " ");
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        JButton checkout = new JButton("Checkout All");
        checkout.addActionListener(e -> checkoutAll());
        right.add(new JLabel("Promo:"));
        right.add(promoField);
        right.add(promoHint);
        right.add(new JLabel("Payment:"));
        right.add(paymentMethod);
        right.add(checkout);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        List<CartItem> items = CartService.get().list();
        for (CartItem ci : items) {
            Room r = bookingService.getRoomById(ci.getRoomId());
            if (r == null) continue;
            long nights = ChronoUnit.DAYS.between(ci.getCheckIn(), ci.getCheckOut());
            double total = r.getPricePerNight() * Math.max(0, nights);
            model.addRow(new Object[]{
                    r.getId(), r.getHotelName(), r.getLocation(),
                    ci.getCheckIn(), ci.getCheckOut(), nights, ci.getGuests(),
                    CurrencyUtil.format(total)
            });
        }
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Toast.show(this, "Select an item to remove.", Toast.Type.INFO);
            return;
        }
        CartService.get().remove(row);
        refresh();
        Toast.show(this, "Removed from cart.", Toast.Type.SUCCESS);
    }

    private void checkoutAll() {
        if (!SessionManager.isLoggedIn()) {
            Toast.show(this, "Please login first to checkout.", Toast.Type.INFO);
            new LoginDialog(getOwner()).setVisible(true);
            if (!SessionManager.isLoggedIn()) return;
        }

        User u = SessionManager.getCurrentUser();
        String guestName = u != null ? u.getFullName() : "Guest";

        List<CartItem> items = new ArrayList<>(CartService.get().list());
        if (items.isEmpty()) {
            Toast.show(this, "Your cart is empty.", Toast.Type.INFO);
            return;
        }

        String method = (String) paymentMethod.getSelectedItem();
        String promo = promoField.getText();
        int ok = JOptionPane.showConfirmDialog(this,
                "Checkout " + items.size() + " room(s) as \"" + guestName + "\"?",
                "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        int success = 0;
        int failed = 0;
        for (CartItem ci : items) {
            // extra validation (safe)
            if (ci.getCheckIn().isBefore(LocalDate.now()) || !ci.getCheckOut().isAfter(ci.getCheckIn())) {
                failed++;
                continue;
            }
            Booking b = bookingService.createBooking(guestName, ci.getRoomId(), ci.getCheckIn(), ci.getCheckOut(), ci.getGuests(), method, promo);
            if (b == null) {
                failed++;
                continue;
            }
            success++;

            // receipt
            try {
                Room r = bookingService.getRoomById(ci.getRoomId());
                if (r != null) {
                    Path receipt = PdfReceiptGenerator.generateReceipt(b, r, u);
                    // non-blocking note
                    Toast.show(this, "Receipt: " + receipt.getFileName(), Toast.Type.INFO, 1400);
                }
            } catch (Exception ignored) {}
        }

        CartService.get().clear();
        refresh();

        if (success > 0 && failed == 0) {
            Toast.show(this, "Checkout complete: " + success + " booking(s).", Toast.Type.SUCCESS);
        } else if (success > 0) {
            Toast.show(this, "Checkout complete: " + success + " succeeded, " + failed + " failed.", Toast.Type.WARNING);
        } else {
            Toast.show(this, bookingService.getLastError(), Toast.Type.ERROR);
        }
    }
}
