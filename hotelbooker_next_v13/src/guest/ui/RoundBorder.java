package guest.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/** Rounded border for buttons */
public class RoundBorder extends AbstractBorder {
    private int radius;

    public RoundBorder(int radius) { this.radius = radius; }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (c instanceof JButton) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(0.9f));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    @Override
    public Insets getBorderInsets(Component c) { return new Insets(5, 15, 5, 15); }
}
