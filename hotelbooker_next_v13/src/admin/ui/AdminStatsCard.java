package admin.ui;

import guest.ui.UIStyles;
import javax.swing.*;
import java.awt.*;

/**
 * Reusable stats card component for the admin dashboard.
 */
public class AdminStatsCard extends JPanel {
    public AdminStatsCard(Icon icon, String title, String value, String growth) {
        setLayout(new BorderLayout(8, 8));
        setBackground(UIStyles.BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyles.BORDER),
                BorderFactory.createEmptyBorder(12,12,12,12)));
        setOpaque(true);

        // Left icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setPreferredSize(new Dimension(40,40));
        add(iconLabel, BorderLayout.WEST);

        // Center content
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(UIStyles.FONT_PLAIN);
        t.setForeground(UIStyles.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UIStyles.FONT_BOLD.deriveFont(18f));
        v.setForeground(UIStyles.TEXT);
        center.add(t);
        center.add(Box.createVerticalStrut(6));
        center.add(v);
        add(center, BorderLayout.CENTER);

        // Top-right growth
        JLabel g = new JLabel(growth);
        g.setFont(UIStyles.FONT_PLAIN);
        g.setForeground(UIStyles.PRIMARY);
        JPanel topRight = new JPanel(new BorderLayout());
        topRight.setOpaque(false);
        topRight.add(g, BorderLayout.NORTH);
        add(topRight, BorderLayout.EAST);

        // Soft shadow via empty border margin (simple and safe)
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
    }
}
