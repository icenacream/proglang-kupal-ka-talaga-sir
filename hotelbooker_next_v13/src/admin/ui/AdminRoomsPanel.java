package admin.ui;

import common.filehandler.TransactionFileHandler;
import common.model.Room;
import common.util.FileUtils;
import guest.ui.UIStyles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Admin panel to manage rooms (CRUD) backed by data/master/rooms.txt
 */
public class AdminRoomsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JLabel info;

    public AdminRoomsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);
        initUI();
        refresh();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        JLabel title = new JLabel("Manage Rooms");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = new JButton("Add Room");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton toggle = new JButton("Toggle Availability");
        JButton refresh = new JButton("Refresh");
        for (JButton b : new JButton[]{add, edit, delete, toggle, refresh}) {
            b.setFont(UIStyles.FONT_PLAIN);
        }
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(toggle);
        actions.add(refresh);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Hotel", "Room Type", "Location", "Price", "Rating", "Reviews", "Capacity", "Units", "Available", "Image"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIStyles.FONT_PLAIN);
        table.getTableHeader().setFont(UIStyles.FONT_BOLD);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        info = new JLabel(" ");
        info.setFont(UIStyles.FONT_PLAIN);
        info.setForeground(UIStyles.MUTED);
        footer.add(info, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        delete.addActionListener(e -> onDelete());
        toggle.addActionListener(e -> onToggle());
        refresh.addActionListener(e -> refresh());
    }

    private void refresh() {
        model.setRowCount(0);
        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        for (Room r : rooms) {
            model.addRow(new Object[]{
                    r.getId(), r.getHotelName(), r.getRoomType(), r.getLocation(),
                    r.getPricePerNight(), r.getRating(), r.getReviewCount(),
                    r.getCapacity(), r.getUnits(), r.isAvailable(), r.getImagePath()
            });
        }
        info.setText("Rooms: " + rooms.size());
    }

    private void onAdd() {
        RoomEditorDialog dialog = new RoomEditorDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        Room created = dialog.getResult();
        if (created != null) {
            TransactionFileHandler.updateRoom(created);
            refresh();
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room to edit.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = String.valueOf(model.getValueAt(row, 0));
        Room existing = TransactionFileHandler.readRoomsFromFile().stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
        if (existing == null) return;

        RoomEditorDialog dialog = new RoomEditorDialog(SwingUtilities.getWindowAncestor(this), existing);
        dialog.setVisible(true);
        Room updated = dialog.getResult();
        if (updated != null) {
            TransactionFileHandler.updateRoom(updated);
            refresh();
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room to delete.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = String.valueOf(model.getValueAt(row, 0));
        int ok = JOptionPane.showConfirmDialog(this, "Delete room " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        rooms.removeIf(r -> r.getId().equalsIgnoreCase(id));
        // rewrite by updating each; simplest: overwrite file via updateRoom loop
        // We'll just rebuild by writing through updateRoom on a clean file.
        // (small dataset, OK)
        // Direct rewrite helper isn't exposed, so we do a safe rewrite here.
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get("data/master/rooms.txt"), new byte[0]);
        } catch (Exception ignored) {}
        for (Room r : rooms) {
            TransactionFileHandler.saveRoom(r);
        }
        refresh();
    }

    private void onToggle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = String.valueOf(model.getValueAt(row, 0));
        Room room = TransactionFileHandler.readRoomsFromFile().stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
        if (room == null) return;
        room.setAvailable(!room.isAvailable());
        TransactionFileHandler.updateRoom(room);
        refresh();
    }

    /** Simple editor dialog */
    private static class RoomEditorDialog extends JDialog {
        private Room result;

        private JTextField idField;
        private JTextField hotelField;
        private JTextField roomTypeField;
        private JTextField locationField;
        private JTextField priceField;
        private JTextField ratingField;
        private JTextField reviewsField;
        private JTextField amenitiesField;
        private JTextField capacityField;
        private JTextField unitsField;
        private JCheckBox availableBox;
        private JTextField imageField;

        RoomEditorDialog(Window owner, Room existing) {
            super(owner, existing == null ? "Add Room" : "Edit Room", ModalityType.APPLICATION_MODAL);
            setSize(640, 520);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            form.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.anchor = GridBagConstraints.WEST;

            idField = new JTextField(18);
            hotelField = new JTextField(28);
            locationField = new JTextField(28);
            priceField = new JTextField(10);
            ratingField = new JTextField(10);
            reviewsField = new JTextField(10);
            amenitiesField = new JTextField(28);
            capacityField = new JTextField(10);
            unitsField = new JTextField(10);
            availableBox = new JCheckBox("Available");
            availableBox.setBackground(Color.WHITE);
            imageField = new JTextField(22);

            JButton browseImg = new JButton("Choose Image");
            browseImg.setFont(UIStyles.FONT_PLAIN);

            addRow(form, gbc, "ID", idField);
            addRow(form, gbc, "Hotel", hotelField);
            addRow(form, gbc, "Room Type", roomTypeField);
            addRow(form, gbc, "Location", locationField);
            addRow(form, gbc, "Price", priceField);
            addRow(form, gbc, "Rating", ratingField);
            addRow(form, gbc, "Reviews", reviewsField);
            addRow(form, gbc, "Amenities (comma)", amenitiesField);
            addRow(form, gbc, "Capacity", capacityField);
            addRow(form, gbc, "Units", unitsField);

            // Available
            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel(""), gbc);
            gbc.gridx = 1;
            form.add(availableBox, gbc);

            // Image row
            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Image"), gbc);
            gbc.gridx = 1;
            JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            imgRow.setOpaque(false);
            imgRow.add(imageField);
            imgRow.add(browseImg);
            form.add(imgRow, gbc);

            add(form, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            bottom.setBackground(Color.WHITE);
            JButton save = new JButton("Save");
            JButton cancel = new JButton("Cancel");
            save.setFont(UIStyles.FONT_PLAIN);
            cancel.setFont(UIStyles.FONT_PLAIN);
            bottom.add(cancel);
            bottom.add(save);
            add(bottom, BorderLayout.SOUTH);

            if (existing != null) {
                idField.setText(existing.getId());
                idField.setEditable(false);
                hotelField.setText(existing.getHotelName());
                roomTypeField.setText(existing.getRoomType());
                locationField.setText(existing.getLocation());
                priceField.setText(String.valueOf(existing.getPricePerNight()));
                ratingField.setText(String.valueOf(existing.getRating()));
                reviewsField.setText(String.valueOf(existing.getReviewCount()));
                amenitiesField.setText(String.join(", ", existing.getAmenities()));
                capacityField.setText(String.valueOf(existing.getCapacity()));
                unitsField.setText(String.valueOf(existing.getUnits()));
                availableBox.setSelected(existing.isAvailable());
                imageField.setText(existing.getImagePath());
            } else {
                idField.setText("R" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                availableBox.setSelected(true);
                unitsField.setText("1");
                imageField.setText("assets/images/city_center.jpg");
            }

            browseImg.addActionListener(e -> chooseImage());
            cancel.addActionListener(e -> { result = null; dispose(); });
            save.addActionListener(e -> onSave());
        }

        private static void addRow(JPanel form, GridBagConstraints gbc, String label, JComponent field) {
            gbc.gridx = 0;
            gbc.gridy++;
            JLabel l = new JLabel(label);
            l.setFont(UIStyles.FONT_PLAIN);
            form.add(l, gbc);
            gbc.gridx = 1;
            field.setFont(UIStyles.FONT_PLAIN);
            form.add(field, gbc);
        }

        private void chooseImage() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose room image");
            int res = chooser.showOpenDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;
            File file = chooser.getSelectedFile();
            if (file == null) return;

            try {
                String ext = "jpg";
                String name = file.getName();
                int dot = name.lastIndexOf('.');
                if (dot > 0 && dot < name.length() - 1) ext = name.substring(dot + 1);
                String safeName = ("room_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext).toLowerCase();
                String rel = FileUtils.copyToDir(file, "assets/images", safeName);
                imageField.setText(rel);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to copy image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onSave() {
            try {
                String id = idField.getText().trim();
                String hotel = hotelField.getText().trim();
                String roomType = roomTypeField.getText().trim();
                if (roomType.isEmpty()) roomType = "Standard Room";
                String loc = locationField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                double rating = Double.parseDouble(ratingField.getText().trim());
                int reviews = Integer.parseInt(reviewsField.getText().trim());
                String[] amenities = amenitiesField.getText().trim().isEmpty()
                        ? new String[]{"Free WiFi"}
                        : amenitiesField.getText().split(",");
                for (int i = 0; i < amenities.length; i++) amenities[i] = amenities[i].trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                int units = Integer.parseInt(unitsField.getText().trim());
                if (units <= 0) units = 1;
                boolean available = availableBox.isSelected();
                String image = imageField.getText().trim();
                if (image.isEmpty()) image = "assets/images/city_center.jpg";

                if (id.isEmpty() || hotel.isEmpty() || loc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill ID, Hotel, and Location.", "Invalid", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                result = new Room(id, hotel, roomType, loc, price, rating, reviews, amenities, capacity, units, available, image);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please check your inputs.\n" + ex.getMessage(), "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        }

        Room getResult() {
            return result;
        }
    }
}
