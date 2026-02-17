package admin.ui;

import common.service.PromoCodeService;
import guest.ui.UIStyles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin panel for managing promo codes (CRUD).
 */
public class AdminPromoCodesPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public AdminPromoCodesPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JLabel title = new JLabel("Promo Codes");
        title.setFont(UIStyles.FONT_TITLE);
        title.setForeground(UIStyles.TEXT);

        JLabel subtitle = new JLabel("Create, edit, disable, or delete discount codes used in checkout.");
        subtitle.setFont(UIStyles.FONT_PLAIN);
        subtitle.setForeground(UIStyles.MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Code","Percent","Active","Description"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Double.class;
                if (columnIndex == 2) return Boolean.class;
                return String.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);
        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyles.BORDER));
        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton toggleBtn = new JButton("Enable/Disable");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        UIStyles.stylePrimaryButton(addBtn);
        UIStyles.styleSecondaryButton(editBtn);
        UIStyles.styleSecondaryButton(toggleBtn);
        UIStyles.styleSecondaryButton(deleteBtn);
        deleteBtn.setForeground(new Color(180, 30, 30));
        UIStyles.styleSecondaryButton(refreshBtn);

        actions.add(refreshBtn);
        actions.add(deleteBtn);
        actions.add(toggleBtn);
        actions.add(editBtn);
        actions.add(addBtn);

        add(actions, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadTable());
        addBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a promo code first.", "Promo Codes", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            PromoCodeService.Promo p = rowToPromo(r);
            openEditor(p);
        });
        toggleBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a promo code first.", "Promo Codes", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            PromoCodeService.Promo p = rowToPromo(r);
            PromoCodeService.upsert(p.code, p.percent, !p.active, p.description);
            loadTable();
        });
        deleteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a promo code first.", "Promo Codes", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String code = String.valueOf(model.getValueAt(r, 0));
            int ok = JOptionPane.showConfirmDialog(this, "Delete promo code '" + code + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                PromoCodeService.delete(code);
                loadTable();
            }
        });

        loadTable();
    }

    private PromoCodeService.Promo rowToPromo(int r) {
        String code = String.valueOf(model.getValueAt(r, 0));
        double pct = 0;
        try { pct = Double.parseDouble(String.valueOf(model.getValueAt(r, 1))); } catch (Exception ignored) {}
        boolean active = Boolean.TRUE.equals(model.getValueAt(r, 2));
        String desc = String.valueOf(model.getValueAt(r, 3));
        return new PromoCodeService.Promo(code, pct, active, desc);
    }

    private void loadTable() {
        PromoCodeService.reload();
        model.setRowCount(0);
        List<PromoCodeService.Promo> list = PromoCodeService.listAll();
        for (PromoCodeService.Promo p : list) {
            model.addRow(new Object[]{p.code, p.percent, p.active, p.description});
        }
    }

    private void openEditor(PromoCodeService.Promo existing) {
        JTextField codeField = new JTextField(existing == null ? "" : existing.code);
        JTextField pctField = new JTextField(existing == null ? "" : String.valueOf(existing.percent));
        JCheckBox activeBox = new JCheckBox("Active", existing == null || existing.active);
        JTextField descField = new JTextField(existing == null ? "" : existing.description);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(new JLabel("Code (e.g., WELCOME10)"));
        form.add(codeField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Percent (1..100)"));
        form.add(pctField);
        form.add(Box.createVerticalStrut(8));
        form.add(activeBox);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Description (optional)"));
        form.add(descField);

        if (existing != null) {
            codeField.setEditable(false);
        }

        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Add Promo Code" : "Edit Promo Code",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        String pctStr = pctField.getText() == null ? "" : pctField.getText().trim();
        String desc = descField.getText() == null ? "" : descField.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Code is required.", "Promo Codes", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double pct;
        try { pct = Double.parseDouble(pctStr); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Percent must be a number.", "Promo Codes", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (pct <= 0 || pct > 100) {
            JOptionPane.showMessageDialog(this, "Percent must be between 1 and 100.", "Promo Codes", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PromoCodeService.upsert(code, pct, activeBox.isSelected(), desc);
        loadTable();
    }
}
