package heg.backendspring.service;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Evaluation;
import heg.backendspring.entity.Submission;
import heg.backendspring.entity.TP;
import heg.backendspring.mapping.MapperSubmission;
import heg.backendspring.models.SubmissionDto;
import heg.backendspring.repository.RepositorySubmission;
import heg.backendspring.utils.StudentNameUtils;
import heg.backendspring.utils.ZipUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceSubmission {

    // Constante par défaut pour le type de cours
    private static final String DEFAULT_COURSE_TYPE = "JAVA";

    //==============================
    //       DEPENDANCES
    //==============================
    private final RepositorySubmission repositorySubmission;
    private final MapperSubmission mapperSubmission;
    private final SubmissionRestructureHelper restructureHelper;


    //==============================
    //    CRUD COURSE METHODS
    //==============================

    /**
     * Trouver une soumission par son id
     */
    public Optional<SubmissionDto> findSubmissionById(Long submissionId) {
        return repositorySubmission.findById(submissionId).map(mapperSubmission::toDto);
    }

    /**
     * Lance une restructuration du rendu d'un TP.
     * Thread-safe: le type de cours est passé en paramètre, pas stocké en variable d'instance.
     */
    @Transactional
    public void processZipSubmissionForTP(Course c, TP tp) throws IOException {
        Submission submission = tp.getSubmission();
        if (submission == null) {
            log.error("Aucune soumission trouvée pour le TP id {} du cours {}", tp.getId(), c.getName());
            return;
        }
        // Déterminer le type de cours (thread-safe)
        String courseType = (c.getCourseType() != null)
                ? c.getCourseType().name()
                : DEFAULT_COURSE_TYPE;

        log.info("Starting restructuring for TP {} of course {} with submission id {}", tp.getNo(), c.getName(), submission.getId());
        processZipSubmission(submission, "TP" + tp.getNo() + "_RenduRestructuration.zip", courseType);
    }

    /**
     * Lance une restructuration du rendu d'une Evaluation.
     * Thread-safe: le type de cours est passé en paramètre.
     */
    @Transactional
    public void processZipSubmissionForEvaluation(Course c, Evaluation evaluation) throws IOException {
        Submission submission = evaluation.getSubmission();
        if (submission == null) {
            log.error("Aucune soumission trouvée pour l'evaluation id {} du cours {}", evaluation.getId(), c.getName());
            return;
        }
        // Déterminer le type de cours (thread-safe)
        String courseType = (c.getCourseType() != null)
                ? c.getCourseType().name()
                : DEFAULT_COURSE_TYPE;

        log.info("Starting restructuring for Evaluation {} of course {} with submission id {}", evaluation.getName(), c.getName(), submission.getId());
        processZipSubmission(submission, evaluation.getName() + "_RenduRestructuration.zip", courseType);
    }

    /**
     * Async version of processZipSubmissionForTP.
     * Processes ZIP file restructuring in a background thread.
     *
     * @return CompletableFuture that completes when processing is done
     */
    @Async("zipProcessingExecutor")
    @Transactional
    public CompletableFuture<Void> processZipSubmissionForTPAsync(Course c, TP tp) {
        try {
            processZipSubmissionForTP(c, tp);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            log.error("Async ZIP processing failed for TP {} of course {}", tp.getNo(), c.getName(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Async version of processZipSubmissionForEvaluation.
     * Processes ZIP file restructuring in a background thread.
     *
     * @return CompletableFuture that completes when processing is done
     */
    @Async("zipProcessingExecutor")
    @Transactional
    public CompletableFuture<Void> processZipSubmissionForEvaluationAsync(Course c, Evaluation evaluation) {
        try {
            processZipSubmissionForEvaluation(c, evaluation);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            log.error("Async ZIP processing failed for Evaluation {} of course {}", evaluation.getName(), c.getName(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Restructure le rendu d'un fichier ZIP.
     * <p>
     * Logique de restructuration : Chaque dossier dans le ZIP est un étudiant (le
     * nom du fichier contient les infos de rendu, dont le nom au début) et
     * contient dedans un fichier ZIP qui est son rendu : ce fichier ZIP est un
     * projet Java ou Python qu'il faut dézipper, et récupérer son contenu pour le
     * stocker dans un nouveau dossier "Nom_Prenom" qui sera lui-même dans un dossier
     * "RenduRestructuration" et qui sera zippé puis stocké au même endroit que le ZIP
     * d'origine.
     *
     * @param submission        La soumission à traiter
     * @param nomZipRestructure Le nom du fichier ZIP restructuré
     * @param courseType        Le type de cours (JAVA, PYTHON, JAVA_JEE) - thread-safe
     */
    @Transactional
    public void processZipSubmission(Submission submission, String nomZipRestructure, String courseType) throws IOException {

        // chemin vers le zip d'origine
        Path originalZip = Paths.get(submission.getPathStorage());

        // chemin racine du TP : "DocumentsZips/{CODE_COURS}/TP{no}"
        Path tpRoot = originalZip.getParent();

        // chemin vers le dossier de restructuration
        Path restructurationDir = tpRoot.resolve("RenduRestructuration");
        restructureHelper.createFolderIfPossible(restructurationDir);

        // dossier temporaire pour extraire le zip initial
        Path tpmExtractDir = tpRoot.resolve("tmpExtract");
        restructureHelper.createFolderIfPossible(tpmExtractDir);

        // extraire le zip d'origine dans le dossier temporaire
        ZipUtils.unzip(originalZip, tpmExtractDir);

        // Parcourir chaque dossier étudiant (passer courseType pour thread-safety)
        browseStudentFolders(tpmExtractDir, restructurationDir, courseType);

        // Créer un zip global pour le dossier de restructuration
        Path zipRestructure = tpRoot.resolve(nomZipRestructure);
        ZipUtils.zipDirectory(restructurationDir, zipRestructure);

        // Mettre à jour le chemin du zip restructuré
        submission.setPathFileStructured(zipRestructure.toString());
        log.info("Submission path with restructurated data updated: {}", submission.getPathFileStructured());

        // Nettoyer les dossiers temporaires et le dossier de restructuration
        ZipUtils.deleteFolder(tpmExtractDir);
        ZipUtils.deleteFolder(restructurationDir);
    }

    /**
     * Méthode permettant de retourner la liste des étudiants ayant rendu leur TP
     */
    public List<String> getStudentsSubmission(Submission submission) {
        List<String> etudiants = new ArrayList<>();

        // Aucun rendu restructuré disponible (zip pas encore traité) → retourner une liste vide
        if (submission == null || submission.getPathFileStructured() == null
                || submission.getPathFileStructured().isBlank()) {
            log.warn("Aucun rendu restructuré disponible — retour d'une liste d'étudiants vide");
            return etudiants;
        }

        //Récupérer le path du zip restructuré
        Path zipRestructure = Paths.get(submission.getPathFileStructured());
        if (!Files.exists(zipRestructure)) {
            log.warn("Le fichier de rendu restructuré n'existe pas sur le disque : {}", zipRestructure);
            return etudiants;
        }

        try {
            //Extraire le zip
            Path extractDir = Files.createTempDirectory("extracted");
            ZipUtils.unzip(zipRestructure, extractDir);

            //Récupérer la liste des dossiers étudiants
            Files.list(extractDir)
                    .filter(Files::isDirectory)
                    .forEach(etudiantDir -> {
                        String nomEtudiant = etudiantDir.getFileName().toString();
                        etudiants.add(nomEtudiant);
                    });

            //Supprimer le dossier temporaire
            ZipUtils.deleteFolder(extractDir);
        } catch (IOException e) {
            log.error("Error while retrieving the list of students", e);
        }

        return etudiants;
    }


    // --------------------------------------------------------------------------
    // Méthodes privées "utilitaires"
    // --------------------------------------------------------------------------

    /**
     * Parcourt les dossiers étudiants dans le répertoire temporaire.
     *
     * @param courseType Le type de cours (passé pour thread-safety)
     */
    private void browseStudentFolders(Path tpmExtractDir, Path restructurationDir, String courseType) throws IOException {
        Files.list(tpmExtractDir)
                .filter(Files::isDirectory)
                .forEach(dossierEtudiant -> {
                    try {
                        manageStudentSubmission(dossierEtudiant, restructurationDir, courseType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Gère le rendu d'un étudiant.
     * Récupère son nom et prénom pour créer un dossier "Nom_Prenom".
     *
     * @param courseType Le type de cours (passé pour thread-safety)
     */
    private void manageStudentSubmission(Path etudiantDir, Path restructurationDir, String courseType) throws IOException {
        // Nom du dossier : Nom Prenom_xxxxxxxx (ne pas prendre après le _)
        String studentName = etudiantDir.getFileName().toString();

        // Corriger le nom
        String nameStudentFormatted = StudentNameUtils.toFolderNameFromZipDir(studentName);

        // Créer le dossier "Nom_Prenom" dans le dossier de restructuration
        Path studentDirRestructured = restructurationDir.resolve(nameStudentFormatted);
        restructureHelper.createFolderIfPossible(studentDirRestructured);

        // Gérer le contenu du dossier étudiant pour le restructurer
        restructureHelper.sendStudentContent(etudiantDir, studentDirRestructured, courseType);
    }

}
