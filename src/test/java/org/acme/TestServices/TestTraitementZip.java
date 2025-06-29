package org.acme.TestServices;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.models.CourseDTO;
import org.acme.models.TP_DTO;
import org.acme.enums.*;
import org.acme.repository.*;
import org.acme.service.*;
import org.acme.service.TPService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@QuarkusTest
public class TestTraitementZip {

    @Inject
    CourseService courseService;
    @Inject
    SubmissionService submissionService;
    @Inject
    CourseRepository repositoryCours;
    @Inject
    TPService TPService;

    @Inject
    TPStatusRepository repositoryTP_Status;
    @Inject
    StudentRepository studentRepository;

    @Inject
    TPRepository repositoryTravailPratique;
    @Inject
    SubmissionRepository submissionRepository;

    @BeforeEach
    @TestTransaction
    void setUp() throws IOException {
        //Gérer le repository en supprimant les données
        Path testZip = Paths.get("src/test/resources/testZips");
        if (Files.exists(testZip)) {
            Files.walk(testZip)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // supprimer d'abord les fichiers
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
        Files.createDirectories(testZip);
        /**
        renduRepository.deleteAll();
        repositoryTravailPratique.deleteAll();
        etudiantRepository.deleteAll();
        repositoryCours.deleteAll();
         */
    }


    @Test
    @TestTransaction
    public void testerTraitementZip() throws IOException {
        //Creation des entités
        CourseDTO cours = new CourseDTO(null,
                "Approfondissement de la programmation",
                "62-21", SemesterType.PRINTEMPS, 2027, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.addCourse(cours);

        TP_DTO tpDTP = courseService.addTP(courseDTO, 1);

        //Creer l'input pour le zip
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        //Ajout d'un rendu
        Course c = repositoryCours.findById(courseDTO.getId());
        TP tp = repositoryCours.findTpByNo(c.id, 1);

        TPService.addSubmissionToTP(tpDTP, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        courseService.startZipProcess(c.id, tp.id);

        //Vérifier que le rendu a bien été traité
        Submission submission = tp.submission;
        Assertions.assertNotNull(submission);
        String cheminVersZip = submission.pathStorage;
        String cheminVersZipRestructure = submission.pathFileStructured;
        // Normaliser les chemins pour éviter les problèmes de séparateurs ('/' vs '\\')
        Path expectedPath1 = Paths.get("src/test/resources/testZips/62-21/TP1/TP1_RenduCyberlearn.zip");
        Path expectedPath2 = Paths.get("src/test/resources/testZips/62-21/TP1/TP1_RenduRestructuration.zip");

        Path actualPath1 = Paths.get(cheminVersZip);
        Path actualPath2 = Paths.get(cheminVersZipRestructure);

        // Comparaison normalisée des chemins
        Assertions.assertTrue(expectedPath1.equals(actualPath1),
                "Le chemin du ZIP ne correspond pas : " + actualPath1);
        Assertions.assertTrue(expectedPath2.equals(actualPath2),
                "Le chemin du ZIP restructuré ne correspond pas : " + actualPath2);
    }

    @Test
    @TestTransaction
    public void testerContenuZip() throws IOException {
        //Creation des entités

        CourseDTO courseDTO = courseService.addCourse(new CourseDTO(null,
                "Python Introduction",
                "61-13", SemesterType.AUTOMNE, 2025, "Stettler", CourseType.Python, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        TP_DTO tpDTO = courseService.addTP(courseDTO, 1);

        //Creer l'input pour le zip
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);
        //Ajout d'un rendu
        Course c = repositoryCours.findCoursByCode("61-13");
        TP tp = repositoryCours.findTpByNo(c.id, 1);
        TPService.addSubmissionToTP(tpDTO, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        courseService.startZipProcess(c.id, tp.id);

        //tests
        Submission submission = tp.submission;
        Assertions.assertNotNull(submission);

        //Vérifier si le rendu de george dylan ne contient que des fichiers .py
        Path pathRendu = Paths.get(submission.pathFileStructured);
        Assertions.assertTrue(Files.exists(pathRendu));

        Path etudiantPath = pathRendu.resolve("georgedylan");
        // Vérifier le contenu du ZIP sans extraction
        try (ZipFile zipFile = new ZipFile(pathRendu.toFile())) {
            boolean hasStudentFolder = false;
            boolean containsOtherThanPy = false;
            boolean containsSrcFolder = false;

            for (ZipEntry entry : zipFile.stream().toList()) {
                String entryName = entry.getName();

                // Vérifier si le dossier de l'étudiant existe
                if (entryName.startsWith("georgedylan/")) {
                    hasStudentFolder = true;
                }

                // Vérifier que tous les fichiers sous 'georgedylan/' sont bien des .py
                if (entryName.startsWith("georgedylan/") && !entry.isDirectory()) {
                    if (!entryName.endsWith(".py")) {
                        containsOtherThanPy = true;
                    }
                }

                //Vérification pour le contenu d'un projet java : tester si un dossier src existe
                if (entryName.startsWith("scoutmark/") && entry.isDirectory() && entryName.endsWith("/src/")) {
                    containsSrcFolder = true;
                }
            }

            Assertions.assertTrue(hasStudentFolder, "Le dossier de l'étudiant georgedylan doit exister dans le ZIP.");
            Assertions.assertFalse(containsOtherThanPy, "Tous les fichiers dans 'georgedylan/' doivent être des .py, y compris dans les sous-dossiers.");
            Assertions.assertTrue(containsSrcFolder, "Le dossier 'src' doit exister dans le dossier 'scoutmark'.");
        }

        /**
        try (Stream<Path> files = Files.walk(etudiantPath)) {
            boolean containsOnlyPy = files
                    .filter(Files::isRegularFile)
                    .allMatch(file -> file.toString().endsWith(".py"));
            Assertions.assertTrue(containsOnlyPy, "Le rendu doit contenir uniquement des fichiers .py");

        }*/
    }

    /**
     * Tester la mise à jour des status des étudiants pour voir qui a fait et qui a pas fait
     */
    @Test
    @TestTransaction
    public void testStatusUpdateEtudiants() throws IOException {

        //Créer les données de test
        CourseDTO cours = new CourseDTO(null,
                "Approfondissement de la programmation",
                "62-23", SemesterType.PRINTEMPS, 2028, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.addCourse(cours);

        TP_DTO tpDTO = courseService.addTP(courseDTO, 1);

        String[] etudiants = {
                "Scout Mark;Test3mark@hesge.ch;temps_plein",
                "Riggs Helly;Test3helly@hesge.ch;temps_partiel",
                "George Dylan;Test3dylan@hesge.ch;temps_plein",
                "Bailiff Irving;Test3irving@hesge.ch;temps_plein",
        };
        courseService.addAllStudentsFromFile(courseDTO, etudiants);

        //Utilisation d'un zip pour ajouter un rendu
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        //Ajout d'un rendu
        Course c = repositoryCours.findById(courseDTO.getId());
        TP tp = repositoryCours.findTpByNo(c.id, 1);
        TPService.addSubmissionToTP(tpDTO, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        courseService.startZipProcess(c.id, tp.id);

        //tests
        Submission submission = tp.submission;
        Assertions.assertNotNull(submission);

        //Lancement de la methode pour traiter les TP_Status avec gestionRendusTP dans serviceTravailPratique
        tp = TPService.manageSubmissionsTP(c, tp, c.studentList);
        //repositoryTP_Status.flush();
        //première vérification : tester si les 4 étudiants ont bien un TP_Status
        Set<TPStatus> tpStatusList = tp.statusStudents;
        Assertions.assertEquals(4, tpStatusList.size());

        //Parcours de la liste : normalement 3 étudiants sur 4 ont rendu, Irvin n'a pas rendu
        for (TPStatus tpStatus : tpStatusList) {
            if (tpStatus.student.name.equals("Bailiff Irving")) {
                Assertions.assertFalse(tpStatus.studentSubmission);
            }
            else{
                Assertions.assertTrue(tpStatus.studentSubmission);
            }
        }
    }


}
