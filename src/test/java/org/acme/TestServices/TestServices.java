package org.acme.TestServices;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.enums.*;
import org.acme.models.*;
import org.acme.repository.*;
import org.acme.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class TestServices {

    @Inject
    CourseRepository courseRepository;
    @Inject
    TPRepository travailPratiqueRepository;
    @Inject
    StudentRepository studentRepository;
    @Inject
    CourseService courseService;
    @Inject
    TPService TPService;
    @Inject
    StudentService studentService;



    /**
     * Nettoyage de la base avant chaque test.
     * On ne recrée pas ici d'entités par défaut
     * pour que chaque test reste indépendant.
     */
    @BeforeEach
    @Transactional
    void cleanDatabase() {
        travailPratiqueRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    /**
     * Vérifie qu'on peut créer un cours et un TP,
     * ainsi que 4 étudiants, puis vérifier leur bonne insertion.
     * // Ajouter des étudiants
     *         Etudiant e1 = new Etudiant("Scout Mark", "mark@hesge.ch", TypeEtude.temps_plein);
     *         Etudiant e2 = new Etudiant("Riggs Helly", "helly@hesge.ch", TypeEtude.temps_partiel);
     *         Etudiant e3 = new Etudiant("George Dylan", "dylan@hesge.ch", TypeEtude.temps_plein);
     *         Etudiant e4 = new Etudiant("Bailiff Irving", "irving@hesge.ch", TypeEtude.temps_plein);
     */


    /**
     * Exemple d'utilisation de @TempDir :
     * JUnit crée un dossier temporaire unique pour chaque test.
     * Il est supprimé automatiquement après.
     */
    @Test
    void testCreateFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.txt");
        List<String> lines = Arrays.asList("howtodoinjava.com");

        Files.write(tempFile, lines);
        Assertions.assertTrue(Files.exists(tempFile), "Temp File should have been created");
        Assertions.assertEquals(lines, Files.readAllLines(tempFile));
    }

    /**
     * Test de création d'un cours, sans interférer avec le setUp.
     * On utilise @TestTransaction pour valider les changements.
     */
    @Test
    @TestTransaction
    public void testCreationCours() {
        CourseDTO cours = new CourseDTO(null,
                "Programmation collaborative",
                "63-21", SemesterType.AUTOMNE, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);

        //vérifier le DTO
        Assertions.assertNotNull(courseDTO);

        Assertions.assertEquals(1, courseRepository.count());
        Course c = courseRepository.findCoursByCode("63-21");
        Assertions.assertEquals("63-21", c.code);
        Assertions.assertEquals("Programmation collaborative", c.name);
    }

    /**
     * Test ajout d'étudiants à un cours existant.
     */
    @Test
    @Transactional
    public void testAjoutEtudiants() {
        // Créer un cours
        CourseDTO cours = new CourseDTO(null,
                "Programmation collaborative",
                "63-21", SemesterType.AUTOMNE, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);

        Course c = courseRepository.findCoursByCode("63-21");

        // Créer un étudiant en base
        StudentDTO eMark = new StudentDTO(null, "Scout Mark", "mark@hesge.ch", StudyType.temps_plein, new ArrayList<>());
        StudentDTO eMarkDTO = studentService.addEtudiant(eMark, c);

        // Ajouter Mark au cours 63-21
        StudentDTO etuAdded = courseService.ajouterEtudiant(courseDTO, eMarkDTO);


        Assertions.assertEquals(1, c.studentList.size());
        Assertions.assertNotNull(etuAdded);

        // On vérifie qu'il est aussi lié côté Etudiant
        Assertions.assertEquals(1, studentRepository.findByEmail(etuAdded.getEmail()).courseStudentList.size());
        Assertions.assertEquals("63-21", studentRepository.findByEmail(etuAdded.getEmail()).courseStudentList.getFirst().code);

        // Test d'un ID inexistant
        //Assertions.assertThrows(Exception.class, () -> serviceCours.ajouterEtudiant(new CoursDTO(), new EtudiantDTO()));
    }

    /**
     * Test de l'ajout d'étudiants à partir d'un fichier .txt.
     */
    @Test
    @Transactional
    public void testAjoutEtudiantAvecTxt() {
        // Créer un cours
        CourseDTO cours = new CourseDTO(null,
                "Approfondissement de la programmation",
                "62-21", SemesterType.PRINTEMPS, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);
        Course c = courseRepository.findCoursByCode("62-21");
        // Ajout de quelques étudiants via un pseudo-fichier .txt
        String[] data = {
                "Walter White;w@hesge.ch;temps_plein",
                "Jesse Pinkman;jesse@hesge.ch;temps_partiel"
        };
        courseService.addAllStudentsFromFile(courseDTO, data);

        // Vérifications
        Assertions.assertNotNull(studentRepository.findByEmail("w@hesge.ch"));
        Assertions.assertNotNull(studentRepository.findByEmail("jesse@hesge.ch"));
        Assertions.assertEquals(2, c.studentList.size());
    }

    /**
     * Test d'ajout d'un TP.
     */
    @Test
    @Transactional
    public void testAjoutTP() {
        // Créer un cours
        CourseDTO cours = new CourseDTO(null,
                "Approfondissement de la programmation",
                "62-21", SemesterType.PRINTEMPS, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);


        // Ajouter un TP
        TP_DTO tpDTO = courseService.ajouterTP(courseDTO, 2);

        // Vérifier la méthode d'ajout du TP avec le DTO
        Assertions.assertNotNull(tpDTO);
        Assertions.assertEquals(courseDTO.getCode(), travailPratiqueRepository.findById(tpDTO.getId()).course.code);

        //Vérification du côté de l'entité cours
        Course c = courseRepository.findCoursByCode("62-21");
        Assertions.assertEquals(1, c.tpsList.size());

        // Tester si le TP existe bien
        TP tp = courseRepository.findTpByNo(c.id, 2);
        Assertions.assertNotNull(tp);
        Assertions.assertEquals(2, tp.no);
    }

    /**
     * Test d'ajout d'évaluations dans un cours existant.
     */
    @Test
    @Transactional
    public void testAjoutEvaluations() {
        // Créer un cours
        CourseDTO cours = new CourseDTO(null,
                "Approfondissement de la programmation",
                "62-21", SemesterType.PRINTEMPS, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);
        Course c = courseRepository.findCoursByCode("62-21");

        // Ajout d'un examen et d'un CC
        ExamDTO examDTO = courseService.ajouterExamen(courseDTO, new ExamDTO(null, "Examen final", LocalDateTime.now(), courseDTO, null, courseDTO.getSemester()));
        ContinuousAssessmentDTO ccDTO = courseService.addCC(courseDTO, new ContinuousAssessmentDTO(null, "CC1", LocalDateTime.now(), courseDTO, null, 2, 1));

        //Tester les DTO :
        Assertions.assertNotNull(examDTO);
        Assertions.assertNotNull(ccDTO);


        // Vérifier
        Assertions.assertEquals(2, c.evaluations.size());
        Assertions.assertEquals("Examen final", c.evaluations.get(0).name);
        Assertions.assertEquals("CC1", c.evaluations.get(1).name);
    }

    /**
     * Test de création de dossiers (TP, Examen) et vérification de leur existence.
     */
    @Test
    @TestTransaction
    public void testCreationDesDossiers() throws IOException {
        // Nettoyage du répertoire s'il existe
        Path dossierTest = Paths.get("src/test/resources/testZips/63-21");
        if (Files.exists(dossierTest)) {
            Files.walk(dossierTest)
                    .sorted((p1, p2) -> p2.compareTo(p1))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }

        // Créer un cours
        CourseDTO cours = new CourseDTO(null,
                "Programmation collaborative",
                "63-21", SemesterType.AUTOMNE, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);
        Course c = courseRepository.findCoursByCode("63-21");

        // Créer un TP et un examen
        courseService.ajouterTP(courseDTO, 1);
        courseService.ajouterExamen(courseDTO, new ExamDTO(null, "Examen final", LocalDateTime.now(), courseDTO, null, courseDTO.getSemester()));

        // Vérifier l'existence des dossiers
        Path tpDir = Paths.get("src/test/resources/testZips", c.code, "TP1");
        Assertions.assertTrue(Files.exists(tpDir), "Le dossier TP1 devrait exister !");

        Path examDir = Paths.get("src/test/resources/testZips", c.code, "Examen final");
        Assertions.assertTrue(Files.exists(examDir), "Le dossier Examen final devrait exister !");
    }

    /**
     * Test ajout de rendu d'un TP (copie de fichier ZIP) et vérification.
     */
    @Test
    @TestTransaction
    public void testRenduTp() throws IOException {
        // Créer un cours et un TP
        CourseDTO cours = new CourseDTO(null,
                "Programmation collaborative",
                "63-21", SemesterType.AUTOMNE, 2025, "Stettler", CourseType.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CourseDTO courseDTO = courseService.creerCours(cours);
        Course c = courseRepository.findCoursByCode("63-21");

        TP_DTO tp01DTO = courseService.ajouterTP(courseDTO, 1);

        //TravailPratique tp01 = coursRepository.findTpByNo(c.id, 1);

        // Préparer un rendu (ZIP)
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        // Créer le rendu du TP via le service
        TP_DTO tp01_dto = TPService.creerRenduTP(tp01DTO, inputStream);

        // Vérifier
        Assertions.assertNotNull(tp01_dto.getSubmission());
        Assertions.assertEquals("TP1_RenduCyberlearn.zip",tp01_dto.getSubmission().getFileName());

        Path tpZipPath = Paths.get("src/test/resources/testZips", c.code, "TP1", "TP1_RenduCyberlearn.zip");
        Assertions.assertTrue(Files.exists(tpZipPath), "Le dossier TP1 devrait exister !");
    }
}
