package org.acme.TestServices;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.entity.Course;
import org.acme.entity.Student;
import org.acme.entity.TP;
import org.acme.enums.CourseType;
import org.acme.enums.SemesterType;
import org.acme.enums.StudyType;
import org.acme.models.*;
import org.acme.repository.CourseRepository;
import org.acme.repository.StudentRepository;
import org.acme.repository.TPRepository;
import org.acme.service.CourseService;
import org.acme.service.StudentService;
import org.acme.service.TPService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TestServicesV2 {

    @Inject
    CourseService courseService;
    @Inject
    TPService tpService;
    @Inject
    StudentService studentService;
    @Inject
    CourseRepository courseRepository;
    @Inject
    TPRepository tpRepository;
    @Inject
    StudentRepository studentRepository;

    /**
     * Chaque test est isolé : on utilise un code unique pour le cours.
     */
    private String uniqueCode() {
        return "CODE-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Exemple d'utilisation de @TempDir :
     * JUnit crée un dossier temporaire unique pour chaque test.
     * Il est supprimé automatiquement après.
     */
    @Test
    @TestTransaction
    void testCreateFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.txt");
        List<String> lines = List.of("howtodoinjava.com");

        Files.write(tempFile, lines);
        assertTrue(Files.exists(tempFile), "Temp File should have been created");
        assertEquals(lines, Files.readAllLines(tempFile));
    }

    /**
     * Test de création d'un cours, sans interférer avec le setUp.
     * On utilise @TestTransaction pour valider les changements.
     */
    @Test
    @TestTransaction
    public void testCreationCours() {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Programmation collaborative",
                code,
                SemesterType.AUTOMNE,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);

        // Vérifier le DTO
        assertNotNull(courseDTO);
        //assertEquals(1, courseRepository.count());
        Course c = courseRepository.findCoursByCode(code);
        assertEquals(code, c.code);
        assertEquals("Programmation collaborative", c.name);
    }

    /**
     * Test ajout d'étudiants à un cours existant.
     */
    @Test
    @TestTransaction
    public void testAjoutEtudiants() {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Programmation collaborative",
                code,
                SemesterType.AUTOMNE,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);
        Course c = courseRepository.findCoursByCode(code);

        // Créer un étudiant en base
        String email = "mark-" + UUID.randomUUID().toString().substring(0,6) + "@hesge.ch";
        StudentDTO eMark = new StudentDTO(null, "Scout Mark", email, StudyType.temps_plein, new ArrayList<>());
        StudentDTO eMarkDTO = studentService.addStudent(eMark, c);

        // Ajouter Mark au cours
        StudentDTO etuAdded = courseService.addStudentToCourse(courseDTO, eMarkDTO);

        assertEquals(1, c.studentList.size());
        assertNotNull(etuAdded);

        // Vérifie la relation inverse
        assertEquals(1, studentRepository.findByEmail(email).courseStudentList.size());
        assertEquals(code, studentRepository.findByEmail(email).courseStudentList.getFirst().code);

    }

    @Test
    @TestTransaction
    /**
     * Test de l'ajout d'etudiant à plusieurs cours via addEtudiantToCourses
     */
    public void testAjoutStudents() {
        //Ajouter mark à un autre cours
        String code = uniqueCode();
        CourseDTO cours1 = new CourseDTO(
                null,
                "Prog 1",
                code,
                SemesterType.AUTOMNE,
                2026,
                "Test",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO1 = courseService.addCourse(cours1);

        //Créer un étudiant en base
        // Créer un étudiant en base
        String email = "mark-" + UUID.randomUUID().toString().substring(0,6) + "@hesge.ch";
        StudentDTO eMark = new StudentDTO(null, "Scout Mark", email, StudyType.temps_plein, new ArrayList<>());
        StudentDTO etuAdded1 = courseService.addStudentToCourse(courseDTO1, eMark);

        //tester le cours de mark
        assertNotNull(etuAdded1);
        assertNotNull(studentRepository.findByEmail(email));
        //verifier le nombre de cours de Mark
        Student etu = studentRepository.findByEmail(etuAdded1.getEmail());
        System.out.println("Cours de Mark : " + etu.courseStudentList);
        assertEquals(1, etu.courseStudentList.size());

        //Creer un autre cours
        String code2 = uniqueCode();
        CourseDTO cours2 = new CourseDTO(
                null,
                "Prog 2",
                code2,
                SemesterType.AUTOMNE,
                2026,
                "Test",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO2 = courseService.addCourse(cours2);
        StudentDTO etuAdded2 = courseService.addStudentToCourse(courseDTO2, eMark);

        //tester les cours de mark
        assertNotNull(etuAdded2);
        //verifier le nombre de cours de Mark
        assertEquals(2, studentRepository.findByEmail(email).courseStudentList.size());
    }

    /**
     * Test de l'ajout d'étudiants à partir d'un fichier .txt.
     */
    @Test
    @TestTransaction
    public void testAjoutEtudiantAvecTxt() {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Approfondissement de la programmation",
                code,
                SemesterType.PRINTEMPS,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);
        Course c = courseRepository.findCoursByCode(code);

        // Ajout de quelques étudiants via un pseudo-fichier .txt
        String[] data = {
                "Walter White;w-" + UUID.randomUUID().toString().substring(0,6) + "@hesge.ch;temps_plein",
                "Jesse Pinkman;j-" + UUID.randomUUID().toString().substring(0,6) + "@hesge.ch;temps_partiel"
        };
        courseService.addAllStudentsFromFile(courseDTO, data);

        // Vérifications
        assertNotNull(studentRepository.findByEmail(data[0].split(";")[1]));
        assertNotNull(studentRepository.findByEmail(data[1].split(";")[1]));
        assertEquals(2, c.studentList.size());
    }

    /**
     * Test d'ajout d'un TP.
     */
    @Test
    @TestTransaction
    public void testAjoutTP() {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Approfondissement de la programmation",
                code,
                SemesterType.PRINTEMPS,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);

        // Ajouter un TP
        TP_DTO tpDTO = courseService.addTP(courseDTO, 2);

        // Vérifier la méthode avec le DTO
        assertNotNull(tpDTO);
        assertEquals(code, tpRepository.findById(tpDTO.getId()).course.code);

        // Vérification côté entité Course
        Course c = courseRepository.findCoursByCode(code);
        assertEquals(1, c.tpsList.size());

        // Tester existence du TP
        TP tp = courseRepository.findTpByNo(c.id, 2);
        assertNotNull(tp);
        assertEquals(2, tp.no);
    }

    /**
     * Test d'ajout d'évaluations dans un cours existant.
     */
    @Test
    @TestTransaction
    public void testAjoutEvaluations() {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Approfondissement de la programmation",
                code,
                SemesterType.PRINTEMPS,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);
        Course c = courseRepository.findCoursByCode(code);

        // Ajout d'un examen et d'un CC
        courseService.addExam(courseDTO,
                new ExamDTO(null, "Examen final", LocalDateTime.now(), courseDTO, null, courseDTO.getSemester())
        );
        courseService.addCC(courseDTO,
                new ContinuousAssessmentDTO(null, "CC1", LocalDateTime.now(), courseDTO, null, 2, 1)
        );

        // Vérifier
        assertEquals(2, c.evaluations.size());
        assertEquals("Examen final", c.evaluations.get(0).name);
        assertEquals("CC1", c.evaluations.get(1).name);
    }

    /**
     * Test de création de dossiers (TP, Examen) et vérification de leur existence.
     */
    @Test
    @TestTransaction
    public void testCreationDesDossiers() throws IOException {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Programmation collaborative",
                code,
                SemesterType.AUTOMNE,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);
        Course c = courseRepository.findCoursByCode(code);

        // Créer TP et Examen
        courseService.addTP(courseDTO, 1);
        courseService.addExam(courseDTO,
                new ExamDTO(null, "Examen final", LocalDateTime.now(), courseDTO, null, courseDTO.getSemester())
        );

        // Vérification des dossiers
        Path tpDir = Paths.get("src/test/resources/testZips", code, "TP1");
        assertTrue(Files.exists(tpDir), "Le dossier TP1 devrait exister !");

        Path examDir = Paths.get("src/test/resources/testZips", code, "Examen final");
        assertTrue(Files.exists(examDir), "Le dossier Examen final devrait exister !");
    }

    /**
     * Test ajout de rendu d'un TP (copie de fichier ZIP) et vérification.
     */
    @Test
    @TestTransaction
    public void testRenduTp() throws IOException {
        String code = uniqueCode();
        CourseDTO cours = new CourseDTO(
                null,
                "Programmation collaborative",
                code,
                SemesterType.AUTOMNE,
                2025,
                "Stettler",
                CourseType.JAVA,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        CourseDTO courseDTO = courseService.addCourse(cours);
        TP_DTO tp01 = courseService.addTP(courseDTO, 1);

        Path zipPath = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        try (InputStream in = Files.newInputStream(zipPath)) {
            TP_DTO result = tpService.addSubmissionToTP(tp01, in);
            assertNotNull(result.getSubmission());
            assertEquals("TP1_RenduCyberlearn.zip", result.getSubmission().getFileName());

            Path tpZipPath = Paths.get("src/test/resources/testZips", code, "TP1", "TP1_RenduCyberlearn.zip");
            assertTrue(Files.exists(tpZipPath), "Le rendu du TP devrait exister !");
        }
    }
}
