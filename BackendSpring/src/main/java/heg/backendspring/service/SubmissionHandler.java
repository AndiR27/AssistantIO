package heg.backendspring.service;

import heg.backendspring.entity.Submission;
import heg.backendspring.entity.Submittable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service centralisé pour la gestion des soumissions (TP et Evaluation).
 * Évite la duplication de code entre ServiceTP et ServiceEvaluation.
 */
@Slf4j
@Service
public class SubmissionHandler {

    @Value("${zip-storage.path}")
    private String zipStoragePath;

    /**
     * Ajoute une soumission (fichier ZIP) à une entité Submittable (TP ou Evaluation).
     *
     * @param entity  L'entité cible (TP ou Evaluation)
     * @param zipFile Le flux du fichier ZIP à stocker
     * @return true si l'opération a réussi, false sinon
     */
    public boolean addSubmission(Submittable entity, InputStream zipFile) {
        if (zipFile == null) {
            log.warn("Zip file is null for entity {}", entity.getSubmissionName());
            return false;
        }

        String nomFichier = entity.getSubmissionName() + "_RenduCyberlearn.zip";
        String codeCours = entity.getCourse().getCode();
        Path folder = Paths.get(zipStoragePath, codeCours, entity.getFolderName());
        Path cheminVersZip = folder.resolve(nomFichier);

        try {
            // S'assurer que le dossier existe
            Files.createDirectories(folder);
            // Copier le fichier
            Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
            // Créer la soumission
            entity.setSubmission(new Submission(nomFichier, cheminVersZip.toString(), null));
            log.info("Submission added for {} at {}", entity.getSubmissionName(), cheminVersZip);
            return true;
        } catch (IOException e) {
            log.error("Unable to copy zip file for {}: {}", entity.getSubmissionName(), e.getMessage());
            return false;
        }
    }

    /**
     * Récupère le fichier ZIP restructuré d'une entité Submittable.
     *
     * @param entity L'entité (TP ou Evaluation)
     * @return Le fichier restructuré ou null si non trouvé
     */
    public File getRestructuredFile(Submittable entity) {
        Submission submission = entity.getSubmission();
        if (submission != null && submission.getPathFileStructured() != null) {
            log.info("Path to restructured file: {}", submission.getPathFileStructured());
            Path pathFile = Paths.get(submission.getPathFileStructured());
            return pathFile.toFile();
        }
        log.warn("No restructured file found for {}", entity.getSubmissionName());
        return null;
    }

    /**
     * Construit le chemin vers le dossier de stockage d'une entité.
     *
     * @param entity L'entité Submittable
     * @return Le chemin du dossier
     */
    public Path getStoragePath(Submittable entity) {
        return Paths.get(zipStoragePath, entity.getCourse().getCode(), entity.getFolderName());
    }

    /**
     * Stocke un fichier mapping.txt à côté de la soumission ZIP de l'entité.
     * Met à jour le chemin du mapping sur l'entité Submission associée.
     *
     * @param entity   L'entité Submittable (doit déjà avoir une Submission)
     * @param mappingFile Le flux du fichier mapping.txt à stocker
     * @return true si l'opération a réussi, false sinon
     */
    public boolean addMappingFile(Submittable entity, InputStream mappingFile) {
        if (mappingFile == null) {
            log.warn("Mapping file is null for entity {}", entity.getSubmissionName());
            return false;
        }
        Submission submission = entity.getSubmission();
        if (submission == null) {
            log.warn("Aucune soumission existante pour {}, impossible d'attacher un mapping", entity.getSubmissionName());
            return false;
        }

        Path folder = getStoragePath(entity);
        Path mappingPath = folder.resolve("mapping.txt");
        try {
            Files.createDirectories(folder);
            Files.copy(mappingFile, mappingPath, StandardCopyOption.REPLACE_EXISTING);
            submission.setPathMappingFile(mappingPath.toString());
            log.info("Mapping file stored for {} at {}", entity.getSubmissionName(), mappingPath);
            return true;
        } catch (IOException e) {
            log.error("Unable to copy mapping file for {}: {}", entity.getSubmissionName(), e.getMessage());
            return false;
        }
    }
}
