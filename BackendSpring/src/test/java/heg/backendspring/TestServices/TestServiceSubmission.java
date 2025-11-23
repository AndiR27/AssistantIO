package heg.backendspring.TestServices;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Submission;
import heg.backendspring.entity.TP;
import heg.backendspring.enums.CourseType;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.models.SubmissionDto;
import heg.backendspring.repository.*;
import heg.backendspring.service.ServiceCourse;
import heg.backendspring.service.ServiceSubmission;
import heg.backendspring.service.ServiceTP;
import heg.backendspring.utils.ZipUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class TestServiceSubmission {

    @Autowired
    private ServiceCourse serviceCourse;

    @Autowired
    private ServiceSubmission serviceSubmission;

    @Autowired
    private ServiceTP serviceTP;

    @Autowired
    private MapperCourse mapperCourse;

    @Autowired
    private MapperTP mapperTP;

    @MockitoBean
    private RepositoryCourse repositoryCourse;

    @MockitoBean
    private RepositorySubmission repositorySubmission;

    @MockitoBean
    private RepositoryTP repositoryTP;

    @MockitoBean
    private RepositoryStudent repositoryStudent;

    @MockitoBean
    private RepositoryTPStatus repositoryTPStatus;

    private Path testZipInput;
    private Path outputDir;

    @BeforeEach
    void setUp() throws IOException {

        // Dossier final contenant les zips générés
        outputDir = Path.of("target/test-zips");
        if (Files.exists(outputDir)) {
            FileSystemUtils.deleteRecursively(outputDir);
        }
        Files.createDirectories(outputDir);

        // ZIP d’entrée (lecture seule)
        testZipInput = Path.of("src/test/resources/mockinginputstreams/test_zip.zip");
        assertTrue(Files.exists(testZipInput), "Le zip de test doit exister");
    }

    @Test
    @DisplayName("findSubmissionById - submission trouvée")
    void testFindSubmissionById_found() {

        Submission submission = new Submission();
        submission.setId(1L);
        submission.setPathFileStructured("some/path.zip");

        when(repositorySubmission.findById(1L)).thenReturn(Optional.of(submission));

        Optional<SubmissionDto> result = serviceSubmission.findSubmissionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        assertEquals("some/path.zip", result.get().pathFileStructured());
    }

    @Test
    @DisplayName("findSubmissionById - submission non trouvée")
    void testFindSubmissionById_notFound() {

        when(repositorySubmission.findById(99L)).thenReturn(Optional.empty());

        Optional<SubmissionDto> result = serviceSubmission.findSubmissionById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getStudentsSubmission - retourne la liste des dossiers étudiants")
    void testGetStudentsSubmission_nominal() throws IOException {

        // Création d'un dossier temporaire avec deux dossiers étudiants
        Path tempRoot = Files.createTempDirectory("studentsZipTest");
        Path student1 = tempRoot.resolve("georgedylan");
        Path student2 = tempRoot.resolve("scoutmark");
        Files.createDirectories(student1);
        Files.createDirectories(student2);
        Files.createFile(student1.resolve("file1.txt"));
        Files.createFile(student2.resolve("file2.txt"));

        // Création d'un zip "restructuré" à partir de ce dossier
        Path zipPath = tempRoot.resolve("structured.zip");
        ZipUtils.zipDirectory(tempRoot, zipPath);

        Submission submission = new Submission();
        submission.setPathFileStructured(zipPath.toString());

        List<String> result = serviceSubmission.getStudentsSubmission(submission);

        assertTrue(result.contains("georgedylan"));
        assertTrue(result.contains("scoutmark"));
    }


    @Test
    @DisplayName("startZipProcess - ZIP is processed + paths are correct")
    void testProcessZipSubmission_setPathsAndSave() throws Exception {

        Course course = new Course();
        course.setId(10L);
        course.setCode("62-21");

        TP tp = new TP();
        tp.setId(5L);
        tp.setNo(1);
        tp.setCourse(course);

        Submission submission = new Submission();
        submission.setPathStorage(testZipInput.toString());
        tp.setSubmission(submission);

        serviceSubmission.processZipSubmission(course, tp);

        assertNotNull(tp.getSubmission());
        assertNotNull(tp.getSubmission().getPathFileStructured());

        Path structuredZip = Path.of(tp.getSubmission().getPathFileStructured());
        assertTrue(Files.exists(structuredZip));
    }

    @Test
    @DisplayName("processZipSubmission - aucune submission sur le TP => ne fait rien")
    void testProcessZipSubmission_withoutSubmission_doesNothing() throws IOException {

        Course course = new Course();
        course.setId(10L);
        course.setName("Cours test");
        course.setCode("62-21");

        TP tp = new TP();
        tp.setId(5L);
        tp.setNo(1);
        tp.setCourse(course);
        tp.setSubmission(null);

        // Appel de la méthode
        serviceSubmission.processZipSubmission(course, tp);

        // Aucun changement sur le TP
        assertNull(tp.getSubmission());

        // Aucun appel à la couche repository
        verify(repositorySubmission, org.mockito.Mockito.never()).save(any(Submission.class));
    }

    @Test
    @DisplayName("processZipSubmission - courseType null => restructuration quand même")
    void testProcessZipSubmission_withNullCourseType_stillWorks() throws IOException {

        Course course = new Course();
        course.setId(30L);
        course.setName("Cours sans type");
        course.setCode("61-13");
        // important pour tester le cas par défaut
        course.setCourseType(null);

        TP tp = new TP();
        tp.setId(9L);
        tp.setNo(1);
        tp.setCourse(course);

        Submission submission = new Submission();
        // zip de test déjà préparé dans @BeforeEach
        submission.setPathStorage(testZipInput.toString());
        tp.setSubmission(submission);

        serviceSubmission.processZipSubmission(course, tp);

        Submission updated = tp.getSubmission();
        assertNotNull(updated);
        assertNotNull(updated.getPathFileStructured());

        Path structuredZip = Path.of(updated.getPathFileStructured());
        assertTrue(Files.exists(structuredZip));
    }



    @Test
    @DisplayName("startZipProcess restructure ZIP and keeps expected content")
    void testContentZip_verifyZipContents() throws IOException {

        // Course
        Course course = new Course();
        course.setId(20L);
        course.setCode("61-13");
        course.setCourseType(CourseType.PYTHON);

        // TP
        TP tp = new TP();
        tp.setId(8L);
        tp.setNo(1);
        tp.setCourse(course);

        // Simule la sauvegarde de la submission
        when(repositorySubmission.save(any(Submission.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Simulation du chemin du ZIP d’entrée déjà associé au TP
        Submission submission = new Submission();
        submission.setPathStorage(testZipInput.toString());
        tp.setSubmission(submission);

        // Traitement du ZIP
        serviceSubmission.processZipSubmission(course, tp);

        assertNotNull(tp.getSubmission());
        assertNotNull(tp.getSubmission().getPathFileStructured());

        Path structuredZip = Path.of(tp.getSubmission().getPathFileStructured());
        assertTrue(Files.exists(structuredZip));

        // Vérification du contenu du ZIP restructuré
        try (ZipFile zipFile = new ZipFile(structuredZip.toFile())) {
            boolean hasStudentFolder = false;
            boolean containsOtherThanPy = false;
            boolean containsSrcFolder = false;

            for (ZipEntry entry : zipFile.stream().toList()) {
                String entryName = entry.getName();

                if (entryName.startsWith("georgedylan/")) {
                    hasStudentFolder = true;
                    if (!entry.isDirectory() && !entryName.endsWith(".py")) {
                        containsOtherThanPy = true;
                    }
                }

                if (entryName.startsWith("scoutmark/")
                        && entry.isDirectory()
                        && entryName.endsWith("/src/")) {
                    containsSrcFolder = true;
                }
            }

            assertTrue(hasStudentFolder);
            assertFalse(containsOtherThanPy);
            assertTrue(containsSrcFolder);
        }
    }


    }




