package guest.ui;

import java.awt.*;
import javax.swing.*;

/**
 * Shared UI style constants for lightweight, Swing-safe theming.
 */
public final class UIStyles {
    // Brand color stays consistent across themes.
    public static final Color PRIMARY = new Color(26, 115, 232);
    public static final Color PRIMARY_DARK = PRIMARY.darker();

    // Theme colors (mutable). Updated via ThemeManager.
    public static Color BG = Color.WHITE;
    public static Color BORDER = new Color(220, 220, 220);
    public static Color TEXT = new Color(50, 50, 50);
    public static Color MUTED = new Color(120, 120, 120);

    public static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);

    private UIStyles() {}

    public static void stylePrimaryButton(JButton b) {
        b.setFont(FONT_BOLD);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
    }

    public static void styleSecondaryButton(JButton b) {
        b.setFont(FONT_BOLD);
        b.setBackground(BG);
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
    }
}
