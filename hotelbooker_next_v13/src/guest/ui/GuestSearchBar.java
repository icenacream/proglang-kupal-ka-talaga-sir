//GuestSearchBar.java - A modern, rounded search card component for hotel booking with filters and calendar input.
package guest.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.regex.*;
import java.awt.*;
import java.util.Calendar;

/**
 * A modern, rounded search card component for hotel booking.
 * Features four input fields (Destination, Check-in, Check-out, Guests),
 * a Search button, and a Filters button.
 */
public class GuestSearchBar extends JPanel {
    private JTextField destinationField;
    private CalendarField checkInField;
    private CalendarField checkOutField;
    private JTextField guestsField; // hidden field for Guests value
    private int guestCount = 1; // single source of truth for guest count
    private JButton searchButton;
    private JButton filterButton;
    // Filter selections
    private String selectedMinPrice = "";
    private String selectedMaxPrice = "";
    private String selectedMinRating = "Any rating";
    private SearchListener searchListener;

    public interface SearchListener {
        void onSearch(String destination, String checkIn, String checkOut, String guests);
    }

    public GuestSearchBar() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.weighty = 1.0;

        // Row 1: Input Fields
        gbc.gridy = 0;

        // Destination
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        add(createInputSection("Destination", "Where are you going?"), gbc);

        // Check-in
        gbc.gridx = 1;
        add(createCalendarSection("Check-in", "mm/dd/yyyy"), gbc);

        // Check-out
        gbc.gridx = 2;
        add(createCalendarSection("Check-out", "mm/dd/yyyy"), gbc);

        // Guests
        gbc.gridx = 3;
        add(createGuestsDropdownSection("Guests"), gbc);

        // Row 2: Buttons
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 0.85;
        gbc.insets = new Insets(15, 10, 0, 10);
        searchButton = createSearchButton();
        add(searchButton, gbc);

        // Filter Button
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.15;
        filterButton = createFilterButton();
        add(filterButton, gbc);

