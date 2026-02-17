package admin.ui;

import common.util.DataBackupUtils;
import guest.ui.UIStyles;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * Admin Settings page: Backup/Restore data.
 */
public class AdminSettingsPanel extends JPanel {

    public AdminSettingsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyles.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Settings");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        JLabel sub = new JLabel("Backup and restore your local data files");
        sub.setFont(UIStyles.FONT_PLAIN);
        sub.setForeground(UIStyles.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 235, 235)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JTextArea info = new JTextArea(
                "- Backup creates a ZIP file containing your /data folder (rooms, bookings, payments, users).\n" +
                "- Restore replaces the current /data folder with the selected backup.\n" +
                "- For safety, the system automatically creates a backup before restoring." );
        info.setEditable(false);
        info.setOpaque(false);
        info.setFont(UIStyles.FONT_PLAIN);
        info.setForeground(UIStyles.TEXT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton backupBtn = new JButton("Backup Data");
        backupBtn.setFont(UIStyles.FONT_PLAIN);
        JButton restoreBtn = new JButton("Restore Data");
        restoreBtn.setFont(UIStyles.FONT_PLAIN);

        buttons.add(backupBtn);
        buttons.add(restoreBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        backupBtn.addActionListener(e -> doBackup());
        restoreBtn.addActionListener(e -> doRestore());
    }

    private void doBackup() {
        try {
            Path zip = DataBackupUtils.createTimestampedBackupZip();
            JOptionPane.showMessageDialog(this, "Backup created:\n" + zip.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRestore() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select backup ZIP to restore");
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        int ok = JOptionPane.showConfirmDialog(this,
                "Restore from selected backup?\nCurrent data will be overwritten.\n(A safety backup will be created automatically.)",
                "Confirm restore",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            DataBackupUtils.restoreFrom(fc.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(this, "Restore complete.\nPlease restart the app to reload data.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
