package heg.backendspring.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class ZipUtils {

    // =============================
    //            UNZIP
    // =============================
    public void unzip(Path zipFile, Path targetDir) throws IOException {
        if (Files.notExists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (FileSystem zipFs = newZipFileSystem(zipFile)) {
            for (Path root : zipFs.getRootDirectories()) {
                Files.walkFileTree(root, new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult preVisitDirectory(
                            Path dir, BasicFileAttributes attrs
                    ) throws IOException {
                        Path rel = root.relativize(dir);
                        Path destDir = targetDir.resolve(rel.toString());
                        Files.createDirectories(destDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file, BasicFileAttributes attrs
                    ) throws IOException {
                        Path rel = root.relativize(file);
                        Path destFile = targetDir.resolve(rel.toString());
                        Files.createDirectories(destFile.getParent());
                        Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }


    // =============================
    //              ZIP
    // =============================
    public void zipDirectory(Path sourceDir, Path zipFile) throws IOException {

        if (Files.notExists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("Source directory not valid: " + sourceDir);
        }

        if (zipFile.getParent() != null && Files.notExists(zipFile.getParent())) {
            Files.createDirectories(zipFile.getParent());
        }

        URI uri = URI.create("jar:" + zipFile.toUri());
        Map<String, String> env = new HashMap<>();
        env.put("create", String.valueOf(Files.notExists(zipFile)));

        try (FileSystem zipFs = FileSystems.newFileSystem(uri, env)) {

            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(
                        Path dir, BasicFileAttributes attrs
                ) throws IOException {
                    Path rel = sourceDir.relativize(dir);
                    if (!rel.toString().isEmpty()) {
                        Files.createDirectories(zipFs.getPath(rel.toString()));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(
                        Path file, BasicFileAttributes attrs
                ) throws IOException {
                    Path rel = sourceDir.relativize(file);
                    Path dest = zipFs.getPath(rel.toString());
                    if (dest.getParent() != null) {
                        Files.createDirectories(dest.getParent());
                    }
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public void extract7z(Path sevenZipFile, Path outputDir) {
        try (SevenZFile sevenZFile = new SevenZFile(sevenZipFile.toFile())) {

            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            SevenZArchiveEntry entry;

            while ((entry = sevenZFile.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    continue;
                }

                Path outputFile = outputDir.resolve(entry.getName());
                Files.createDirectories(outputFile.getParent());

                byte[] buffer = new byte[(int) entry.getSize()];
                int read = sevenZFile.read(buffer);

                if (read > 0) {
                    Files.write(outputFile, buffer, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    log.info("Extracted 7z entry: {}", outputFile);
                }
            }

        } catch (IOException e) {
            log.error("Error extracting 7z file {} → {}", sevenZipFile, e.getMessage(), e);
        }
    }

    public void copyJavaProject(Path sourceDir, Path targetDir) {
        Set<String> ignoreDirs = Set.of(".git", ".idea", "target", "build", "out");
        Set<String> ignoreFiles = Set.of(".DS_Store", "Thumbs.db", "desktop.ini",
                ".iml", ".pdf", ".docx", ".txt");
        copyAll(sourceDir, targetDir, ignoreDirs, ignoreFiles);
    }

    public void copyPythonProject(Path sourceDir, Path targetDir) {
        Set<String> ignoreDirs = Set.of(".git", ".idea", "venv", "__pycache__");
        Set<String> ignoreFiles = Set.of(".DS_Store", "Thumbs.db", "desktop.ini",
                ".iml", ".pdf", ".docx", ".txt");
        copyAll(sourceDir, targetDir, ignoreDirs, ignoreFiles);
    }

    /**
     * Copie un projet Java JEE/Maven (multi-modules) en filtrant les outputs.
     */
    public void copyJavaJEEProject(Path sourceDir, Path targetDir) {
        Set<String> ignoreDirs = Set.of(
                ".git", ".idea", "target", "build", "out",
                "logs", ".settings", ".mvn", "node_modules"
        );
        Set<String> ignoreFiles = Set.of(
                ".DS_Store", "Thumbs.db", "desktop.ini",
                ".iml", ".log"
        );
        copyAll(sourceDir, targetDir, ignoreDirs, ignoreFiles);
    }

    /**
     * Copie récursivement les fichiers d'un projet tout en filtrant certains fichiers/dossiers.
     */
    public void copyAll(Path sourceDir, Path targetDir,
                        Set<String> ignoreDirs,
                        Set<String> ignoreFiles) {
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream
                    .filter(path -> !path.equals(sourceDir))
                    .filter(path -> {
                        String filename = path.getFileName().toString();

                        if (Files.isDirectory(path) && ignoreDirs.contains(filename)) {
                            return false;
                        }

                        return ignoreFiles.stream().noneMatch(filename::endsWith);
                    })
                    .forEach(path -> {
                        Path destPath = targetDir.resolve(sourceDir.relativize(path));
                        try {
                            if (Files.isDirectory(path)) {
                                Files.createDirectories(destPath);
                            } else {
                                Files.createDirectories(destPath.getParent());
                                Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            log.error("Error while copying {} to {}", path, destPath, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error while traversing {}", sourceDir, e);
        }
    }

    public void deleteFolder(Path dir) {
        if (Files.notExists(dir)) {
            return;
        }

        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc)
                        throws IOException {
                    Files.deleteIfExists(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed to delete directory {}", dir, e);
        }
    }


    // =============================
    //         PRIVATE HELPERS
    // =============================
    private FileSystem newZipFileSystem(Path zipFile) throws IOException {
        if (Files.notExists(zipFile)) {
            throw new NoSuchFileException("Zip file does not exist: " + zipFile);
        }
        URI uri = URI.create("jar:" + zipFile.toUri());
        Map<String, String> env = Map.of("create", "false");

        try {
            return FileSystems.newFileSystem(uri, env);
        } catch (IOException e) {
            throw new IOException("Unable to open ZIP filesystem for: " + zipFile, e);
        }
    }
}
