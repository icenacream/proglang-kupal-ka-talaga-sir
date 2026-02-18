package guest.ui;

import common.model.Booking;
import common.model.CartItem;
import common.model.Room;
import common.util.ImageUtils;
import guest.service.GuestBookingService;
import guest.service.CartService;
import common.session.SessionManager;
import common.model.User;
import common.util.PdfReceiptGenerator;
import common.ui.Toast;
import common.service.ReviewService;
import common.model.Review;
import common.ui.DatePickerDialog;

import java.nio.file.Path;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Room details + booking form.
 */
public class RoomDetailsDialog extends JDialog {
    private final Room room;
    private final GuestBookingService bookingService;

    private JTextField nameField;
    private JSpinner checkIn;
    private JSpinner checkOut;
    private JButton pickInBtn;
    private JButton pickOutBtn;
    private JSpinner guests;
    private JComboBox<String> paymentMethod;
    private JTextField promoField;
    private JLabel promoHint;
    private JLabel totalLabel;
    private JLabel availabilityLabel;
    private JPanel reviewsList;
    private JLabel ratingHeader;

    public RoomDetailsDialog(JFrame owner, Room room, GuestBookingService bookingService) {
        super(owner, "Room Details", true);
        this.room = room;
        this.bookingService = bookingService;
        buildUI();
    }

    private void buildUI() {
        setSize(900, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridLayout(1, 2));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(content, BorderLayout.CENTER);

        // Left: image + highlights
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Color.WHITE);
        content.add(left);

        JLabel image = new JLabel();
        image.setAlignmentX(Component.LEFT_ALIGNMENT);
        image.setIcon(ImageUtils.loadAndScale(room.getImagePath(), 420, 250));
        image.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        left.add(image);
        left.add(Box.createVerticalStrut(14));

        JLabel name = new JLabel(room.getHotelName());
        name.setFont(UIStyles.FONT_TITLE.deriveFont(22f));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(name);

        JLabel type = new JLabel(room.getRoomType());
        type.setForeground(new Color(60, 60, 60));
        type.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(type);

        JLabel location = new JLabel(room.getLocation());
        location.setForeground(new Color(90, 90, 90));
        location.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(location);

        left.add(Box.createVerticalStrut(10));
        JLabel rating = new JLabel("Rating: " + room.getRatingTag() + " (" + room.getReviewCount() + " reviews)");
        rating.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingHeader = rating;
        left.add(ratingHeader);

        left.add(Box.createVerticalStrut(10));
        JLabel price = new JLabel("Price: " + room.getPriceTag());
        price.setFont(UIStyles.FONT_TITLE.deriveFont(16f));
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(price);

        left.add(Box.createVerticalStrut(14));
        JLabel amTitle = new JLabel("Amenities");
        amTitle.setFont(UIStyles.FONT_TITLE.deriveFont(14f));
        amTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(amTitle);

