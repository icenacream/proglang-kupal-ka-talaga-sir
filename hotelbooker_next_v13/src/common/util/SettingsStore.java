package common.util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Very small settings store backed by a local properties file.
 * Keeps the project DB-less and offline-friendly.
 */
public final class SettingsStore {
    private static final String SETTINGS_PATH = "data/settings.properties";
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private SettingsStore() {}

    public static synchronized void load() {
        if (loaded) return;
        try {
            Path p = Paths.get(SETTINGS_PATH);
            if (Files.exists(p)) {
                try (InputStream in = Files.newInputStream(p)) {
                    props.load(in);
                }
            }
        } catch (Exception ignored) {
        } finally {
            loaded = true;
        }
    }

    /** Raw getter for other settings (currency, conversion rates, etc.). */
    public static synchronized String getRaw(String key, String fallback) {
        load();
        return props.getProperty(key, fallback);
    }

    public static synchronized double getRawDouble(String key, double fallback) {
        load();
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(fallback)));
        } catch (Exception e) {
            return fallback;
        }
    }

    public static synchronized void setRaw(String key, String value) {
        load();
        if (key == null || key.isBlank()) return;
        props.setProperty(key.trim(), value == null ? "" : value);
        save();
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(Paths.get("data"));
            try (OutputStream out = Files.newOutputStream(Paths.get(SETTINGS_PATH))) {
                props.store(out, "HotelBooker Settings");
            }
        } catch (Exception ignored) {
        }
    }
}
