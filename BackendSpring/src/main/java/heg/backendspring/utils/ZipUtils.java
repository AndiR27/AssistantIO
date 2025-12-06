package heg.backendspring.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
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

        FileSystem zipFs = newZipFileSystem(zipFile);
        if (zipFs == null) {
            // Fichier non valide ou impossible à ouvrir → on log déjà côté helper, on sort.
            return;
        }

        try (zipFs) {
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

    // =============================
    //           7-ZIP EXTRACTION
    // =============================
    public void extract7z(Path sevenZipFile, Path outputDir) {
        if (sevenZipFile == null || !Files.isRegularFile(sevenZipFile)) {
            return;
        }

        try (
                SeekableByteChannel channel = Files.newByteChannel(sevenZipFile);
                SevenZFile sevenZFile = SevenZFile.builder()
                        .setSeekableByteChannel(channel)
                        .get()
        ) {

            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            SevenZArchiveEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = sevenZFile.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    continue;
                }

                Path outputFile = outputDir.resolve(entry.getName()).normalize();

                // Anti path traversal
                if (!outputFile.startsWith(outputDir.normalize())) {
                    continue;
                }

                Files.createDirectories(outputFile.getParent());

                try (java.io.OutputStream out = Files.newOutputStream(
                        outputFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )) {

                    long remaining = entry.getSize();

                    if (remaining < 0) {
                        // Taille inconnue => lire jusqu'à EOF
                        int read;
                        while ((read = sevenZFile.read(buffer)) > 0) {
                            out.write(buffer, 0, read);
                        }
                    } else {
                        long toRead = remaining;
                        while (toRead > 0) {
                            int read = sevenZFile.read(buffer, 0, (int) Math.min(buffer.length, toRead));
                            if (read < 0) break;
                            out.write(buffer, 0, read);
                            toRead -= read;
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.debug("7z extraction skipped for {} ({})", sevenZipFile, e.getMessage());
        }
    }


    public void copyJavaProject(Path sourceDir, Path targetDir) {
        Set<String> ignoreDirs = Set.of(".git", ".idea", "target", "build", "out", "__MACOSX");
        Set<String> ignoreFiles = Set.of(".DS_Store", "Thumbs.db", "desktop.ini",
                ".iml", ".pdf", ".docx", ".txt", ".gitignore");
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

                        // Ne rien copier qui se trouve DANS un répertoire ignoré
                        Path current = path;
                        while (current != null && !current.equals(sourceDir)) {
                            String dirName = current.getFileName().toString();
                            if (ignoreDirs.contains(dirName)) {
                                return false; // on skip tout ce sous-arbre
                            }
                            current = current.getParent();
                        }

                        //Gestion des fichiers à ignorer
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
        if (!isValidZip(zipFile)) {
            log.warn("File {} is not a valid ZIP archive. Skipping.", zipFile);
            return null; // on signalera à unzip() de ne rien faire
        }

        URI uri = URI.create("jar:" + zipFile.toUri());
        Map<String, String> env = Map.of("create", "false");

        try {
            return FileSystems.newFileSystem(uri, env);
        } catch (IOException e) {
            log.warn("Unable to open ZIP filesystem for {} → {}. Skipping this archive.",
                    zipFile, e.getMessage());
            return null;
        }
    }

    private boolean isValidZip(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }

        // Vérifie la "magic signature" d'un ZIP : PK\003\004
        try (var in = Files.newInputStream(path)) {
            byte[] signature = new byte[4];
            int read = in.read(signature);
            if (read < 4) {
                return false;
            }
            return signature[0] == 'P'
                    && signature[1] == 'K'
                    && signature[2] == 3
                    && signature[3] == 4;
        } catch (IOException e) {
            return false;
        }
    }

}
