package common.util;

import guest.ui.UIStyles;

import javax.swing.*;
import java.awt.*;

/**
 * Central place to apply a LIGHT/DARK theme across Swing components.
 */
public final class ThemeManager {
    private ThemeManager() {}

    public static void applyTheme(Theme theme) {
        if (theme == null) theme = Theme.LIGHT;

        if (theme == Theme.DARK) {
            UIStyles.BG = new Color(20, 20, 24);
            UIStyles.BORDER = new Color(60, 60, 70);
            UIStyles.TEXT = new Color(235, 235, 240);
            UIStyles.MUTED = new Color(170, 170, 180);
        } else {
            UIStyles.BG = Color.WHITE;
            UIStyles.BORDER = new Color(220, 220, 220);
            UIStyles.TEXT = new Color(50, 50, 50);
            UIStyles.MUTED = new Color(120, 120, 120);
        }

        // Basic UIManager defaults (covers most standard Swing components)
        UIManager.put("Panel.background", UIStyles.BG);
        UIManager.put("Viewport.background", UIStyles.BG);
        UIManager.put("ScrollPane.background", UIStyles.BG);
        UIManager.put("Label.foreground", UIStyles.TEXT);

        UIManager.put("TextField.background", theme == Theme.DARK ? new Color(34, 34, 40) : Color.WHITE);
        UIManager.put("TextField.foreground", UIStyles.TEXT);
        UIManager.put("TextField.caretForeground", UIStyles.TEXT);

        UIManager.put("TextArea.background", theme == Theme.DARK ? new Color(34, 34, 40) : Color.WHITE);
        UIManager.put("TextArea.foreground", UIStyles.TEXT);
        UIManager.put("TextArea.caretForeground", UIStyles.TEXT);

        UIManager.put("ComboBox.background", theme == Theme.DARK ? new Color(34, 34, 40) : Color.WHITE);
        UIManager.put("ComboBox.foreground", UIStyles.TEXT);

        UIManager.put("Table.background", theme == Theme.DARK ? new Color(28, 28, 34) : Color.WHITE);
        UIManager.put("Table.foreground", UIStyles.TEXT);
        UIManager.put("Table.gridColor", UIStyles.BORDER);
    }

    public static void refreshAllWindows() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.invalidate();
            w.validate();
            w.repaint();
        }
    }
}
