package common.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Simple currency formatter/converter.
 *
 * Base prices in the data files remain in USD to keep older data compatible.
 * If the user selects PHP, we convert using a configurable rate.
 */
public final class CurrencyUtil {

    public enum Currency {
        USD("$"),
        PHP("\u20B1");

        private final String symbol;
        Currency(String symbol) { this.symbol = symbol; }
        public String symbol() { return symbol; }
        public static Currency from(String s) {
            if (s == null) return USD;
            try { return Currency.valueOf(s.trim().toUpperCase()); }
            catch (Exception e) { return USD; }
        }
    }

    private CurrencyUtil() {}

    public static Currency getCurrency() {
        SettingsStore.load();
        return Currency.from(SettingsStore.getRaw("currency", "USD"));
    }

    /** USD -> current currency amount */
    public static double convertFromUsd(double amountUsd) {
        Currency c = getCurrency();
        if (c == Currency.PHP) {
            double rate = SettingsStore.getRawDouble("php_per_usd", 56.0);
            return amountUsd * rate;
        }
        return amountUsd;
    }

    public static String format(double amountUsd) {
        Currency c = getCurrency();
        double amt = convertFromUsd(amountUsd);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return c.symbol() + nf.format(amt);
    }

    public static String formatNoCents(double amountUsd) {
        Currency c = getCurrency();
        double amt = convertFromUsd(amountUsd);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        return c.symbol() + nf.format(amt);
    }

    public static String formatNightly(double amountUsdPerNight) {
        return formatNoCents(amountUsdPerNight) + "/night";
    }
}
