package admin.ui;

import guest.ui.UIStyles;
import javax.swing.*;
import java.awt.*;

public class AdminTopBarPanel extends JPanel {
    public AdminTopBarPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);

        JButton switchBtn = new JButton("Switch to Customer Mode") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        UIStyles.stylePrimaryButton(switchBtn);
        switchBtn.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        switchBtn.setPreferredSize(new Dimension(180, 40));
        switchBtn.addActionListener(e -> {
            // Close admin and return to guest mode (simple behavior)
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });

        right.add(switchBtn);
        add(right, BorderLayout.EAST);
    }
}
