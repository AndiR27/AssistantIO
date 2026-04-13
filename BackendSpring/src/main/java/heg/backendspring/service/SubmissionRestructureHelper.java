package heg.backendspring.service;

import heg.backendspring.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Helpers partagés pour la restructuration des rendus ZIP.
 * Utilisés à la fois par ServiceSubmission (flux classique par nom d'étudiant)
 * et par ServiceSubmissionLocalWS (flux basé sur mapping HEG-WS).
 */
@Slf4j
@Component
public class SubmissionRestructureHelper {

    /**
     * Crée un répertoire s'il n'existe pas déjà.
     */
    public void createFolderIfPossible(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("Could not create directory: {}", dir, e);
        }
    }

    /**
     * Transfère le contenu d'un dossier source vers un dossier de restructuration,
     * en gérant le cas où le dossier source contient un sous-zip (projet étudiant).
     */
    public void sendStudentContent(Path sourceDir, Path destDirRestructured, String courseType) throws IOException {
        // Normalement : le rendu est un zip, mais dans le cas contraire, on récupère
        // juste ce qu'il y a dedans pour le stocker dans le dossier de restructuration.
        Optional<Path> zipEtudiant = findSubFolderZip(sourceDir);
        if (zipEtudiant.isPresent()) {
            Path zipEtudiantPath = zipEtudiant.get();

            // Extraire le contenu dans le dossier temporaire local
            Path projetExtract = sourceDir.resolve("extractedProject");
            createFolderIfPossible(projetExtract);
            manageExtractionZip(zipEtudiantPath, projetExtract);

            log.debug("Manage extraction for student project, courseType={}", courseType);
            // Gérer le contenu du projet à copier selon le type de cours
            switch (courseType) {
                case "JAVA" -> ZipUtils.copyJavaProject(projetExtract, destDirRestructured);
                case "PYTHON" -> ZipUtils.copyPythonProject(projetExtract, destDirRestructured);
                case "JAVA_JEE" -> ZipUtils.copyJavaJEEProject(projetExtract, destDirRestructured);
                default -> ZipUtils.copyAll(projetExtract, destDirRestructured, Set.of(), Set.of());
            }
        } else {
            ZipUtils.copyAll(sourceDir, destDirRestructured, Set.of(), Set.of());
        }
    }

    /**
     * Trouve un sous-zip (.zip, .7zip ou .rar) dans le dossier source, s'il existe
     */
    public Optional<Path> findSubFolderZip(Path dossierSource) {
        try (Stream<Path> files = Files.list(dossierSource)) {
            return files.filter(p -> {
                String fileName = p.toString().toLowerCase();
                return fileName.endsWith(".zip") || fileName.endsWith(".7z") || fileName.endsWith(".rar");
            }).findFirst();
        } catch (IOException e) {
            log.error("Error while searching for sub-zip in {}", dossierSource, e);
        }
        return Optional.empty();
    }

    /**
     * Gère la façon d'extraire le zip selon le type de zip
     */
    public void manageExtractionZip(Path zipPath, Path extractDir) throws IOException {
        String zipPathString = zipPath.toString();
        if (zipPathString.endsWith(".zip")) {
            ZipUtils.unzip(zipPath, extractDir);
        } else if (zipPathString.endsWith(".7z")) {
            ZipUtils.extract7z(zipPath, extractDir);
        }
    }
}
