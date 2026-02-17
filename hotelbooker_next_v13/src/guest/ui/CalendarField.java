package guest.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.Calendar;
import java.util.regex.Pattern;

/** Calendar field component for date selection */
public class CalendarField extends JPanel {
    private RoundedTextField textField;
    private JButton calendarButton;
    private JDialog calendarDialog;
    private JPanel daysPanel;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private int selectedDay = -1;

    public CalendarField(String placeholder) {
        setLayout(new BorderLayout());
        setOpaque(false);

        textField = new RoundedTextField(placeholder, 10);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setForeground(Color.GRAY);
        textField.setPreferredSize(new Dimension(120, 40));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        Color placeholderColor = new Color(170, 170, 170);
        textField.setText(placeholder);
        textField.setForeground(placeholderColor);

        try {
            AbstractDocument doc = (AbstractDocument) textField.getDocument();
            doc.setDocumentFilter(new DateDocumentFilter());
        } catch (Exception ex) {
            // fallback silently
        }

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
                    textField.setText("");
                    textField.setForeground(new Color(50, 50, 50));
                }
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        
        add(textField, BorderLayout.CENTER);

        calendarButton = new JButton("Cal");
        calendarButton.setBorder(BorderFactory.createEmptyBorder());
        calendarButton.setContentAreaFilled(false);
        calendarButton.setFocusPainted(false);
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarButton.setPreferredSize(new Dimension(40, 40));
        add(calendarButton, BorderLayout.EAST);

        calendarButton.addActionListener(e -> showCalendar());
    }

    private void showCalendar() {
        if (calendarDialog == null) {
            calendarDialog = new JDialog((Frame) null);
            calendarDialog.setUndecorated(true);
            calendarDialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

            JPanel container = new JPanel(new BorderLayout(10, 10));
            container.setBackground(Color.WHITE);
            container.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            topPanel.setOpaque(false);

            monthCombo = new JComboBox<>(new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"});
            yearCombo = new JComboBox<>();
            int yearNow = Calendar.getInstance().get(Calendar.YEAR);
            for (int y = yearNow - 10; y <= yearNow + 10; y++) yearCombo.addItem(y);

            Font comboFont = new Font("Segoe UI", Font.PLAIN, 12);
            monthCombo.setFont(comboFont);
            yearCombo.setFont(comboFont);
            monthCombo.setPreferredSize(new Dimension(110, 28));
            yearCombo.setPreferredSize(new Dimension(90, 28));
            monthCombo.setBorder(new RoundBorder(6));
            yearCombo.setBorder(new RoundBorder(6));
            monthCombo.setBackground(Color.WHITE);
            yearCombo.setBackground(Color.WHITE);

            monthCombo.addActionListener(e -> refreshDays());
            yearCombo.addActionListener(e -> refreshDays());

            topPanel.add(monthCombo);
            topPanel.add(yearCombo);

            daysPanel = new JPanel(new GridLayout(0, 7, 6, 6));
            daysPanel.setOpaque(false);
            refreshDays();

            container.add(topPanel, BorderLayout.NORTH);
            container.add(daysPanel, BorderLayout.CENTER);

            calendarDialog.getContentPane().setLayout(new BorderLayout());
            calendarDialog.getContentPane().add(container, BorderLayout.CENTER);
            calendarDialog.pack();
        }

        Point location = textField.getLocationOnScreen();
        calendarDialog.setLocation(location.x, location.y + textField.getHeight());
        calendarDialog.setVisible(true);
    }

    private void refreshDays() {
        daysPanel.removeAll();
        int month = monthCombo.getSelectedIndex();
        int year = (int) yearCombo.getSelectedItem();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDay; i++) {
            int day = i;
            JButton dayBtn = new JButton(String.format("%02d", day));
            dayBtn.setPreferredSize(new Dimension(36, 30));
            dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayBtn.setFocusPainted(false);

            dayBtn.addActionListener(e -> {
                selectedDay = day;
                textField.setText(String.format("%02d/%02d/%d", selectedDay, month+1, year));
                calendarDialog.setVisible(false);
                refreshDays();
            });

            daysPanel.add(dayBtn);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    public String getText() { return textField.getText(); }
    public void setText(String text) { textField.setText(text); }

    private static class DateDocumentFilter extends DocumentFilter {
        private static final Pattern PARTIAL = Pattern.compile("^$|^\\d{0,2}(/\\d{0,2}(/\\d{0,4})?)?$");
        private static final int MAX_LEN = 10;

        private boolean isAcceptable(String text) {
            if (text.length() > MAX_LEN) return false;
            return PARTIAL.matcher(text).matches();
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, string);
            String newText = sb.toString();
            if (isAcceptable(newText)) {
                super.insertString(fb, offset, string, attr);
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text == null ? "" : text);
            String newText = sb.toString();
            if (isAcceptable(newText)) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.delete(offset, offset + length);
            String newText = sb.toString();
            if (isAcceptable(newText)) {
                super.remove(fb, offset, length);
            } else {
                super.remove(fb, offset, length);
            }
        }
    }
}
