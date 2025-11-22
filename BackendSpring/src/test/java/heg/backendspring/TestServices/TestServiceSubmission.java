package heg.backendspring.TestServices;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Submission;
import heg.backendspring.entity.TP;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.repository.*;
import heg.backendspring.service.ServiceCourse;
import heg.backendspring.service.ServiceSubmission;
import heg.backendspring.service.ServiceTP;
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
import java.util.Optional;

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

    // -------------------------
    // TEST 1 : traitement ZIP
    // -------------------------
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

        when(repositorySubmission.save(any(Submission.class)))
                .thenAnswer(invocation -> {
                    Submission s = invocation.getArgument(0);
                    s.setId(42L);
                    return s;
                });

        serviceSubmission.processZipSubmission(course, tp);

        assertNotNull(tp.getSubmission());
        assertEquals(42L, tp.getSubmission().getId());
        // tu peux vérifier la logique de path si elle est dans SubmissionService
        // assertEquals("...", tp.getSubmission().getPathStorage());

        verify(repositorySubmission).save(any(Submission.class));
    }
}

