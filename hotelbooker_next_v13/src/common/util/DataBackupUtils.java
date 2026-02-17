package common.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Backup/restore utility for the local txt data files.
 *
 * Backups are ZIP files containing the /data folder contents.
 */
public class DataBackupUtils {

    private static final Path DATA_DIR = Paths.get("data");

    public static Path ensureBackupsDir() throws IOException {
        Path dir = Paths.get("backups");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        return dir;
    }

    public static Path createTimestampedBackupZip() throws IOException {
        Path backups = ensureBackupsDir();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path target = backups.resolve("backup_" + ts + ".zip");
        backupTo(target);
        return target;
    }

    public static void backupTo(Path targetZip) throws IOException {
        if (!Files.exists(DATA_DIR)) {
            throw new FileNotFoundException("Data folder not found: " + DATA_DIR.toAbsolutePath());
        }
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(targetZip)))) {
            Files.walk(DATA_DIR)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String rel = DATA_DIR.relativize(path).toString().replace('\\', '/');
                            ZipEntry entry = new ZipEntry("data/" + rel);
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException uio) {
            throw uio.getCause();
        }
    }

    /**
     * Restores /data from a backup zip.
     * This will overwrite existing files in /data.
     */
    public static void restoreFrom(Path zipFile) throws IOException {
        if (!Files.exists(zipFile)) throw new FileNotFoundException(zipFile.toString());
        if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);

        // Safety: take an automatic backup before restore
        createTimestampedBackupZip();

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().replace('\\', '/');
                if (!name.startsWith("data/")) {
                    // ignore unexpected entries
                    continue;
                }
                Path out = Paths.get(name);
                if (out.getParent() != null && !Files.exists(out.getParent())) {
                    Files.createDirectories(out.getParent());
                }
                Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
            }
        }
    }
}
