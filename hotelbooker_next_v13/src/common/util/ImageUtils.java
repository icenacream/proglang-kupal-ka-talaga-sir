package common.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Small helper for loading and scaling local images for Swing.
 *
 * We keep it file-based (relative paths) so it works easily in VS Code without extra build tooling.
 */
public class ImageUtils {

    private ImageUtils() {}

    public static ImageIcon loadAndScale(String path, int width, int height) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return placeholder(width, height, "No Image");
            }
            ImageIcon raw = new ImageIcon(path);
            Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return placeholder(width, height, "Image Error");
        }
    }

    public static ImageIcon placeholder(int width, int height, String label) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(210, 210, 210));
        g2.fillRect(0, 0, width, height);
        g2.setColor(new Color(160, 160, 160));
        g2.drawRect(0, 0, width - 1, height - 1);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int x = Math.max(10, (width - fm.stringWidth(label)) / 2);
        int y = (height + fm.getAscent()) / 2;
        g2.drawString(label, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }
}
