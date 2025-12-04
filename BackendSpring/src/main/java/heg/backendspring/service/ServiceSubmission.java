package heg.backendspring.service;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Submission;
import heg.backendspring.entity.TP;
import heg.backendspring.mapping.MapperSubmission;
import heg.backendspring.models.SubmissionDto;
import heg.backendspring.repository.RepositorySubmission;
import heg.backendspring.utils.ZipUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceSubmission {

    private String typeCours = "JAVA";

    //==============================
    //       DEPENDANCES
    //==============================
    private final RepositorySubmission repositorySubmission;
    private final MapperSubmission mapperSubmission;


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
     * Methode permettant de restructurer le rendu d'un fichier zip
     * <p>
     * Logique de restructuration : Chaque dossier dans le zip est un étudiant (le
     * nom du fichier contient les infos de rendu, dont le nom au début) et
     * contient dedans un fichier zip qui est son rendu : ce fichier zip est un
     * projet Java ou python qu'il faut dézipper, et récupérer son contenu pour le
     * stocker dans un nouveau dossier "Nom_Prenom" qui sera lui-même dans un dossier
     * "RenduRestructuration" et qui sera zippé puis stocké au même endroit que le zip
     * d'origine
     * <p>
     * Cas à gérer :
     * — Lister les dossiers à ignorer (dossiers d'environnements, de build, etc...)
     * — Les fichiers MacOS (._) à ignorer
     * — Les fichiers cachés à ignorer
     * <p>
     * - Gérer le cas ou c'est un projet Java ou Python (si java, on prends tout le
     * dossier src, si python, on ne prend que les fichiers .py par exemple)
     * — si java avancé, il faudrait récupérer aussi des fichiers supplémentaires tel
     * que les tests unitaires ou dossiers ressources et pom.xml
     * <p>
     * Une entité TP a les infos de stockage du zip d'origine
     */
    @Transactional
    public void processZipSubmission(Course c, TP tp) throws IOException {
        Submission submission = tp.getSubmission();
        if (submission == null) {
            log.error("Aucune soumission trouvée pour le TP id {} du cours {}", tp.getId(), c.getName());
            return;
        }
        //gérer le type de cours
        if (c.getCourseType() != null) {
            typeCours = String.valueOf(c.getCourseType());
        }

        log.info("Starting restructuring for TP {} of course {} with submission id {}", tp.getNo(), c.getName(), submission.getId());

        // chemin vers le zip d'origine
        Path originalZip = Paths.get(submission.getPathStorage());

        // chemin racine du TP : "DocumentsZips/{CODE_COURS}/TP{no}"
        Path tpRoot = originalZip.getParent();

        // chemin vers le dossier de restructuration
        Path restructurationDir = tpRoot.resolve("RenduRestructuration");
        createFolderIfPossible(restructurationDir);

        // dossier temporaire pour extraire le zip initial => tpmExtract
        Path tpmExtractDir = tpRoot.resolve("tmpExtract");
        createFolderIfPossible(tpmExtractDir);

        // extraire le zip d'origine dans le dossier temporaire
        ZipUtils.unzip(originalZip, tpmExtractDir);

        // Parcourir chaque dossier étudiant
        browseStudentFolders(tpmExtractDir, restructurationDir);

        //Créez un zip global pour le dossier de restructuration
        String nomZipRestructure = "TP" + tp.getNo() + "_RenduRestructuration.zip";
        Path zipRestructure = tpRoot.resolve(nomZipRestructure);
        ZipUtils.zipDirectory(restructurationDir, zipRestructure);

        //Mettre à jour le chemin du zip restructuré
        submission.setPathFileStructured(zipRestructure.toString());
        log.info("Submission path with restructurated data updated: {}", submission.getPathFileStructured());

        //Nettoyer les dossiers temporaires et le dossier de restructuration
        ZipUtils.deleteFolder(tpmExtractDir);
        ZipUtils.deleteFolder(restructurationDir);
    }

    /**
     * Méthode permettant de retourner la liste des étudiants ayant rendu leur TP
     */
    public List<String> getStudentsSubmission(Submission submission) {
        //Récupérer le path du zip restructuré
        Path zipRestructure = Paths.get(submission.getPathFileStructured());
        List<String> etudiants = new ArrayList<>();
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
     * Crée un répertoire s'il n'existe pas déjà.
     */
    private void createFolderIfPossible(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("Could not create directory: {}", dir, e);
        }
    }

    /**
     * Parcourt les dossiers étudiants dans le répertoire temporaire.
     */
    private void browseStudentFolders(Path tpmExtractDir, Path restructurationDir) throws IOException {
        Files.list(tpmExtractDir)
                .filter(Files::isDirectory)
                .forEach(dossierEtudiant -> {
                    try {
                        manageStudentSubmission(dossierEtudiant, restructurationDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Gère le rendu d'un étudiant
     * Doit pouvoir récupérer son nom et prénom pour créer un dossier "Nom_Prenom"
     *
     * @param etudiantDir
     * @param restructurationDir
     */
    private void manageStudentSubmission(Path etudiantDir, Path restructurationDir) throws IOException {
        //Nom du dossier : Nom Prenom_xxxxxxxx (ne pas prendre après le _)
        String nomEtudiant = etudiantDir.getFileName().toString().split("_")[0];
        //Corriger le nom avec un regex pour enlever les caractères spéciaux
        nomEtudiant = nomEtudiant.replaceAll("[^\\p{L}]+", "");

        //Créer le dossier "Nom_Prenom" dans le dossier de restructuration
        Path etudiantDirRestructured = restructurationDir.resolve(nomEtudiant);
        createFolderIfPossible(etudiantDirRestructured);

        //Gérer le contenu du dossier étudiant pour le restructurer
        sendStudentContent(etudiantDir, etudiantDirRestructured);
    }

    /**
     * Transfère le contenu du dossier étudiant vers le dossier de restructuration
     *
     * @param etudiantDir
     * @param etudiantDirRestructured
     */
    private void sendStudentContent(Path etudiantDir, Path etudiantDirRestructured) throws IOException {
        //Normalement : le rendu est un zip, mais dans le cas contraire, on récupère
        // juste ce qu'il y a dedans pour le stocker dans le dossier de restructuration.
        Optional<Path> zipEtudiant = findSubFolderZip(etudiantDir);
        if (zipEtudiant.isPresent()) {
            Path zipEtudiantPath = zipEtudiant.get();

            //Extraire le contenu dans le dossier temporaire local
            Path projetExtract = etudiantDir.resolve("extractedProject");
            createFolderIfPossible(projetExtract);
            //Gestion des différents types de zip avec une méthode de Quarkus
            //OLD VERSION : unzip(zipEtudiantPath, projetExtract);
            manageExtractionZip(zipEtudiantPath, projetExtract, etudiantDir);


            log.debug("Manage extraction for student project and typeCours");
            //Gérer le contenu du projet à copier selon le type de cours
            if (typeCours.equals("JAVA")) {
                ZipUtils.copyJavaProject(projetExtract, etudiantDirRestructured);
            } else if (typeCours.equals("PYTHON")) {
                ZipUtils.copyPythonProject(projetExtract, etudiantDirRestructured);
            } else if (typeCours.equals("JAVA_JEE")) {
                ZipUtils.copyJavaJEEProject(projetExtract, etudiantDirRestructured);
            } else {
                //les deux sets à ignorer sont vides
                ZipUtils.copyAll(projetExtract, etudiantDirRestructured, Set.of(), Set.of());
            }
        } else {
            ZipUtils.copyAll(etudiantDir, etudiantDirRestructured, Set.of(), Set.of());
        }
    }

    /**
     * Trouve un sous-zip (.zip, .7zip ou .rar) dans le dossier d'étudiant, s'il existe
     */
    private Optional<Path> findSubFolderZip(Path dossierEtudiant) {
        try (Stream<Path> files = Files.list(dossierEtudiant)) {
            return files.filter(p -> {
                String fileName = p.toString().toLowerCase();
                return fileName.endsWith(".zip") || fileName.endsWith(".7z") || fileName.endsWith(".rar");
            }).findFirst();
        } catch (IOException e) {
            log.error("Error while searching for sub-zip in {}", dossierEtudiant, e);
        }
        return Optional.empty();
    }

    /**
     * Gère la façon d'extraire le zip selon le type de zip
     */
    private void manageExtractionZip(Path zipEtudiantPath, Path projetExtract, Path etudiantDir) throws IOException {
        String zipEtudiantPathString = zipEtudiantPath.toString();
        if (zipEtudiantPathString.endsWith(".zip")) {
            ZipUtils.unzip(zipEtudiantPath, projetExtract);
        } else if (zipEtudiantPathString.endsWith(".7z")) {
            //Gestion des fichiers 7z
            ZipUtils.extract7z(zipEtudiantPath, projetExtract);
        }
    }


}
