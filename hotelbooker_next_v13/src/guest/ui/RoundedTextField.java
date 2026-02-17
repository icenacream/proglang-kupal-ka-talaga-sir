package guest.ui;

import javax.swing.*;
import java.awt.*;

/** Rounded text field with custom border and styling */
public class RoundedTextField extends JTextField {
    private int cornerRadius;

    public RoundedTextField(String placeholder, int radius) {
        super(placeholder);
        this.cornerRadius = radius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        super.paintComponent(g);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
    }
}
