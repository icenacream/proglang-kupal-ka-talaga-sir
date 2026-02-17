package admin.ui;

import common.filehandler.TransactionFileHandler;
import common.util.CsvExportUtils;
import guest.ui.UIStyles;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * One-stop reports page: exports CSV + bundles receipts.
 */
public class AdminReportsPanel extends JPanel {

    public AdminReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyles.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        JLabel title = new JLabel("Reports");
        title.setFont(UIStyles.FONT_TITLE.deriveFont(20f));
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("Export CSV and bundle receipts in one place.");
        hint.setFont(UIStyles.FONT_PLAIN);
        hint.setForeground(UIStyles.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hint);
        body.add(Box.createVerticalStrut(12));

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);

        JButton bookingsCsv = new JButton("Export Bookings CSV");
        JButton guestsCsv = new JButton("Export Guests CSV");
        JButton zipReceipts = new JButton("Zip Receipts (PDF + TXT)");
        JButton zipAll = new JButton("Zip Reports (exports + receipts)");

        for (JButton b : new JButton[]{bookingsCsv, guestsCsv, zipReceipts, zipAll}) {
            b.setFont(UIStyles.FONT_BOLD);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(260, 46));
        }

        bookingsCsv.addActionListener(e -> exportBookingsCsv());
        guestsCsv.addActionListener(e -> exportGuestsCsv());
        zipReceipts.addActionListener(e -> zipReceipts());
        zipAll.addActionListener(e -> zipAllReports());

        grid.add(bookingsCsv);
        grid.add(guestsCsv);
        grid.add(zipReceipts);
        grid.add(zipAll);

        body.add(grid);
        add(body, BorderLayout.CENTER);
    }

    private void exportBookingsCsv() {
        try {
            Path p = CsvExportUtils.exportBookings(TransactionFileHandler.readBookingsFromFile(), TransactionFileHandler.readRoomsFromFile());
            JOptionPane.showMessageDialog(this, "Bookings CSV exported to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not export bookings CSV.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportGuestsCsv() {
        try {
            Path p = CsvExportUtils.exportGuests(common.service.UserService.getInstance().getAllUsers());
            JOptionPane.showMessageDialog(this, "Guests CSV exported to:\n" + p.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not export guests CSV.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void zipReceipts() {
        try {
            Files.createDirectories(Paths.get("exports"));
            Path receiptsDir = Paths.get("receipts");
            Files.createDirectories(receiptsDir);
            Path zip = Paths.get("exports").resolve("receipts_" + ts() + ".zip");
            zipDirectory(receiptsDir, zip, "receipts");
            JOptionPane.showMessageDialog(this, "Receipts ZIP created:\n" + zip.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not zip receipts.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void zipAllReports() {
        try {
            Files.createDirectories(Paths.get("exports"));
            Path zip = Paths.get("exports").resolve("reports_" + ts() + ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zip)))) {
                Path exportsDir = Paths.get("exports");
                if (Files.exists(exportsDir)) addDirToZip(exportsDir, zos, "exports");
                Path receiptsDir = Paths.get("receipts");
                if (Files.exists(receiptsDir)) addDirToZip(receiptsDir, zos, "receipts");
            }

            JOptionPane.showMessageDialog(this, "Reports ZIP created:\n" + zip.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create reports ZIP.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String ts() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private static void zipDirectory(Path dir, Path targetZip, String rootName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(targetZip)))) {
            addDirToZip(dir, zos, rootName);
        }
    }

    private static void addDirToZip(Path dir, ZipOutputStream zos, String rootName) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir).filter(Files::isRegularFile).forEach(path -> {
            try {
                String rel = dir.relativize(path).toString().replace('\\', '/');
                ZipEntry entry = new ZipEntry(rootName + "/" + rel);
                zos.putNextEntry(entry);
                Files.copy(path, zos);
                zos.closeEntry();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
