package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.*;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.quarkus.fs.util.ZipUtils.*;

@ApplicationScoped
public class SubmissionService {

    private String typeCours = "Java";
    private static final Logger LOG = Logger.getLogger(SubmissionService.class);


    /**
     * Methode permettant de restructurer le rendu d'un fichier zip
     *
     * Logique de restructuration : Chaque dossier dans le zip est un étudiant (le
     * nom du fichier contient les infos de rendu, dont le nom au début) et
     * contient dedans un fichier zip qui est son rendu : ce fichier zip est un
     * projet Java ou python qu'il faut dézipper, et récupérer son contenu pour le
     * stocker dans un nouveau dossier "Nom_Prenom" qui sera lui même dans un dossier
     * "RenduRestructuration" et qui sera zippé puis stocké au même endroit que le zip
     * d'origine
     *
     * Cas à gérer :
     * - Lister les dossiers à ignorer (dossiers d'environnements, de build, etc...)
     * - les fichiers MacOS (._) à ignorer
     * - les fichiers cachés à ignorer
     *
     * - Gérer le cas ou c'est un projet Java ou Python (si java, on prends tout le
     * dossier src, si python, on ne prend que les fichiers .py par exemple)
     * - si java avancé, il faudrait récupérer aussi des fichiers supplémentaires tel
     * que les tests unitaires ou dossiers ressources et pom.xml
     *
     * Une entité Rendu a les infos de stockage du zip d'origine
     */
    public void traitementRenduZip(Course c, TP tp) throws IOException {
        //TODO
        Submission submission = tp.submission;
        if (submission == null) {
            LOG.warn("No submission found for TP: " + tp.no);
            return;
        }

        //Gérer le type de cours
        if (c.courseType != null) {
            typeCours = String.valueOf(c.courseType);
        }

        LOG.debug("Starting restructuring for TP: " + tp.no + " of course: " + c.code);
        // chemin vers le zip d'origine
        Path originalZip = Paths.get(submission.pathStorage);

        // chemin racine du TP : "DocumentsZips/{CODE_COURS}/TP{no}"
        Path tpRoot = originalZip.getParent();

        // chemin vers le dossier de restructuration
        Path restructurationDir = tpRoot.resolve("RenduRestructuration");
        creerRepertoireSilExistePas(restructurationDir);

        // dossier temporaire pour extraire le zip initial => tpmExtract
        Path tpmExtractDir = tpRoot.resolve("tmpExtract");
        creerRepertoireSilExistePas(tpmExtractDir);

        // extraire le zip d'origine dans le dossier temporaire
        unzip(originalZip, tpmExtractDir);

        // Parcours de chaque dossier étudiant
        Files.list(tpmExtractDir)
                .filter(Files::isDirectory)
                .forEach(dossierEtudiant -> {
                    try {
                        gererRenduEtudiant(dossierEtudiant, restructurationDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        //Créez un zip global pour le dossier de restructuration
        String nomZipRestructure = "TP" + tp.no + "_RenduRestructuration.zip";
        Path zipRestructure = tpRoot.resolve(nomZipRestructure);
        zip(restructurationDir, zipRestructure);

        //Mettre à jour le chemin du zip restructuré
        submission.pathFileRestructurated = zipRestructure.toString();
        LOG.info("Submission path with restructurated data updated: " + submission.pathFileRestructurated);
        //Nettoyer les dossiers temporaires et le dossier de restructuration
        supprimerRepertoire(tpmExtractDir);
        supprimerRepertoire(restructurationDir);

    }


    /**
     * Méthode permettant de retourner la liste des étudiants ayant rendu leur TP
     */
    public List<String> getListRendus(Submission submission) {
        //Récupérer le path du zip restructuré
        Path zipRestructure = Paths.get(submission.pathFileRestructurated);
        List<String> etudiants = new ArrayList<>();
        try {
            //Extraire le zip
            Path extractDir = Files.createTempDirectory("extracted");
            unzip(zipRestructure, extractDir);

            //Récupérer la liste des dossiers étudiants
            Files.list(extractDir)
                    .filter(Files::isDirectory)
                    .forEach(etudiantDir -> {
                        String nomEtudiant = etudiantDir.getFileName().toString();
                        etudiants.add(nomEtudiant);
                    });

            //Supprimer le dossier temporaire
            supprimerRepertoire(extractDir);
        } catch (IOException e) {
            LOG.error("Error while retrieving the list of students", e);
        }

        return etudiants;
    }


    // --------------------------------------------------------------------------
    // Méthodes privées "utilitaires"
    // --------------------------------------------------------------------------

    /**
     * Crée un répertoire s'il n'existe pas déjà.
     */
    private void creerRepertoireSilExistePas(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOG.error("Could not create directory: " + dir, e);
            //throw new RuntimeException("Impossible de créer le dossier : " + dir, e);
        }
    }

    /**
     * Gère le rendu d'un étudiant
     * Doit pouvoir récupérer son nom et prénom pour créer un dossier "Nom_Prenom"
     *
     * @param etudiantDir
     * @param restructurationDir
     */
    private void gererRenduEtudiant(Path etudiantDir, Path restructurationDir) throws IOException {
        //Nom du dossier : Nom Prenom_xxxxxxxx (ne pas prendre après le _)
        String nomEtudiant = etudiantDir.getFileName().toString().split("_")[0];
        //Corriger le nom avec un regex pour enlever les caractères spéciaux
        nomEtudiant = nomEtudiant.replaceAll("[^\\p{L}]+", "");

        //Créer le dossier "Nom_Prenom" dans le dossier de restructuration
        Path etudiantDirRestructured = restructurationDir.resolve(nomEtudiant);
        creerRepertoireSilExistePas(etudiantDirRestructured);

        //Gérer le contenu du dossier étudiant pour le restructurer
        transfererContenuEtudiant(etudiantDir, etudiantDirRestructured);

    }

    /**
     * Transfère le contenu du dossier étudiant vers le dossier de restructuration
     *
     * @param etudiantDir
     * @param etudiantDirRestructured
     */
    private void transfererContenuEtudiant(Path etudiantDir, Path etudiantDirRestructured) throws IOException {
        //Normalement : le rendu est un zip, mais dans le cas contraire, on récupère
        // juste ce qu'il y a dedans pour le stocker dans le dossier de restructuration
        Optional<Path> zipEtudiant = trouverSousZip(etudiantDir);
        if (zipEtudiant.isPresent()) {
            Path zipEtudiantPath = zipEtudiant.get();

            //Extraire le contenu dans le dossier temporaire local
            Path projetExtract = etudiantDir.resolve("extractedProject");
            creerRepertoireSilExistePas(projetExtract);
            //Gestion des différents types de zip avec une méthode de Quarkus
            //OLD VERSION : unzip(zipEtudiantPath, projetExtract);
            manageExtractionZip(zipEtudiantPath, projetExtract, etudiantDir);


            LOG.debug("Manage extraction for student project and typeCours");
            //Gérer le contenu du projet à copier selon le type de cours
            if (typeCours.equals("Java")) {
                copierProjetJava(projetExtract, etudiantDirRestructured);
            } else if (typeCours.equals("Python")) {
                copierProjetPython(projetExtract, etudiantDirRestructured);
            } else {
                //les deux set à ignorer sont vides
                copierTout(projetExtract, etudiantDirRestructured, Set.of(), Set.of());
            }
        } else {
            copierTout(etudiantDir, etudiantDirRestructured, Set.of(), Set.of());
        }


    }

    /**
     * Trouve un sous-zip (.zip, .7zip ou .rar) dans le dossier d'étudiant, s'il existe
     * .
     */
    private Optional<Path> trouverSousZip(Path dossierEtudiant) {
        try (Stream<Path> files = Files.list(dossierEtudiant)) {
            return files.filter(p -> {
                String fileName = p.toString().toLowerCase();
                return fileName.endsWith(".zip") || fileName.endsWith(".7z") || fileName.endsWith(".rar");
            }).findFirst();
        } catch (IOException e) {
            LOG.error("Error while searching for sub-zip in " + dossierEtudiant, e);
        }
        return Optional.empty();
    }

    /**
     * Gère la façon d'extraire le zip selon le type de zip
     */
    private void manageExtractionZip(Path zipEtudiantPath, Path projetExtract, Path etudiantDir) throws IOException {
        String zipEtudiantPathString = zipEtudiantPath.toString();
        if (zipEtudiantPathString.endsWith(".zip")) {
            unzip(zipEtudiantPath, projetExtract);
        } else if (zipEtudiantPathString.endsWith(".7z")) {
            //Gestion des fichiers 7z
            extract7z(zipEtudiantPath, projetExtract);
        }
    }

    /**
     * Extrait un fichier 7z dans un dossier
     */
    private void extract7z(Path zipFile, Path outputDir) {
        try (SevenZFile sevenZFile = new SevenZFile(zipFile.toFile())) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                Path outputFile = outputDir.resolve(entry.getName());
                Files.createDirectories(outputFile.getParent());

                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content);
                Files.write(outputFile, content);

                LOG.info("Extracted: " + outputFile);
            }
        } catch (IOException e) {
            LOG.error("Error while extracting 7z file", e);
        }
    }





