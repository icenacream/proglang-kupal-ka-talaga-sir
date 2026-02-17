package common.service;

import java.nio.file.*;
import java.util.*;

/**
 * Promo code store.
 *
 * File: data/promocodes.txt
 * Format (backward compatible):
 *   CODE|percent
 * or
 *   CODE|percent|active|description
 *
 * - percent: 0..100
 * - active: true/false (default true when omitted)
 */
public final class PromoCodeService {
    private static final String PATH = "data/promocodes.txt";

    public static class Promo {
        public String code;
        public double percent;
        public boolean active;
        public String description;

        public Promo(String code, double percent, boolean active, String description) {
            this.code = code;
            this.percent = percent;
            this.active = active;
            this.description = description == null ? "" : description;
        }
    }

    private static final Map<String, Promo> codes = new LinkedHashMap<>();
    private static boolean loaded = false;

    private PromoCodeService() {}

    public static synchronized void ensureSeed() {
        try {
            Path p = Paths.get(PATH);
            if (!Files.exists(p)) {
                Files.createDirectories(p.getParent());
                Files.writeString(p,
                        "WELCOME10|10|true|Welcome discount\n" +
                        "STUDENT5|5|true|Student discount\n" +
                        "SUMMER15|15|true|Seasonal promo\n",
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (Exception ignored) {}
    }

    public static synchronized void load() {
        if (loaded) return;
        reload();
    }

    public static synchronized void reload() {
        ensureSeed();
        codes.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get(PATH));
            for (String line : lines) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;

                String code = parts[0].trim().toUpperCase();
                if (code.isEmpty()) continue;

                double pct;
                try { pct = Double.parseDouble(parts[1].trim()); }
                catch (Exception ex) { continue; }
                if (pct <= 0 || pct > 100) continue;

                boolean active = true;
                if (parts.length >= 3) {
                    String a = parts[2].trim().toLowerCase();
                    if (!a.isEmpty()) active = a.equals("true") || a.equals("1") || a.equals("yes");
                }

                String desc = "";
                if (parts.length >= 4) desc = parts[3].trim();

                codes.put(code, new Promo(code, pct, active, desc));
            }
        } catch (Exception ignored) {
        } finally {
            loaded = true;
        }
    }

    /** Returns discount percent (0..100). Inactive codes return 0. */
    public static synchronized double getDiscountPercent(String code) {
        load();
        if (code == null) return 0;
        Promo p = codes.get(code.trim().toUpperCase());
        if (p == null || !p.active) return 0;
        return p.percent;
    }

    public static synchronized List<Promo> listAll() {
        load();
        return new ArrayList<>(codes.values());
    }

    public static synchronized void upsert(String code, double percent, boolean active, String description) {
        load();
        if (code == null) return;
        String c = code.trim().toUpperCase();
        if (c.isEmpty()) return;
        if (percent <= 0 || percent > 100) return;
        codes.put(c, new Promo(c, percent, active, description));
        save();
    }

    public static synchronized void delete(String code) {
        load();
        if (code == null) return;
        String c = code.trim().toUpperCase();
        if (c.isEmpty()) return;
        codes.remove(c);
        save();
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(Paths.get(PATH).getParent());
            StringBuilder sb = new StringBuilder();
            for (Promo p : codes.values()) {
                sb.append(p.code).append("|")
                  .append(trim2(p.percent)).append("|")
                  .append(p.active).append("|")
                  .append(p.description == null ? "" : p.description.replace("\n", " ").replace("\r", " "))
                  .append("\n");
            }
            Files.writeString(Paths.get(PATH), sb.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception ignored) {}
    }

    private static String trim2(double d) {
        // keep a clean human-readable number like "10" or "7.5"
        if (Math.abs(d - Math.rint(d)) < 1e-9) return String.valueOf((long)Math.rint(d));
        String s = String.valueOf(d);
        // avoid trailing zeros from weird formats
        if (s.contains(".")) {
            while (s.endsWith("0")) s = s.substring(0, s.length()-1);
            if (s.endsWith(".")) s = s.substring(0, s.length()-1);
        }
        return s;
    }
}
