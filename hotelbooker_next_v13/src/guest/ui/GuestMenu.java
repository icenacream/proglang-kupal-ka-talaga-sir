//ui/GuestMenu.java
package guest.ui;

import common.model.Room;
import guest.service.GuestBookingService;
import javax.swing.*;
//import javax.swing.border.*;
import java.awt.*;
import java.util.List;

import common.util.ImageUtils;
import common.session.SessionManager;
import common.model.User;
import common.service.FavoritesService;
import common.filehandler.TransactionFileHandler;
import common.ui.Toast;
import common.service.ReviewService;

import java.util.ArrayList;
import java.util.Comparator;

public class GuestMenu extends JFrame {
    private GuestBookingService bookingService;
    private JPanel hotelGridPanel;
    private JPanel gridSection;
    private JPanel mainPanel;
    private List<Room> currentRooms;
    private boolean showingFavorites = false;
    private boolean onlyFavoritesFilter = false;
    private boolean onlyAvailable = true;
    private String destinationQuery = "";
    private GuestSearchBar searchBar;
    private Navbar navbar;
    private JComboBox<String> sortCombo;

    // CardLayout for in-panel navigation
    private CardLayout cardLayout;
    private JPanel contentCards;
    private MyBookingsDialog bookingsPanel;

    public GuestMenu() {
        bookingService = new GuestBookingService();
        currentRooms = bookingService.getAllAvailableRooms();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("HotelBooker - Guest Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(true);

        // Set BorderLayout to accommodate navbar and content
        setLayout(new BorderLayout());

        // Create and add navbar
        navbar = new Navbar();
        navbar.setAdminButtonListener(() -> handleAdminModeSwitch());
        navbar.setMyBookingsListener(() -> openMyBookings());
        navbar.setFavoritesListener(() -> toggleFavorites());
        navbar.setCartListener(() -> openCart());
        navbar.setAuthListener(new Navbar.AuthListener() {
            @Override
            public void onProfileClicked() {
                new MyProfileDialog(GuestMenu.this).setVisible(true);
                refreshSessionUI();
            }

            @Override
            public void onLogoutClicked() {
                SessionManager.logout();
                // If bookings panel is showing, go back to home on logout
                cardLayout.show(contentCards, "home");
                refreshSessionUI();
            }
        });
        add(navbar, BorderLayout.NORTH);

        refreshSessionUI();

        // Build bookings panel (embedded, not a dialog)
        bookingsPanel = new MyBookingsDialog(bookingService,
                () -> cardLayout.show(contentCards, "home"));

        // CardLayout container: "home" = hotel grid, "bookings" = bookings panel
        cardLayout   = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.add(createScrollPane(), "home");
        contentCards.add(bookingsPanel,      "bookings");
        add(contentCards, BorderLayout.CENTER);

        setVisible(true);
    }

    private void openMyBookings() {
        if (!SessionManager.isLoggedIn()) {
            Toast.show(this, "Please login first to view your bookings.", Toast.Type.INFO);
            new LoginDialog(this).setVisible(true);
            refreshSessionUI();
            if (!SessionManager.isLoggedIn())
                return;
        }
        bookingsPanel.onShow();
        cardLayout.show(contentCards, "bookings");
    }

    private void openCart() {
        if (!SessionManager.isLoggedIn()) {
            Toast.show(this, "Please login first to use the cart.", Toast.Type.INFO);
            new LoginDialog(this).setVisible(true);
            refreshSessionUI();
            if (!SessionManager.isLoggedIn())
                return;
        }
        CartDialog dialog = new CartDialog(this, bookingService);
        dialog.setVisible(true);
    }

    private void toggleFavorites() {
        if (!SessionManager.isLoggedIn()) {
            Toast.show(this, "Please login first to use favorites.", Toast.Type.INFO);
            new LoginDialog(this).setVisible(true);
            refreshSessionUI();
            if (!SessionManager.isLoggedIn())
                return;
        }

        showingFavorites = !showingFavorites;
        Toast.show(this, showingFavorites ? "Showing favorites." : "Showing results.", Toast.Type.INFO, 1200);
        refreshHotelGrid();
    }

    private void refreshSessionUI() {
        User u = SessionManager.getCurrentUser();
        navbar.setLoggedInState(u != null, u != null ? u.getFullName() : null);
    }

    private JScrollPane createScrollPane() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeaderSection());
        mainPanel.add(createHotelGridSection());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyles.PRIMARY); // #1a73e8
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Title
        JLabel titleLabel = new JLabel("Find Your Perfect Stay");
        titleLabel.setFont(UIStyles.FONT_TITLE.deriveFont(36f));
        titleLabel.setForeground(Color.WHITE);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Search from thousands of hotels worldwide");
        subtitleLabel.setFont(UIStyles.FONT_PLAIN.deriveFont(14f));
        subtitleLabel.setForeground(Color.WHITE);

        // Add title and subtitle to a wrapper panel to ensure full width
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(26, 115, 232));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(subtitleLabel);

        headerPanel.add(textPanel, BorderLayout.NORTH);

        // Spacer between text and search bar
        JPanel spacerPanel = new JPanel();
        spacerPanel.setBackground(new Color(26, 115, 232));
        spacerPanel.setPreferredSize(new Dimension(0, 25));
        headerPanel.add(spacerPanel, BorderLayout.CENTER);

        // Add the new GuestSearchBar component
        searchBar = new GuestSearchBar();
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        searchBar.setBackground(UIStyles.BG);

        // Set search listener with null checks
        if (searchBar != null) {
            searchBar.setSearchListener((destination, checkIn, checkOut, guests) -> {
                if (destination != null && !destination.trim().isEmpty()) {
                    handleSearch(destination);
                }
            });
        }

        headerPanel.add(searchBar, BorderLayout.SOUTH);

        return headerPanel;
    }

    private void handleSearch(String destination) {
        destinationQuery = destination == null ? "" : destination.trim();
        refreshHotelGrid();
    }

    private JPanel createHotelGridSection() {
        gridSection = new JPanel();
        gridSection.setLayout(new BorderLayout());
        gridSection.setBackground(Color.WHITE);
        gridSection.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        gridSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        gridSection.setMinimumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Top bar (filters + sort + count)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel countLabel = new JLabel();
        countLabel.setForeground(new Color(90, 90, 90));
        topBar.add(countLabel, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JCheckBox availableToggle = new JCheckBox("Only available");
        availableToggle.setOpaque(false);
        availableToggle.setSelected(true);
        availableToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        availableToggle.addActionListener(e -> {
            onlyAvailable = availableToggle.isSelected();
            refreshHotelGrid();
        });
        rightControls.add(availableToggle);

        JCheckBox favoritesToggle = new JCheckBox("Only favorites");
        favoritesToggle.setOpaque(false);
        favoritesToggle.setSelected(false);
        favoritesToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        favoritesToggle.addActionListener(e -> {
            if (favoritesToggle.isSelected() && !SessionManager.isLoggedIn()) {
                Toast.show(this, "Please login first to filter favorites.", Toast.Type.INFO);
                new LoginDialog(this).setVisible(true);
                refreshSessionUI();
                if (!SessionManager.isLoggedIn()) {
                    favoritesToggle.setSelected(false);
                    return;
                }
            }
            onlyFavoritesFilter = favoritesToggle.isSelected();
            refreshHotelGrid();
        });
        rightControls.add(favoritesToggle);

        sortCombo = new JComboBox<>(new String[] {
                "Recommended",
                "Price: Low to High",
                "Price: High to Low",
                "Rating: High to Low"
        });
        sortCombo.addActionListener(e -> {
            applySort();
            refreshHotelGrid();
        });
        rightControls.add(sortCombo);

        // Refresh button to clear all filters and reload hotels
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            // Clear search filter
            destinationQuery = "";
            // Clear search bar input
            if (searchBar != null) {
                searchBar.clearFields();
            }
            // Reset sort to recommended
            if (sortCombo != null) {
                sortCombo.setSelectedIndex(0);
            }
            // Reload all hotels
            refreshHotelGrid();
            Toast.show(this, "Filters cleared. Showing all hotels.", Toast.Type.SUCCESS, 1200);
        });
        rightControls.add(refreshButton);
        topBar.add(rightControls, BorderLayout.EAST);

        gridSection.add(topBar, BorderLayout.NORTH);

        hotelGridPanel = new JPanel();
        hotelGridPanel.setLayout(new GridLayout(0, 3, 20, 20));
        hotelGridPanel.setBackground(Color.WHITE);
        // Ensure grid panel has a minimum size
        hotelGridPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 350));

        refreshHotelGrid();

        // update count label whenever grid refreshes
        gridSection.putClientProperty("countLabel", countLabel);

        gridSection.add(hotelGridPanel, BorderLayout.CENTER);
        return gridSection;
    }

    private List<Room> computeRooms() {
        // Base list: either favorites or all rooms
        List<Room> base;
        if (showingFavorites && SessionManager.isLoggedIn()) {
            var favIds = FavoritesService.getFavorites(SessionManager.getCurrentUser());
            base = TransactionFileHandler.readRoomsFromFile()
                    .stream()
                    .filter(r -> favIds.contains(r.getId()))
                    .toList();
        } else {
            base = bookingService.getAllRooms();
        }

        // Favorites filter inside search results
        if (onlyFavoritesFilter) {
            if (!SessionManager.isLoggedIn()) {
                // safety: if user logged out after toggle
                onlyFavoritesFilter = false;
            } else {
                var favIds = FavoritesService.getFavorites(SessionManager.getCurrentUser());
                base = base.stream().filter(r -> favIds.contains(r.getId())).toList();
            }
        }

        // Destination filter
        String q = destinationQuery == null ? "" : destinationQuery.trim().toLowerCase();
        if (!q.isEmpty()) {
            base = base.stream()
                    .filter(r -> (r.getHotelName() != null && r.getHotelName().toLowerCase().contains(q))
                            || (r.getLocation() != null && r.getLocation().toLowerCase().contains(q)))
                    .toList();
        }

        // Availability filter
        if (onlyAvailable) {
            base = base.stream().filter(Room::isAvailable).toList();
        }

        return base;
    }

    private void applySort() {
        if (currentRooms == null)
            return;
        String mode = sortCombo == null ? "Recommended" : (String) sortCombo.getSelectedItem();
        if (mode == null || mode.equals("Recommended"))
            return;

        // Copy so we don't mutate source list references unexpectedly
        currentRooms = new ArrayList<>(currentRooms);
        switch (mode) {
            case "Price: Low to High" -> currentRooms.sort(Comparator.comparingDouble(Room::getPricePerNight));
            case "Price: High to Low" ->
                currentRooms.sort(Comparator.comparingDouble(Room::getPricePerNight).reversed());
            case "Rating: High to Low" -> currentRooms.sort((a, b) -> {
                var sa = ReviewService.getStatsForRoom(a.getId());
                var sb = ReviewService.getStatsForRoom(b.getId());
                double ra = sa.count() > 0 ? sa.avg() : a.getRating();
                double rb = sb.count() > 0 ? sb.avg() : b.getRating();
                return Double.compare(rb, ra);
            });
            default -> {
            }
        }
    }

    private void refreshHotelGrid() {
        hotelGridPanel.removeAll();

        // recompute with filters before sorting
        currentRooms = computeRooms();

        // apply sort before painting
        applySort();

        for (Room room : currentRooms) {
            hotelGridPanel.add(createHotelCard(room));
        }

        // update count label if present
        if (gridSection != null) {
            Object o = gridSection.getClientProperty("countLabel");
            if (o instanceof JLabel lbl) {
                String prefix = showingFavorites ? "Favorites" : "Results";
                lbl.setText(prefix + ": " + (currentRooms == null ? 0 : currentRooms.size()));
            }
        }

        // Revalidate and repaint the entire component hierarchy
        hotelGridPanel.revalidate();
        hotelGridPanel.repaint();

        if (gridSection != null) {
            gridSection.revalidate();
            gridSection.repaint();
        }

        if (mainPanel != null) {
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    private JPanel createHotelCard(Room room) {
        RoundedPanel cardPanel = new RoundedPanel(12);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Image (with overlay)
        JLayeredPane imagePanel = new JLayeredPane();
        imagePanel.setPreferredSize(new Dimension(0, 180));
        imagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        imagePanel.setMinimumSize(new Dimension(0, 180));

        JLabel imageLabel = new JLabel();
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(230, 230, 230));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePanel.add(imageLabel, Integer.valueOf(0));

        // Price tag overlay
        JPanel priceTag = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g);
            }
        };
        priceTag.setLayout(new BorderLayout());
        priceTag.setBackground(Color.WHITE);
        priceTag.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        priceTag.setOpaque(false);

        JLabel priceLabel = new JLabel(room.getPriceTag());
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        priceLabel.setForeground(Color.BLACK);
        priceTag.add(priceLabel, BorderLayout.CENTER);
        priceTag.setSize(105, 35);
        // Position price tag at top-right of image panel
        imagePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                imageLabel.setBounds(0, 0, imagePanel.getWidth(), imagePanel.getHeight());
                // Fit & scale image to panel size
                imageLabel.setIcon(
                        ImageUtils.loadAndScale(room.getImagePath(), imagePanel.getWidth(), imagePanel.getHeight()));
                priceTag.setLocation(Math.max(0, imagePanel.getWidth() - 120), 10);
            }
        });
        // initial bounds
        imageLabel.setBounds(0, 0, 400, 180);
        imageLabel.setIcon(ImageUtils.loadAndScale(room.getImagePath(), 400, 180));
        priceTag.setLocation(280, 10);
        imagePanel.add(priceTag, Integer.valueOf(1));

        // Favorites heart overlay (top-left)
        JButton favBtn = new JButton();
        favBtn.setFocusPainted(false);
        favBtn.setBorderPainted(false);
        favBtn.setContentAreaFilled(false);
        favBtn.setOpaque(false);
        favBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        favBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        Runnable refreshHeart = () -> {
            boolean fav = FavoritesService.isFavorite(SessionManager.getCurrentUser(), room.getId());
            favBtn.setText(fav ? "\u2665" : "\u2661");
            favBtn.setForeground(fav ? new Color(220, 20, 60) : new Color(40, 40, 40));
        };
        refreshHeart.run();

        favBtn.addActionListener(e -> {
            if (!SessionManager.isLoggedIn()) {
                Toast.show(this, "Login to save favorites.", Toast.Type.INFO);
                new LoginDialog(this).setVisible(true);
                refreshSessionUI();
                if (!SessionManager.isLoggedIn())
                    return;
            }
            boolean nowFav = FavoritesService.toggle(SessionManager.getCurrentUser(), room.getId());
            refreshHeart.run();
            Toast.show(this, nowFav ? "Added to favorites." : "Removed from favorites.", Toast.Type.SUCCESS, 1100);
        });

        imagePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                favBtn.setLocation(10, 10);
            }
        });
        favBtn.setBounds(10, 10, 40, 40);
        imagePanel.add(favBtn, Integer.valueOf(2));

        cardPanel.add(imagePanel);

        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 15, 18));
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 4, 0);

        // Hotel name
        JLabel hotelName = new JLabel(room.getHotelName());
        hotelName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hotelName.setForeground(Color.BLACK);
        detailsPanel.add(hotelName, gbc);

        // Room type
        gbc.gridy++;
        JLabel roomType = new JLabel(room.getRoomType());
        roomType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomType.setForeground(new Color(60, 60, 60));
        detailsPanel.add(roomType, gbc);

        // Location
        JLabel location = new JLabel("   " + room.getLocation());
        location.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        location.setForeground(new Color(100, 100, 100));
        gbc.insets = new Insets(0, 0, 8, 0);
        detailsPanel.add(location, gbc);

        // Rating badge
        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ratingPanel.setOpaque(false);

        JLabel ratingBadge = new JLabel(room.getRatingTag());
        ratingBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ratingBadge.setForeground(Color.WHITE);
        ratingBadge.setBackground(new Color(26, 115, 232));
        ratingBadge.setOpaque(true);
        ratingBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Use dynamic review stats when available
        ReviewService.Stats stats = ReviewService.getStatsForRoom(room.getId());
        String ratingTag = stats.count() > 0 ? stats.tag() : room.getRatingTag();
        ratingBadge.setText(ratingTag);
        JLabel reviewCount = new JLabel(
                "(" + (stats.count() > 0 ? stats.count() : room.getReviewCount()) + " reviews)");
        reviewCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        reviewCount.setForeground(new Color(100, 100, 100));

        ratingPanel.add(ratingBadge);
        ratingPanel.add(reviewCount);

        gbc.insets = new Insets(8, 0, 8, 0);
        detailsPanel.add(ratingPanel, gbc);

        // Amenities pills
        JPanel amenitiesPanel = new JPanel();
        amenitiesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        amenitiesPanel.setOpaque(false);

        for (String amenity : room.getAmenities()) {
            JLabel amenityPill = new JLabel(amenity);
            amenityPill.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            amenityPill.setForeground(new Color(80, 80, 80));
            amenityPill.setBackground(new Color(245, 245, 245));
            amenityPill.setOpaque(true);
            amenityPill.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            amenitiesPanel.add(amenityPill);
        }

        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        detailsPanel.add(amenitiesPanel, gbc);

        cardPanel.add(detailsPanel);

        // Click-to-open details / booking dialog
        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                RoomDetailsDialog dialog = new RoomDetailsDialog(GuestMenu.this, room, bookingService);
                dialog.setVisible(true);
                // In case availability changed after booking
                currentRooms = bookingService.getAllAvailableRooms();
                refreshHotelGrid();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cardPanel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cardPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
        });

        return cardPanel;
    }

    private void handleAdminModeSwitch() {
        // Open unified Login dialog as modal
        AdminLoginDialog dlg = new AdminLoginDialog(this);
        dlg.setVisible(true);
        // Refresh session state after dialog closes (covers guest login success)
        refreshSessionUI();
    }
}

// Custom rounded JPanel class
class RoundedPanel extends JPanel {
    private int cornerRadius;

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getBorder() != null) {
            super.paintBorder(g);
        }
    }
}