    /**
     * Copie un projet Java (on filtre .git, .idea, target, etc.).
     */
    private void copierProjetJava(Path sourceDir, Path targetDir) {
        // On peut copier tout en ignorant des dossiers courants : .git, .idea, target, build, etc.
        Set<String> ignoreDirs = Set.of(".git", ".idea", "target", "build", "out");
        Set<String> ignoreFiles = Set.of(".DS_Store", "Thumbs.db", "desktop.ini",
                ".iml", ".pdf", ".docx", ".txt");
        copierTout(sourceDir, targetDir, ignoreDirs, ignoreFiles);
    }

    /**
     * Copie un projet Python (on filtre .git, .idea, etc.).
     */
    private void copierProjetPython(Path sourceDir, Path targetDir) {
        // On peut ignorer .git, .idea, venv, etc.
        //copierTout(sourceDir, targetDir, true);
        Set<String> ignoreDirs = Set.of(".git", ".idea", "venv", "__pycache__");
        Set<String> ignoreFiles = Set.of(".DS_Store", "Thumbs.db", "desktop.ini",
                ".iml", ".pdf", ".docx", ".txt");
        copierTout(sourceDir, targetDir, ignoreDirs, ignoreFiles);
    }

    /**
     * Copie récursivement les fichiers d'un projet tout en filtrant certains fichiers/dossiers.
     *
     * @param sourceDir   Dossier source.
     * @param targetDir   Dossier de destination.
     * @param ignoreDirs  Liste des dossiers à exclure.
     * @param ignoreFiles Liste des fichiers (ou extensions) à exclure.
     */
    private void copierTout(Path sourceDir, Path targetDir, Set<String> ignoreDirs, Set<String> ignoreFiles) {
        try (Stream<Path> stream = Files.walk(sourceDir)) {  // 1️⃣
            stream
                    .filter(path -> !path.equals(sourceDir)) // 2️⃣ Ignorer le dossier source lui-même
                    .filter(path -> {
                        String filename = path.getFileName().toString();

                        // 3️⃣ Si c'est un dossier et qu'il est dans ignoreDirs, on l'ignore complètement
                        if (Files.isDirectory(path) && ignoreDirs.contains(filename)) {
                            return false;
                        }

                        // 4️⃣ Si c'est un fichier et qu'il correspond à un nom ou une extension dans ignoreFiles, on l'ignore
                        return ignoreFiles.stream().noneMatch(filename::endsWith);
                    })
                    .forEach(path -> { // 5️⃣ Copier les fichiers/dossiers restants
                        Path destPath = targetDir.resolve(sourceDir.relativize(path));

                        try {
                            if (Files.isDirectory(path)) {
                                Files.createDirectories(destPath); // 6️⃣ Créer le dossier cible si nécessaire
                            } else {
                                Files.createDirectories(destPath.getParent()); // 7️⃣ Assurer que le dossier parent existe
                                Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING); // 8️⃣ Copier le fichier
                            }
                        } catch (IOException e) {
                            LOG.error("Error while copying " + path + " to " + destPath, e);
                        }
                    });
        } catch (IOException e) {
            LOG.error("Error while traversing " + sourceDir, e);

        }
    }

    /**
     * Supprime récursivement un répertoire.
     */
    private void supprimerRepertoire(Path dir) {
        if (!Files.exists(dir)) {
            return; // rien à faire
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc)
                        throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.error("Directory deletion failed: " + dir, e);
        }
    }


}
