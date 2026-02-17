package guest.ui;

import common.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
//import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ThemeDialog extends JDialog {

    public ThemeDialog(JFrame parent) {
        super(parent, "Theme", true);
        setSize(360, 220);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(UIStyles.BG);

        JLabel title = new JLabel("Theme & Currency");
        title.setFont(UIStyles.FONT_BOLD);
        title.setForeground(UIStyles.TEXT);
        root.add(title, BorderLayout.NORTH);  

        Theme current = SettingsStore.getTheme();

        JRadioButton light = new JRadioButton("Light");
        JRadioButton dark = new JRadioButton("Dark");
        light.setOpaque(false);
        dark.setOpaque(false);
        light.setForeground(UIStyles.TEXT);
        dark.setForeground(UIStyles.TEXT);

        ButtonGroup g = new ButtonGroup();
        g.add(light);
        g.add(dark);
        if (current == Theme.DARK) dark.setSelected(true);
        else light.setSelected(true);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(light);
        center.add(Box.createVerticalStrut(6));
        center.add(dark);

        center.add(Box.createVerticalStrut(16));
        JLabel curLabel = new JLabel("Currency");
        curLabel.setFont(UIStyles.FONT_BOLD.deriveFont(12f));
        curLabel.setForeground(UIStyles.TEXT);
        center.add(curLabel);
        center.add(Box.createVerticalStrut(6));

        JComboBox<String> currency = new JComboBox<>(new String[]{"USD", "PHP"});
        currency.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        currency.setSelectedItem(SettingsStore.getRaw("currency", "USD").toUpperCase());
        center.add(currency);

        center.add(Box.createVerticalStrut(6));
        JTextField rate = new JTextField(String.valueOf(SettingsStore.getRawDouble("php_per_usd", 56.0)));
        rate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel rateLabel = new JLabel("PHP per 1 USD (rate)");
        rateLabel.setForeground(UIStyles.MUTED);
        rateLabel.setFont(UIStyles.FONT_PLAIN.deriveFont(11f));
        center.add(rateLabel);
        center.add(rate);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        UIStyles.styleSecondaryButton(cancel);

        JButton apply = new JButton("Apply");
        apply.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        apply.setBackground(new Color(30, 144, 255));
        apply.setForeground(Color.WHITE);
        apply.setFocusPainted(false);
        apply.setBorderPainted(false);
        apply.setOpaque(true);
        apply.setFont(new Font("Segoe UI", Font.BOLD, 14));
        apply.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        apply.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                apply.setBackground(new Color(102, 175, 255));
            }

            public void mouseExited(MouseEvent e){
                apply.setBackground(new Color(30, 144, 255));
            }
        });
        
        /*UIStyles.stylePrimaryButton(apply);
        apply.setPreferredSize(new Dimension(110, 36));
        cancel.setPreferredSize(new Dimension(110, 36));
        */

        cancel.addActionListener(e -> dispose());
        apply.addActionListener(e -> {
            Theme selected = dark.isSelected() ? Theme.DARK : Theme.LIGHT;
            SettingsStore.setTheme(selected);

            // Currency settings
            SettingsStore.setRaw("currency", String.valueOf(currency.getSelectedItem()));
            try {
                double r = Double.parseDouble(rate.getText().trim());
                if (r > 0) SettingsStore.setRaw("php_per_usd", String.valueOf(r));
            } catch (Exception ignored) {}

            ThemeManager.applyTheme(selected);
            ThemeManager.refreshAllWindows();
            dispose();
        });

        actions.add(cancel);
        actions.add(apply);

        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
