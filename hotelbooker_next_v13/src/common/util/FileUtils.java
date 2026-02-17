package common.util;

import java.io.*;
import java.nio.file.*;

public class FileUtils {
    /**
     * Copies a file to the given directory and returns the relative path (dir/name).
     */
    public static String copyToDir(File src, String destDir, String destFileName) throws IOException {
        if (src == null) throw new IOException("Source file is null");
        Files.createDirectories(Paths.get(destDir));
        Path dest = Paths.get(destDir, destFileName);
        Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        // Use forward slashes for portability in our text files
        return (destDir + "/" + destFileName).replace("\\", "/");
    }

    public static void ensureParentDir(Path file) {
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException ignore) {
        }
    }

    public static java.util.List<String> readAllLinesSafe(Path file) {
        try {
            if (!Files.exists(file)) return java.util.List.of();
            return Files.readAllLines(file);
        } catch (IOException e) {
            return java.util.List.of();
        }
    }

    public static void writeAllLines(Path file, java.util.List<String> lines) {
        ensureParentDir(file);
        try {
            Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignore) {
        }
    }
}