        applyRoundedBackground();
    }

    /** Standard text input section */
    private JPanel createInputSection(String label, String placeholder) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UIStyles.FONT_BOLD);
        labelComponent.setForeground(UIStyles.TEXT);
        labelComponent.setBorder(BorderFactory.createEmptyBorder(0, 5, 6, 0));
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedTextField textField = new RoundedTextField(placeholder, 10);
        textField.setFont(UIStyles.FONT_PLAIN);
        textField.setForeground(UIStyles.TEXT);
        textField.setCaretColor(UIStyles.PRIMARY);
        textField.setPreferredSize(new Dimension(120, 42));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Apply placeholder behavior
        setupPlaceholder(textField, placeholder);

        sectionPanel.add(labelComponent);
        sectionPanel.add(textField);

        if (label.equals("Destination")) destinationField = textField;

        return sectionPanel;
    }
    
    /** Setup proper placeholder behavior for text fields - Static for reuse */
    private static void setupPlaceholder(JTextField textField, String placeholder) {
        Color placeholderColor = new Color(170, 170, 170);
        
        // Set initial placeholder appearance
        textField.setText(placeholder);
        textField.setForeground(placeholderColor);
        
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(new Color(50, 50, 50));
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(placeholderColor);
                }
            }
        });
        
        // Handle color change on typing
        textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!textField.getText().equals(placeholder)) {
                    textField.setForeground(new Color(50, 50, 50));
                }
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText("");  // Keep empty, don't show placeholder while focused
                    textField.setForeground(new Color(50, 50, 50));
                }
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
    }

    /** Calendar input section */
    private JPanel createCalendarSection(String label, String placeholder) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UIStyles.FONT_BOLD);
        labelComponent.setForeground(UIStyles.TEXT);
        labelComponent.setBorder(BorderFactory.createEmptyBorder(0, 5, 6, 0));
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        CalendarField calField = new CalendarField(placeholder);
        calField.setPreferredSize(new Dimension(120, 42));
        calField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        calField.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionPanel.add(labelComponent);
        sectionPanel.add(calField);

        if (label.equals("Check-in")) checkInField = calField;
        else if (label.equals("Check-out")) checkOutField = calField;

        return sectionPanel;
    }

    /** Guests dropdown section */
    private JPanel createGuestsDropdownSection(String label) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UIStyles.FONT_BOLD);
        labelComponent.setForeground(UIStyles.TEXT);
        labelComponent.setBorder(BorderFactory.createEmptyBorder(0, 5, 6, 0));

        // Guests button (default based on guestCount)
        JButton guestsButton = new JButton(formatGuestsLabel(guestCount)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        guestsButton.setFont(UIStyles.FONT_PLAIN);
        guestsButton.setFocusPainted(false);
        guestsButton.setBackground(UIStyles.BG);
        guestsButton.setForeground(UIStyles.TEXT);
        guestsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Keep the button opaque so its text is always painted reliably
        guestsButton.setContentAreaFilled(true);
        guestsButton.setOpaque(true);
        guestsButton.setBorder(new RoundBorder(10));
        guestsButton.setPreferredSize(new Dimension(120, 42));
        guestsButton.setMinimumSize(new Dimension(120, 42));
        guestsButton.setMaximumSize(new Dimension(120, 42));
        guestsButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hidden field to store value (keeps synchronized with guestCount)
        guestsField = new RoundedTextField(formatGuestsLabel(guestCount), 10);
        guestsField.setVisible(false);

        // Popup menu for guest selection (use guestCount as single source of truth)
        JPopupMenu guestMenu = new JPopupMenu();
        JMenuItem one = new JMenuItem("1 Guest");
        one.addActionListener(e -> updateGuestCount(1, guestsButton));
        guestMenu.add(one);

        for (int i = 2; i <= 4; i++) {
            int val = i;
            JMenuItem it = new JMenuItem(formatGuestsLabel(val));
            it.addActionListener(e -> updateGuestCount(val, guestsButton));
            guestMenu.add(it);
        }

        JMenuItem fivePlus = new JMenuItem("5+ Guests");
        fivePlus.addActionListener(e -> updateGuestCount(5, guestsButton));
        guestMenu.add(fivePlus);

        guestsButton.addActionListener(e -> guestMenu.show(guestsButton, 0, guestsButton.getHeight()));

        sectionPanel.add(labelComponent);
        sectionPanel.add(guestsButton);
        sectionPanel.add(guestsField); // hidden field for getGuests()

        return sectionPanel;
    }

    /** Search button */
    private JButton createSearchButton() {
        JButton button = new JButton("Search Hotels") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        button.setFont(UIStyles.FONT_BOLD);
        button.setBackground(UIStyles.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setOpaque(false);
        button.addActionListener(e -> handleSearch());
        return button;
    }

    /** Filter button */
    private JButton createFilterButton() {
        JButton button = new JButton("Filters");
        button.setFont(UIStyles.FONT_BOLD);
        button.setBackground(UIStyles.BG);
        button.setForeground(UIStyles.TEXT);
        button.setBorder(new RoundBorder(8));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 48));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            JDialog dlg = new JDialog(parentFrame != null ? parentFrame : new Frame(), "Filters", true);
            dlg.setUndecorated(false);
            dlg.setLayout(new BorderLayout());

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
            content.setBackground(Color.WHITE);

            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font normalFont = new Font("Segoe UI", Font.PLAIN, 13);

            // Filters row: left = Price range (0 - 500) with overlapping sliders, right = Rating
            JPanel filterRow = new JPanel(new BorderLayout(12, 0));
            filterRow.setOpaque(false);

            // Left side: price label + layered sliders
            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setOpaque(false);
            JPanel priceTop = new JPanel(new BorderLayout());
            priceTop.setOpaque(false);
            JLabel priceTitle = new JLabel("Price Range:");
            priceTitle.setFont(labelFont);
            priceTitle.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
            priceTop.add(priceTitle, BorderLayout.WEST);
            JLabel priceValueLabel = new JLabel();
            priceValueLabel.setFont(normalFont);
            priceTop.add(priceValueLabel, BorderLayout.EAST);
            left.add(priceTop);

            JPanel priceWrapper = new JPanel(new BorderLayout());
            priceWrapper.setOpaque(false);
            priceWrapper.setBorder(BorderFactory.createEmptyBorder(6, 6, 10, 6));

            int defaultMin = 50;
            int defaultMax = 300;
            try {
                if (!selectedMinPrice.isEmpty()) {
                    String digits = selectedMinPrice.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) defaultMin = Integer.parseInt(digits);
                }
                if (!selectedMaxPrice.isEmpty()) {
                    String digits = selectedMaxPrice.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) defaultMax = Integer.parseInt(digits);
                }
            } catch (NumberFormatException ignored) {}

            JSlider minSlider = new JSlider(0, 500, Math.max(0, Math.min(defaultMin, 500)));
            JSlider maxSlider = new JSlider(0, 500, Math.max(0, Math.min(defaultMax, 500)));
            minSlider.setMajorTickSpacing(100);
            maxSlider.setMajorTickSpacing(100);
            minSlider.setPaintTicks(false);
            maxSlider.setPaintTicks(false);

            // Layer sliders so they share the same track visually (min on top of max)
            // Keep the layered sliders but place them inside a wrapper with consistent sizing
            JLayeredPane layered = new JLayeredPane();
            layered.setPreferredSize(new Dimension(460, 40));
            minSlider.setBounds(0, 10, 440, 20);
            maxSlider.setBounds(0, 10, 440, 20);
            layered.add(maxSlider, Integer.valueOf(1));
            layered.add(minSlider, Integer.valueOf(2));
            priceWrapper.add(layered, BorderLayout.CENTER);

            // Min/Max labels under sliders for clarity
            JPanel minMaxRow = new JPanel(new BorderLayout());
            minMaxRow.setOpaque(false);
            JLabel minLabel = new JLabel("Min: $" + minSlider.getValue());
            JLabel maxLabel = new JLabel("Max: $" + maxSlider.getValue());
            minLabel.setFont(normalFont);
            maxLabel.setFont(normalFont);
            minMaxRow.add(minLabel, BorderLayout.WEST);
            minMaxRow.add(maxLabel, BorderLayout.EAST);
            priceWrapper.add(minMaxRow, BorderLayout.SOUTH);

            left.add(priceWrapper);

            // Rating selection on the right, styled to match
            JPanel ratingPanel = new JPanel(new BorderLayout());
            ratingPanel.setOpaque(false);
            JLabel ratingTitle = new JLabel("Minimum rating:");
            ratingTitle.setFont(labelFont);
            ratingTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            ratingPanel.add(ratingTitle, BorderLayout.NORTH);
            String[] ratings = {"Any rating", "4.0+", "4.5+", "4.8+"};
            JComboBox<String> ratingCombo = new JComboBox<>(ratings);
            ratingCombo.setSelectedItem(selectedMinRating);
            ratingCombo.setPreferredSize(new Dimension(140, 32));
            ratingCombo.setFont(normalFont);
            ratingCombo.setBorder(new RoundBorder(6));
            ratingPanel.add(ratingCombo, BorderLayout.SOUTH);

            filterRow.add(left, BorderLayout.CENTER);
            filterRow.add(ratingPanel, BorderLayout.EAST);
            content.add(filterRow);

            // Separator between slider/rating and actions
            content.add(Box.createVerticalStrut(12));
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(230,230,230));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            content.add(sep);
            content.add(Box.createVerticalStrut(12));

            // initialize displayed range
            priceValueLabel.setText(" " + common.util.CurrencyUtil.formatNoCents(minSlider.getValue()) +
                    " - " + common.util.CurrencyUtil.formatNoCents(maxSlider.getValue()));

            ChangeListener sliderChange = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int min = minSlider.getValue();
                    int max = maxSlider.getValue();
                    if (min > max) {
                        if (e.getSource() == minSlider) {
                            maxSlider.setValue(min);
                            max = min;
                        } else {
                            minSlider.setValue(max);
                            min = max;
                        }
                    }
                    priceValueLabel.setText(" " + common.util.CurrencyUtil.formatNoCents(min) +
                            " - " + common.util.CurrencyUtil.formatNoCents(max));
                    minLabel.setText("Min: $" + min);
                    maxLabel.setText("Max: $" + max);
                    // reflect immediately
                    selectedMinPrice = String.valueOf(min);
                    selectedMaxPrice = String.valueOf(max);
                    if (searchListener != null) {
                        handleSearch();
                    }
                }
            };

            minSlider.addChangeListener(sliderChange);
            maxSlider.addChangeListener(sliderChange);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton apply = new JButton("Apply");
            apply.setFont(labelFont);
            apply.setBackground(new Color(26,115,232));
            apply.setForeground(Color.BLUE);
            apply.setFocusPainted(false);
            apply.setCursor(new Cursor(Cursor.HAND_CURSOR));
            apply.setBorder(BorderFactory.createEmptyBorder());
            apply.setContentAreaFilled(false);
            actions.setOpaque(false);

            
            apply.setBorder(new RoundBorder(8));
            apply.setPreferredSize(new Dimension(100, 36));

            JButton cancel = new JButton("Cancel");
            cancel.setFont(normalFont);
            cancel.setBorder(new RoundBorder(8));
            cancel.setPreferredSize(new Dimension(100, 36));
            actions.add(cancel);
            actions.add(apply);

            apply.addActionListener(ae -> {
                // capture slider values (they already update live, but ensure stored)
                selectedMinPrice = String.valueOf(minSlider.getValue());
                selectedMaxPrice = String.valueOf(maxSlider.getValue());
                selectedMinRating = (String) ratingCombo.getSelectedItem();
                // update button label to indicate active filter
                if (selectedMinRating != null && !selectedMinRating.equals("Any rating")) {
                    button.setText(selectedMinRating);
                } else {
                    button.setText("Filters");
                }
                dlg.setVisible(false);
                dlg.dispose();
            });

            cancel.addActionListener(ae -> {
                dlg.setVisible(false);
                dlg.dispose();
            });

            dlg.add(content, BorderLayout.CENTER);
            dlg.add(actions, BorderLayout.SOUTH);
            dlg.pack();
            try {
                if (button.isShowing()) {
                    Point loc = button.getLocationOnScreen();
                    dlg.setLocation(loc.x - dlg.getWidth() + button.getWidth(), loc.y + button.getHeight());
                } else {
                    dlg.setLocationRelativeTo(button);
                }
            } catch (Exception ex) {
                dlg.setLocationRelativeTo(button);
            }
            dlg.setVisible(true);
        });

        return button;
    }

    /** Handle search click */
    private void handleSearch() {
        if (searchListener != null && destinationField != null && checkInField != null 
                && checkOutField != null && guestsField != null) {
            searchListener.onSearch(
                    destinationField.getText() != null ? destinationField.getText() : "",
                    checkInField.getText() != null ? checkInField.getText() : "",
                    checkOutField.getText() != null ? checkOutField.getText() : "",
                    guestsField.getText() != null ? guestsField.getText() : ""
            );
        }
    }

    /** Rounded panel background */
    private void applyRoundedBackground() {
        setOpaque(false);
        setUI(new javax.swing.plaf.basic.BasicPanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 25, 25);
                super.paint(g, c);
            }
        });
    }

    /** Set search listener */
    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }

    public String getDestination() { return destinationField != null ? destinationField.getText() : ""; }
    public String getCheckIn() { return checkInField != null ? checkInField.getText() : ""; }
    public String getCheckOut() { return checkOutField != null ? checkOutField.getText() : ""; }
    public String getGuests() { return guestsField != null ? guestsField.getText() : ""; }
    public String getMinPrice() { return selectedMinPrice; }
    public String getMaxPrice() { return selectedMaxPrice; }
    public String getMinRating() { return selectedMinRating; }

    public void clearFields() {
        if (destinationField != null) destinationField.setText("");
        if (checkInField != null) checkInField.setText("mm/dd/yyyy");
        if (checkOutField != null) checkOutField.setText("mm/dd/yyyy");
        // reset guest count to default (1 Guest)
        guestCount = 1;
        if (guestsField != null) guestsField.setText(formatGuestsLabel(guestCount));
    }

    /**
     * Format the guests label based on count (singular/plural and 5+ semantics).
     */
    private String formatGuestsLabel(int count) {
        if (count <= 1) return "1 Guest";
        if (count >= 5) return "5+ Guests";
        return count + " Guests";
    }

    /**
     * Update single source-of-truth guest count and synchronize visible and hidden fields.
     */
    private void updateGuestCount(int count, JButton guestsButton) {
        guestCount = count;
        String label = formatGuestsLabel(count);
        if (SwingUtilities.isEventDispatchThread()) {
            guestsButton.setText(label);
            if (guestsField != null) guestsField.setText(label);
        } else {
            SwingUtilities.invokeLater(() -> {
                guestsButton.setText(label);
                if (guestsField != null) guestsField.setText(label);
            });
        }
    }
}

