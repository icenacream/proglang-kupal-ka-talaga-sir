package common.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Lightweight toast notifications for Swing.
 * Non-blocking (unlike JOptionPane).
 */
public final class Toast {
    private Toast() {}

    public enum Type { INFO, SUCCESS, WARNING, ERROR }

    public static void show(Component parent, String message) {
        show(parent, message, Type.INFO, 2600);
    }

    public static void show(Component parent, String message, Type type) {
        show(parent, message, type, 2600);
    }

    public static void show(Component parent, String message, Type type, int millis) {
        if (message == null || message.isBlank()) return;
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JWindow w = new JWindow(owner);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));

        Color bg;
        switch (type) {
            case SUCCESS: bg = new Color(28, 163, 74); break;
            case WARNING: bg = new Color(211, 142, 16); break;
            case ERROR: bg = new Color(196, 48, 48); break;
            default: bg = new Color(45, 52, 54);
        }
        panel.setBackground(bg);

        JLabel label = new JLabel("<html>" + escape(message) + "</html>");
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.5f));
        panel.add(label, BorderLayout.CENTER);

        w.add(panel);
        w.pack();
        w.setAlwaysOnTop(true);

        // Position bottom-right of parent window
        Rectangle bounds;
        if (owner != null) {
            bounds = owner.getBounds();
        } else {
            Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
            bounds = new Rectangle(0, 0, s.width, s.height);
        }

        int x = bounds.x + bounds.width - w.getWidth() - 18;
        int y = bounds.y + bounds.height - w.getHeight() - 46;
        w.setLocation(Math.max(10, x), Math.max(10, y));

        // Simple fade in/out using alpha composite (works on most systems)
        w.setOpacity(0f);
        w.setVisible(true);

        Timer fadeIn = new Timer(20, null);
        fadeIn.addActionListener(new AbstractAction() {
            float a = 0f;
            @Override public void actionPerformed(ActionEvent e) {
                a += 0.08f;
                if (a >= 1f) { a = 1f; fadeIn.stop(); }
                try { w.setOpacity(a); } catch (Exception ignored) {}
            }
        });
        fadeIn.start();

        Timer stay = new Timer(Math.max(800, millis), e -> {
            Timer fadeOut = new Timer(20, null);
            fadeOut.addActionListener(new AbstractAction() {
                float a = 1f;
                @Override public void actionPerformed(ActionEvent e2) {
                    a -= 0.08f;
                    if (a <= 0f) {
                        fadeOut.stop();
                        w.setVisible(false);
                        w.dispose();
                        return;
                    }
                    try { w.setOpacity(a); } catch (Exception ignored) {}
                }
            });
            fadeOut.start();
        });
        stay.setRepeats(false);
        stay.start();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
    }
}
