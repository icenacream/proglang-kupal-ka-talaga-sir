package common.util;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;

/**
 * Simple printing helpers (prints plain text receipts).
 */
public class PrintUtils {

    public static void printText(Component parent, String jobName, String text) {
        if (text == null) text = "";

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(jobName == null ? "Print" : jobName);

        String finalText = text;
        Printable printable = (graphics, pageFormat, pageIndex) -> {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font font = new Font("Monospaced", Font.PLAIN, 10);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int lineHeight = fm.getHeight();

            int linesPerPage = (int) (pageFormat.getImageableHeight() / lineHeight);
            String[] lines = finalText.split("\\R", -1);
            int numPages = (int) Math.ceil(lines.length / (double) Math.max(1, linesPerPage));

            if (pageIndex >= numPages) return Printable.NO_SUCH_PAGE;

            int start = pageIndex * linesPerPage;
            int end = Math.min(lines.length, start + linesPerPage);
            int y = 0;
            for (int i = start; i < end; i++) {
                y += lineHeight;
                g2.drawString(lines[i], 0, y);
            }
            return Printable.PAGE_EXISTS;
        };

        job.setPrintable(printable);

        boolean doPrint = job.printDialog();
        if (!doPrint) return;

        try {
            job.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(parent, "Printing failed: " + e.getMessage(), "Print error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
