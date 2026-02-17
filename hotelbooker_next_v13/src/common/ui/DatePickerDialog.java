package common.ui;

import guest.ui.UIStyles;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Lightweight calendar date picker (no external libraries).
 */
public class DatePickerDialog {

    /**
     * Opens a modal calendar picker and returns the chosen date, or null if canceled.
     */
    public static LocalDate pick(Component parent, LocalDate initial) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Select date", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(360, 360);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(10, 10));

        LocalDate init = initial != null ? initial : LocalDate.now();
        YearMonth[] currentMonth = new YearMonth[]{YearMonth.from(init)};
        LocalDate[] selected = new LocalDate[]{null};

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        header.setBackground(Color.WHITE);

        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        prev.setFont(UIStyles.FONT_PLAIN);
        next.setFont(UIStyles.FONT_PLAIN);

        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(UIStyles.FONT_BOLD);

        header.add(prev, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        gridWrap.setBackground(Color.WHITE);

        JPanel grid = new JPanel(new GridLayout(7, 7, 4, 4));
        grid.setBackground(Color.WHITE);
        gridWrap.add(grid, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        footer.setBackground(Color.WHITE);
        JButton cancel = new JButton("Cancel");
        cancel.setFont(UIStyles.FONT_PLAIN);
        footer.add(cancel);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(gridWrap, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        Runnable rebuild = () -> {
            grid.removeAll();
            monthLabel.setText(currentMonth[0].getMonth().name() + " " + currentMonth[0].getYear());

            DayOfWeek[] order = new DayOfWeek[]{
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
            };
            for (DayOfWeek d : order) {
                JLabel l = new JLabel(d.name().substring(0, 3), SwingConstants.CENTER);
                l.setFont(UIStyles.FONT_BOLD.deriveFont(12f));
                l.setForeground(UIStyles.MUTED);
                grid.add(l);
            }

            LocalDate first = currentMonth[0].atDay(1);
            int firstDow = first.getDayOfWeek().getValue(); // 1=Mon..7=Sun
            int pad = firstDow - 1;
            for (int i = 0; i < pad; i++) grid.add(new JLabel(""));

            int len = currentMonth[0].lengthOfMonth();
            for (int day = 1; day <= len; day++) {
                LocalDate date = currentMonth[0].atDay(day);
                JButton b = new JButton(String.valueOf(day));
                b.setFont(UIStyles.FONT_PLAIN);
                b.setFocusPainted(false);
                b.setBackground(Color.WHITE);
                b.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

                if (date.equals(LocalDate.now())) {
                    b.setBorder(BorderFactory.createLineBorder(new Color(170, 190, 255)));
                }
                if (initial != null && date.equals(initial)) {
                    b.setBackground(new Color(235, 240, 255));
                }

                b.addActionListener(e -> {
                    selected[0] = date;
                    dialog.dispose();
                });
                grid.add(b);
            }

            int cells = 7 * 7;
            int used = 7 + pad + len;
            for (int i = used; i < cells; i++) grid.add(new JLabel(""));

            grid.revalidate();
            grid.repaint();
        };

        prev.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            rebuild.run();
        });
        next.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            rebuild.run();
        });
        cancel.addActionListener(e -> {
            selected[0] = null;
            dialog.dispose();
        });

        rebuild.run();
        dialog.setVisible(true);
        return selected[0];
    }
}
