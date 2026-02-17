package admin.ui;

import guest.ui.UIStyles;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Simple bar chart drawn with Java2D (no external libraries).
 */
public class BarChartPanel extends JPanel {
    private final String title;
    private final LinkedHashMap<String, Double> data;
    private final String valueSuffix;

    public BarChartPanel(String title, Map<String, Double> data, String valueSuffix) {
        this.title = title;
        this.data = new LinkedHashMap<>();
        if (data != null) {
            for (Map.Entry<String, Double> e : data.entrySet()) {
                this.data.put(e.getKey(), e.getValue() == null ? 0.0 : e.getValue());
            }
        }
        this.valueSuffix = valueSuffix == null ? "" : valueSuffix;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyles.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setFont(UIStyles.FONT_BOLD);
        g2.setColor(new Color(20, 20, 20));
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleH = fmTitle.getHeight();
        g2.drawString(title, 0, titleH);

        if (data.isEmpty()) {
            g2.setFont(UIStyles.FONT_PLAIN);
            g2.setColor(UIStyles.MUTED);
            g2.drawString("No data", 0, titleH + 24);
            g2.dispose();
            return;
        }

        int topPad = titleH + 12;
        int leftPad = 6;
        int rightPad = 6;
        int bottomPad = 32;

        int chartX = leftPad;
        int chartY = topPad;
        int chartW = w - leftPad - rightPad;
        int chartH = h - topPad - bottomPad;
        if (chartW <= 10 || chartH <= 10) {
            g2.dispose();
            return;
        }

        double max = 0;
        for (double v : data.values()) max = Math.max(max, v);
        if (max <= 0) max = 1;

        int n = data.size();
        int gap = Math.max(6, chartW / (n * 10));
        int barW = Math.max(10, (chartW - (gap * (n - 1))) / n);

        // baseline
        g2.setColor(new Color(230, 230, 230));
        g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        int i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            double v = e.getValue();
            int barH = (int) Math.round((v / max) * (chartH - 8));
            int x = chartX + i * (barW + gap);
            int y = chartY + chartH - barH;

            // bar
            g2.setColor(new Color(90, 125, 255));
            g2.fillRoundRect(x, y, barW, barH, 10, 10);

            // value label
            g2.setColor(new Color(30, 30, 30));
            String val = format(v);
            int valW = fm.stringWidth(val);
            g2.drawString(val, x + Math.max(0, (barW - valW) / 2), y - 4);

            // x label
            String label = shorten(e.getKey(), 10);
            int labelW = fm.stringWidth(label);
            int lx = x + Math.max(0, (barW - labelW) / 2);
            int ly = chartY + chartH + fm.getAscent() + 10;
            g2.setColor(UIStyles.MUTED);
            g2.drawString(label, lx, ly);

            i++;
        }
        g2.dispose();
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() <= max) return t;
        return t.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String format(double v) {
        if (Math.abs(v) >= 1000) {
            return String.format("%.0f%s", v, valueSuffix);
        }
        if (Math.abs(v) >= 100) {
            return String.format("%.1f%s", v, valueSuffix);
        }
        return String.format("%.2f%s", v, valueSuffix);
    }
}