        JPanel amenities = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        amenities.setBackground(Color.WHITE);
        amenities.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String a : room.getAmenities()) {
            JLabel pill = new JLabel(a);
            pill.setOpaque(true);
            pill.setBackground(new Color(245, 245, 245));
            pill.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            amenities.add(pill);
        }
        left.add(amenities);

        left.add(Box.createVerticalStrut(10));
        JLabel revTitle = new JLabel("Guest Reviews");
        revTitle.setFont(UIStyles.FONT_TITLE.deriveFont(14f));
        revTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(revTitle);

        reviewsList = new JPanel();
        reviewsList.setLayout(new BoxLayout(reviewsList, BoxLayout.Y_AXIS));
        reviewsList.setBackground(Color.WHITE);

        JScrollPane revScroll = new JScrollPane(reviewsList);
        revScroll.setBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)));
        revScroll.setPreferredSize(new Dimension(420, 130));
        revScroll.setMaximumSize(new Dimension(420, 130));
        revScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(revScroll);

        left.add(Box.createVerticalStrut(10));
        buildReviewComposer(left);
        refreshReviews();

        // Right: booking form
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        content.add(right);

        JLabel title = new JLabel("Book this room");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(title);
        right.add(Box.createVerticalStrut(12));

        if (!room.isAvailable()) {
            JLabel notAvail = new JLabel("This room is currently not available.");
            notAvail.setForeground(new Color(190, 40, 40));
            notAvail.setAlignmentX(Component.LEFT_ALIGNMENT);
            right.add(notAvail);
            right.add(Box.createVerticalStrut(10));
        }

        nameField = new JTextField();
        addField(right, "Guest name", nameField);

        // If logged in, pre-fill and lock name
        User sessionUser = SessionManager.getCurrentUser();
        if (sessionUser != null) {
            nameField.setText(sessionUser.getFullName());
            nameField.setEditable(false);
            nameField.setBackground(new Color(245, 246, 250));
        }

        // Check-in/out with calendar picker buttons
        checkIn = new JSpinner(new SpinnerDateModel());
        checkIn.setEditor(new JSpinner.DateEditor(checkIn, "yyyy-MM-dd"));
        pickInBtn = new JButton("\uD83D\uDCC5");
        pickInBtn.setToolTipText("Pick date");
        pickInBtn.addActionListener(e -> pickDate(true));
        addFieldWithButton(right, "Check-in", checkIn, pickInBtn);

        checkOut = new JSpinner(new SpinnerDateModel());
        checkOut.setEditor(new JSpinner.DateEditor(checkOut, "yyyy-MM-dd"));
        pickOutBtn = new JButton("\uD83D\uDCC5");
        pickOutBtn.setToolTipText("Pick date");
        pickOutBtn.addActionListener(e -> pickDate(false));
        addFieldWithButton(right, "Check-out", checkOut, pickOutBtn);

        availabilityLabel = new JLabel("Availability: —");
        availabilityLabel.setFont(UIStyles.FONT_PLAIN);
        availabilityLabel.setForeground(UIStyles.MUTED);
        availabilityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(Box.createVerticalStrut(6));
        right.add(availabilityLabel);

        // Update availability when dates change
        checkIn.addChangeListener(e -> updateAvailabilityAndTotal());
        checkOut.addChangeListener(e -> updateAvailabilityAndTotal());

        guests = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, room.getCapacity()), 1));
        addField(right, "Guests (max " + room.getCapacity() + ")", guests);

        paymentMethod = new JComboBox<>(new String[]{"GCash", "Credit Card", "Debit Card", "Cash"});
        addField(right, "Payment method", paymentMethod);

        promoField = new JTextField();
        addField(right, "Promo code (optional)", promoField);
        promoHint = new JLabel(" ");
        promoHint.setForeground(new Color(110, 110, 110));
        promoHint.setFont(UIStyles.FONT_PLAIN.deriveFont(11f));
        promoHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(promoHint);

        promoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                double pct = common.service.PromoCodeService.getDiscountPercent(promoField.getText());
                promoHint.setText(pct > 0 ? ("Discount: " + (int)pct + "%") : " ");
                updateAvailabilityAndTotal();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        totalLabel = new JLabel("Total: --");
        totalLabel.setFont(UIStyles.FONT_TITLE.deriveFont(14f));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(totalLabel);
        right.add(Box.createVerticalStrut(12));

        JButton calc = new JButton("Calculate Total");
        calc.setAlignmentX(Component.LEFT_ALIGNMENT);
        calc.addActionListener(e -> updateTotal());
        right.add(calc);

        right.add(Box.createVerticalStrut(8));

        JButton confirm = new JButton("Confirm Booking");
        confirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirm.setBackground(UIStyles.PRIMARY);
        confirm.setForeground(Color.WHITE);
        confirm.setFocusPainted(false);
        confirm.setOpaque(true);
        confirm.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        confirm.addActionListener(e -> submit());
        confirm.setEnabled(room.isAvailable());
        right.add(confirm);

        right.add(Box.createVerticalStrut(8));
        JButton addToCart = new JButton("Add to Cart");
        addToCart.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToCart.addActionListener(e -> addToCart());
        addToCart.setEnabled(room.isAvailable());
        right.add(addToCart);

        right.add(Box.createVerticalStrut(8));
        JButton close = new JButton("Close");
        close.setAlignmentX(Component.LEFT_ALIGNMENT);
        close.addActionListener(e -> dispose());
        right.add(close);
    }
    
    private void addField(JPanel parent, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        parent.add(l);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(field);
    }

    private void addFieldWithButton(JPanel parent, String label, JComponent field, JButton btn) {
        JLabel l = new JLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        parent.add(l);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.add(field, BorderLayout.CENTER);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(42, 34));
        row.add(btn, BorderLayout.EAST);

        parent.add(row);
    }

    private void pickDate(boolean isCheckIn) {
        LocalDate current = toLocalDate(isCheckIn ? checkIn.getValue() : checkOut.getValue());
        LocalDate picked = DatePickerDialog.pick(this, current);
        if (picked == null) return;
        java.util.Date d = java.sql.Date.valueOf(picked);
        if (isCheckIn) {
            checkIn.setValue(d);
            // gentle auto-adjust: if check-out is not after check-in, push it +1
            LocalDate out = toLocalDate(checkOut.getValue());
            if (!out.isAfter(picked)) {
                checkOut.setValue(java.sql.Date.valueOf(picked.plusDays(1)));
            }
        } else {
            checkOut.setValue(d);
        }
        updateAvailabilityAndTotal();
    }

    private void buildReviewComposer(JPanel left) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Leave a review (requires login)");
        hint.setFont(UIStyles.FONT_PLAIN.deriveFont(11f));
        hint.setForeground(new Color(110, 110, 110));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(hint);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        row.setBackground(Color.WHITE);
        JLabel rLbl = new JLabel("Rating:");
        JSpinner rating = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        JTextArea comment = new JTextArea(3, 24);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        JScrollPane cScroll = new JScrollPane(comment);
        cScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        JButton submit = new JButton("Submit Review");
        submit.addActionListener(e -> {
            if (!SessionManager.isLoggedIn()) {
                Toast.show(this, "Please login first to review.", Toast.Type.INFO);
                new LoginDialog(getOwner()).setVisible(true);
                return;
            }
            User u = SessionManager.getCurrentUser();
            if (u == null) return;
            int stars = (int) rating.getValue();
            String text = comment.getText().trim();
            if (text.isBlank()) {
                Toast.show(this, "Please write a short comment.", Toast.Type.WARNING);
                return;
            }
            ReviewService.upsert(room.getId(), u.getEmail(), stars, text);
            Toast.show(this, "Thanks! Your review was saved.", Toast.Type.SUCCESS, 1200);
            comment.setText("");
            refreshReviews();
        });

        row.add(rLbl);
        row.add(rating);
        row.add(submit);
        box.add(row);
        box.add(cScroll);

        left.add(box);
    }

    private void refreshReviews() {
        if (reviewsList == null) return;
        reviewsList.removeAll();

        ReviewService.Stats stats = ReviewService.getStatsForRoom(room.getId());
        if (ratingHeader != null) {
            String tag = stats.count() > 0 ? stats.tag() : room.getRatingTag();
            int cnt = stats.count() > 0 ? stats.count() : room.getReviewCount();
            ratingHeader.setText("Rating: " + tag + " (" + cnt + " reviews)");
        }

        java.util.List<Review> reviews = ReviewService.getReviewsForRoom(room.getId());
        if (reviews.isEmpty()) {
            JLabel empty = new JLabel("No reviews yet. Be the first!");
            empty.setForeground(new Color(120, 120, 120));
            empty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            reviewsList.add(empty);
        } else {
            int shown = 0;
            for (Review r : reviews) {
                if (shown++ >= 4) break;
                JPanel item = new JPanel();
                item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
                item.setBackground(Color.WHITE);
                item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));

                JLabel top = new JLabel(stars(r.getRating()) + "  " + maskEmail(r.getUserEmail()) + "  -  " + r.getDate());
                top.setFont(UIStyles.FONT_BOLD.deriveFont(11f));
                JLabel body = new JLabel("<html>" + escape(r.getComment()) + "</html>");
                body.setFont(UIStyles.FONT_PLAIN.deriveFont(11f));
                body.setForeground(new Color(70, 70, 70));

                item.add(top);
                item.add(Box.createVerticalStrut(4));
                item.add(body);
                reviewsList.add(item);
            }
        }
        reviewsList.revalidate();
        reviewsList.repaint();
    }

    private static String stars(int n) {
        n = Math.max(1, Math.min(5, n));
        return "\u2605".repeat(n) + "\u2606".repeat(5 - n);
    }

    private static String maskEmail(String email) {
        if (email == null) return "Guest";
        String e = email.trim();
        int at = e.indexOf('@');
        if (at <= 1) return "Guest";
        return e.substring(0, 1) + "***" + e.substring(at);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private LocalDate toLocalDate(Object dateObj) {
        java.util.Date d = (java.util.Date) dateObj;
        return new java.sql.Date(d.getTime()).toLocalDate();
    }

    private void updateAvailabilityAndTotal() {
        try {
            LocalDate in = toLocalDate(checkIn.getValue());
            LocalDate out = toLocalDate(checkOut.getValue());
            if (availabilityLabel != null) {
                if (in == null || out == null || !out.isAfter(in)) {
                    availabilityLabel.setText("Availability: —");
                } else {
                    int remaining = bookingService.getRemainingUnitsForDates(room.getId(), in, out);
                    availabilityLabel.setText("Availability: " + remaining + " of " + Math.max(1, room.getUnits()) + " unit(s) for selected dates");
                }
            }
        } catch (Exception ex) {
            if (availabilityLabel != null) availabilityLabel.setText("Availability: —");
        }
        updateTotal();
    }

    private void updateTotal() {
        try {
            LocalDate in = toLocalDate(checkIn.getValue());
            LocalDate out = toLocalDate(checkOut.getValue());
            if (in.isBefore(LocalDate.now())) {
                totalLabel.setText("Total: -- (check-in must be today or later)");
                return;
            }
            if (!out.isAfter(in)) {
                totalLabel.setText("Total: -- (check dates)");
                return;
            }
            long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
            double total = room.getPricePerNight() * nights;
            double pct = common.service.PromoCodeService.getDiscountPercent(promoField.getText());
            if (pct > 0) total = total * (1.0 - (pct / 100.0));
            totalLabel.setText("Total: " + common.util.CurrencyUtil.format(total) + " (" + nights + " nights)");
        } catch (Exception ex) {
            totalLabel.setText("Total: --");
        }
    }

    private void submit() {
        if (!SessionManager.isLoggedIn()) {
            Toast.show(this, "Please login first to book.", Toast.Type.INFO);
            new LoginDialog(getOwner()).setVisible(true);
            if (!SessionManager.isLoggedIn()) return;
            User u = SessionManager.getCurrentUser();
            if (u != null) {
                nameField.setText(u.getFullName());
                nameField.setEditable(false);
                nameField.setBackground(new Color(245, 246, 250));
            }
        }

        String guestName = nameField.getText().trim();
        if (guestName.isBlank()) {
            Toast.show(this, "Please enter your name.", Toast.Type.WARNING);
            return;
        }
        LocalDate in = toLocalDate(checkIn.getValue());
        LocalDate out = toLocalDate(checkOut.getValue());
        if (in.isBefore(LocalDate.now())) {
            Toast.show(this, "Check-in date cannot be in the past.", Toast.Type.WARNING);
            return;
        }
        if (!out.isAfter(in)) {
            Toast.show(this, "Check-out must be after check-in.", Toast.Type.WARNING);
            return;
        }
        int g = (int) guests.getValue();
        String method = (String) paymentMethod.getSelectedItem();

        String promo = promoField.getText();

        Booking booking = bookingService.createBooking(guestName, room.getId(), in, out, g, method, promo);
        if (booking == null) {
            Toast.show(this, bookingService.getLastError(), Toast.Type.ERROR);
            return;
        }

        Toast.show(this, "Booking confirmed! ID: " + booking.getBookingId() + " - Total: " + common.util.CurrencyUtil.format(booking.getTotalPrice()), Toast.Type.SUCCESS);

        // Auto-generate PDF receipt
        try {
            Path receipt = PdfReceiptGenerator.generateReceipt(booking, room, SessionManager.getCurrentUser());
            Toast.show(this, "Receipt saved: " + receipt.getFileName(), Toast.Type.INFO);
        } catch (Exception ex) {
            System.err.println("Receipt generation failed: " + ex.getMessage());
        }
        dispose();
    }

    private void addToCart() {
        if (!SessionManager.isLoggedIn()) {
            Toast.show(this, "Please login first to use the cart.", Toast.Type.INFO);
            new LoginDialog(getOwner()).setVisible(true);
            if (!SessionManager.isLoggedIn()) return;
        }
        LocalDate in = toLocalDate(checkIn.getValue());
        LocalDate out = toLocalDate(checkOut.getValue());
        if (in.isBefore(LocalDate.now())) {
            Toast.show(this, "Check-in date cannot be in the past.", Toast.Type.WARNING);
            return;
        }
        if (!out.isAfter(in)) {
            Toast.show(this, "Check-out must be after check-in.", Toast.Type.WARNING);
            return;
        }
        int g = (int) guests.getValue();
        if (g <= 0 || g > room.getCapacity()) {
            Toast.show(this, "Guest count must be between 1 and " + room.getCapacity() + ".", Toast.Type.WARNING);
            return;
        }
        CartService.get().add(new CartItem(room.getId(), in, out, g));
        Toast.show(this, "Added to cart (" + CartService.get().size() + " item(s)).", Toast.Type.SUCCESS);
    }
}
