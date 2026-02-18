import guest.ui.GuestMenu;
//import admin.ui.AdminMenu;
import javax.swing.*;
import common.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "HotelBooker");
        } catch (Exception e) {
            System.err.println("Error setting Look and Feel: " + e.getMessage());
        }

        // main ui
        SwingUtilities.invokeLater(() -> {
            // Theme on startup
            SettingsStore.load();
            ThemeManager.applyTheme(SettingsStore.getTheme());
            new GuestMenu();
        });
    }
}