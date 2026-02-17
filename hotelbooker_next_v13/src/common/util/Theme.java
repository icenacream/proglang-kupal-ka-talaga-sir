package common.util;

public enum Theme {
    LIGHT,
    DARK;

    public static Theme fromString(String s) {
        if (s == null) return LIGHT;
        try {
            return Theme.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return LIGHT;
        }
    }
}
