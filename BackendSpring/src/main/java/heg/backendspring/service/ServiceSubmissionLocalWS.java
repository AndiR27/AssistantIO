package heg.backendspring.service;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Evaluation;
import heg.backendspring.entity.Submission;
import heg.backendspring.utils.StudentNameUtils;
import heg.backendspring.utils.SubmissionRestructureHelper;
import heg.backendspring.utils.ZipUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Variante de ServiceSubmission pour les Evaluations "Local WS".
 * <p>
 * Les dossiers du zip d'origine sont nommés "HEG-WS-XXXX". Un fichier mapping.txt
 * fournit la correspondance :
 * <pre>
 *   StudentA : HEG-WS-6134
 *   StudentB : HEG-WS-8182
 * </pre>
 * Le zip restructuré produit la même forme que le flux classique (dossiers nommés
 * par étudiant), pour que le reste du code (getStudentsSubmission, matching, etc.)
 * fonctionne à l'identique.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceSubmissionLocalWS {

    private static final String DEFAULT_COURSE_TYPE = "JAVA";
    // Tolère espaces, tirets ou underscores entre "HEG", "WS" et le numéro.
    // Ex: "HEG-WS-6134", "HEG_WS_6134", "HEG WS 6134", "HEG-WS- 6134", "HEG-WS6134".
    private static final Pattern HEG_WS_PATTERN = Pattern.compile(
            "HEG[\\s_-]*WS[\\s_-]*(\\d+)", Pattern.CASE_INSENSITIVE);

    private final SubmissionRestructureHelper restructureHelper;

    /**
     * Lance une restructuration du rendu d'une Evaluation en mode LOCAL_WS.
     */
    @Transactional
    public void processZipSubmissionForEvaluation(Course c, Evaluation evaluation) throws IOException {
        Submission submission = evaluation.getSubmission();
        if (submission == null) {
            log.error("Aucune soumission trouvée pour l'evaluation id {} du cours {}", evaluation.getId(), c.getName());
            return;
        }
        if (submission.getPathMappingFile() == null || submission.getPathMappingFile().isBlank()) {
            log.error("Aucun mapping.txt trouvé pour la soumission {} (LOCAL_WS)", submission.getId());
            throw new IOException("Mapping file required for LOCAL_WS mode");
        }

        String courseType = (c.getCourseType() != null)
                ? c.getCourseType().name()
                : DEFAULT_COURSE_TYPE;

        log.info("Starting LOCAL_WS restructuring for Evaluation {} of course {} with submission id {}",
                evaluation.getName(), c.getName(), submission.getId());
        processZipSubmission(submission, evaluation.getName() + "_RenduRestructuration.zip", courseType);
    }

    /**
     * Version asynchrone du traitement LOCAL_WS d'une Evaluation.
     */
    @Async("zipProcessingExecutor")
    @Transactional
    public CompletableFuture<Void> processZipSubmissionForEvaluationAsync(Course c, Evaluation evaluation) {
        try {
            processZipSubmissionForEvaluation(c, evaluation);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            log.error("Async LOCAL_WS ZIP processing failed for Evaluation {} of course {}",
                    evaluation.getName(), c.getName(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Coeur de la restructuration LOCAL_WS.
     * Produit un zip au même format que le flux classique : dossiers nommés par étudiant.
     */
    @Transactional
    public void processZipSubmission(Submission submission, String nomZipRestructure, String courseType) throws IOException {
        // Parser le mapping HEG-WS → nom d'étudiant
        Map<String, String> codeToStudent = parseMappingFile(Paths.get(submission.getPathMappingFile()));

        Path originalZip = Paths.get(submission.getPathStorage());
        Path root = originalZip.getParent();

        Path restructurationDir = root.resolve("RenduRestructuration");
        restructureHelper.createFolderIfPossible(restructurationDir);

        Path tmpExtractDir = root.resolve("tmpExtract");
        restructureHelper.createFolderIfPossible(tmpExtractDir);

        ZipUtils.unzip(originalZip, tmpExtractDir);

        RestructureStats stats = new RestructureStats();
        browseHegWsFolders(tmpExtractDir, restructurationDir, courseType, codeToStudent, stats);

        Path zipRestructure = root.resolve(nomZipRestructure);
        ZipUtils.zipDirectory(restructurationDir, zipRestructure);

        submission.setPathFileStructured(zipRestructure.toString());
        log.info("LOCAL_WS restructuration terminée → étudiants trouvés : {}, dossiers sans correspondance : {} (zip : {})",
                stats.found, stats.notFound, submission.getPathFileStructured());

        ZipUtils.deleteFolder(tmpExtractDir);
        ZipUtils.deleteFolder(restructurationDir);
    }

    /** Compteur local (thread-safe au scope du traitement) pour le récap final. */
    private static final class RestructureStats {
        int found;
        int notFound;
    }

    // --------------------------------------------------------------------------
    // Méthodes privées "utilitaires"
    // --------------------------------------------------------------------------

    /**
     * Parcourt les dossiers HEG-WS dans le répertoire temporaire.
     * Le zip d'origine contient en général un dossier "wrapper" (ex. 63-51_FM_Q/) qui
     * englobe les dossiers HEG-WS-XXXX. On descend donc d'un niveau si le dossier
     * top-level n'est pas lui-même un HEG-WS.
     * Les dossiers meta (__MACOSX, fichiers cachés) sont ignorés.
     */
    private void browseHegWsFolders(Path extractDir,
                                    Path restructurationDir,
                                    String courseType,
                                    Map<String, String> codeToStudent,
                                    RestructureStats stats) throws IOException {
        try (java.util.stream.Stream<Path> entries = Files.list(extractDir)) {
            entries.filter(Files::isDirectory)
                    .filter(p -> !isMetaDir(p))
                    .forEach(dir -> {
                        try {
                            if (extractHegWsCode(dir.getFileName().toString()) != null) {
                                // Dossier HEG-WS directement au niveau racine
                                manageHegWsSubmission(dir, restructurationDir, courseType, codeToStudent, stats);
                            } else {
                                // Dossier wrapper → descendre d'un niveau pour trouver les HEG-WS
                                browseHegWsChildren(dir, restructurationDir, courseType, codeToStudent, stats);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    /**
     * Parcourt les enfants directs d'un dossier "wrapper" pour y trouver les HEG-WS.
     * Ignore les entrées non-HEG-WS et les dossiers meta.
     */
    private void browseHegWsChildren(Path wrapperDir,
                                     Path restructurationDir,
                                     String courseType,
                                     Map<String, String> codeToStudent,
                                     RestructureStats stats) throws IOException {
        try (java.util.stream.Stream<Path> children = Files.list(wrapperDir)) {
            children.filter(Files::isDirectory)
                    .filter(p -> !isMetaDir(p))
                    .forEach(child -> {
                        String name = child.getFileName().toString();
                        if (extractHegWsCode(name) == null) {
                            log.debug("Entrée ignorée (pas un dossier HEG-WS) : {}", name);
                            return;
                        }
                        try {
                            manageHegWsSubmission(child, restructurationDir, courseType, codeToStudent, stats);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    /** Filtre les dossiers meta à ignorer (macOS, cachés). */
    private boolean isMetaDir(Path p) {
        String name = p.getFileName().toString();
        return name.equals("__MACOSX") || name.startsWith(".");
    }

    /**
     * Gère un dossier HEG-WS : trouve le nom d'étudiant mappé, crée le dossier cible
     * avec le nom de l'étudiant, puis délègue au helper pour la restructuration du contenu.
     */
    private void manageHegWsSubmission(Path hegWsDir,
                                       Path restructurationDir,
                                       String courseType,
                                       Map<String, String> codeToStudent,
                                       RestructureStats stats) throws IOException {
        String folderName = hegWsDir.getFileName().toString();
        String hegWsCode = extractHegWsCode(folderName);

        String nameFormatted;
        if (hegWsCode != null && codeToStudent.containsKey(hegWsCode)) {
            // Correspondance trouvée → normalisation identique au flux classique
            String studentName = codeToStudent.get(hegWsCode);
            log.debug("Mapped {} -> {}", hegWsCode, studentName);
            nameFormatted = StudentNameUtils.toFolderNameFromZipDir(studentName);
            if (nameFormatted.isEmpty()) {
                nameFormatted = hegWsCode + "_NotFound";
                stats.notFound++;
            } else {
                stats.found++;
            }
        } else {
            // Pas de correspondance → conserver le code brut avec suffixe _NotFound
            // (on NE passe PAS par le normalizer : il écraserait les chiffres et on
            // se retrouverait avec plusieurs dossiers collidant sur "HEGWS").
            nameFormatted = (hegWsCode != null ? hegWsCode : folderName) + "_NotFound";
            stats.notFound++;
        }

        Path studentDirRestructured = restructurationDir.resolve(nameFormatted);
        restructureHelper.createFolderIfPossible(studentDirRestructured);

        restructureHelper.sendStudentContentLocal(hegWsDir, studentDirRestructured, courseType);
    }

    /**
     * Extrait le code "HEG-WS-XXXX" d'un nom de dossier et le normalise en
     * "HEG-WS-<digits>" pour garantir une clé uniforme côté mapping.
     * Retourne null si aucun code HEG-WS valide n'est trouvé.
     */
    private String extractHegWsCode(String folderName) {
        if (folderName == null) return null;
        Matcher m = HEG_WS_PATTERN.matcher(folderName);
        if (m.find()) {
            return "HEG-WS-" + m.group(1);
        }
        return null;
    }

    /**
     * Parse le fichier mapping.txt en Map<HEG-WS-CODE, StudentName>.
     * Format attendu par ligne : "StudentName : HEG-WS-XXXX".
     * Ignore les lignes vides et les lignes commençant par '#'.
     * En cas de doublon, le dernier remplace le précédent (avec warn).
     */
    public Map<String, String> parseMappingFile(Path mappingFile) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (!Files.exists(mappingFile)) {
            log.error("Mapping file not found: {}", mappingFile);
            throw new IOException("Mapping file not found: " + mappingFile);
        }

        List<String> lines = Files.readAllLines(mappingFile, StandardCharsets.UTF_8);
        for (String rawLine : lines) {
            if (rawLine == null) continue;
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int sep = line.indexOf(':');
            if (sep < 0) {
                log.warn("Ligne de mapping ignorée (pas de ':'): {}", rawLine);
                continue;
            }
            String studentName = line.substring(0, sep).trim();
            String codePart = line.substring(sep + 1).trim();

            Matcher matcher = HEG_WS_PATTERN.matcher(codePart);
            if (!matcher.find()) {
                log.warn("Ligne de mapping ignorée (code HEG-WS introuvable): {}", rawLine);
                continue;
            }
            String code = "HEG-WS-" + matcher.group(1);

            if (studentName.isEmpty()) {
                log.warn("Ligne de mapping ignorée (nom d'étudiant vide): {}", rawLine);
                continue;
            }

            if (map.containsKey(code)) {
                log.warn("Code {} dupliqué dans le mapping — remplacement '{}' -> '{}'",
                        code, map.get(code), studentName);
            }
            map.put(code, studentName);
        }

        log.info("Mapping parsé : {} entrées", map.size());
        return map;
    }
}
