package heg.backendspring.TestServices;

import heg.backendspring.entity.*;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.models.TPDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.repository.RepositoryTP;
import heg.backendspring.repository.RepositoryTPStatus;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "zip-storage.path=target/test-zips"
})
class TestServiceTP {

    @Autowired
    private ServiceTP serviceTP;

    @Autowired
    private MapperTP mapperTP;

    @MockitoBean
    private RepositoryTP repositoryTP;

    @MockitoBean
    private RepositoryTPStatus repositoryTPStatus;

    @MockitoBean
    private RepositoryCourse repositoryCourse;
    @MockitoBean
    private ServiceCourse serviceCourse;

    @MockitoBean
    private ServiceSubmission serviceSubmission;

    private Path outputDir;

    @BeforeEach
    void setUp() throws Exception {
        outputDir = Path.of("target", "test-zips");
        if (Files.exists(outputDir)) {
            FileSystemUtils.deleteRecursively(outputDir);
        }
        Files.createDirectories(outputDir);
    }

    @Test
    @DisplayName("findTP - TP exists")
    void testFindTP_exists() {
        Course course = new Course();
        course.setId(10L);

        TP tp = new TP();
        tp.setId(1L);
        tp.setNo(1);
        tp.setCourse(course);

        when(repositoryTP.findById(1L)).thenReturn(Optional.of(tp));

        Optional<TPDto> result = serviceTP.findTP(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        assertEquals(1, result.get().no());
        assertEquals(10L, result.get().courseId());

        verify(repositoryTP).findById(1L);
    }

    @Test
    @DisplayName("findTP - TP not found")
    void testFindTP_notFound() {
        when(repositoryTP.findById(99L)).thenReturn(Optional.empty());

        Optional<TPDto> result = serviceTP.findTP(99L);

        assertTrue(result.isEmpty());
        verify(repositoryTP).findById(99L);
    }

    @Test
    @DisplayName("addSubmissionToTP - zip stored and TP updated")
    void testAddSubmissionToTP() throws Exception {

        Course course = new Course();
        course.setId(10L);
        course.setCode("63-11");

        TP tpEntity = new TP();
        tpEntity.setId(1L);
        tpEntity.setNo(1);
        tpEntity.setCourse(course);

        when(repositoryTP.findById(1L)).thenReturn(Optional.of(tpEntity));

        when(repositoryTP.save(any(TP.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Path tpFolder = outputDir.resolve("63-11").resolve("TP1");
        Files.createDirectories(tpFolder);

        byte[] fakeZip = "fake zip content".getBytes();
        InputStream zipStream = new ByteArrayInputStream(fakeZip);

        TPDto result = serviceTP.addSubmissionToTP(1L, zipStream);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1, result.no());

        Path expectedZip = tpFolder.resolve("TP1_RenduCyberlearn.zip");
        assertTrue(Files.exists(expectedZip));

        verify(repositoryTP).findById(1L);
        verify(repositoryTP).save(any(TP.class));
    }

    @Test
    @DisplayName("addTPStatusListToTP - crée les statuts et marque correctement les rendus")
    void testAddTPStatusListToTP() {
        // ----- Arrange -----
        TP tp = new TP();
        tp.setId(1L);
        tp.setNo(1);
        tp.setStatusStudents(new HashSet<>());

        Submission submission = new Submission();
        tp.setSubmission(submission);

        Set<Student> students = getStudentsHelper();

        // Les noms normalisés comme dans ton service (sans espace, en minuscule)
        when(serviceSubmission.getStudentsSubmission(submission))
                .thenReturn(java.util.List.of("scoutmark", "riggshelly", "georgedylan"));

        when(repositoryTP.save(any(TP.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ----- Act -----
        TPDto result = serviceTP.addTPStatusListToTP(tp, students);

        // ----- Assert -----
        assertNotNull(result);
        assertEquals(4, tp.getStatusStudents().size());

        java.util.Map<String, TPStatus> statusByName = tp.getStatusStudents().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ts -> ts.getStudent().getName(),
                        ts -> ts
                ));

        assertTrue(statusByName.get("Scout Mark").isStudentSubmission());
        assertTrue(statusByName.get("Riggs Helly").isStudentSubmission());
        assertTrue(statusByName.get("George Dylan").isStudentSubmission());
        assertFalse(statusByName.get("Bailiff Irving").isStudentSubmission());

        verify(serviceSubmission).getStudentsSubmission(submission);
        verify(repositoryTP).save(tp);
    }

    private static Set<Student> getStudentsHelper() {
        Student s1 = new Student();
        s1.setId(1L);
        s1.setName("Scout Mark");
        s1.setEmail("Test3mark@hesge.ch");

        Student s2 = new Student();
        s2.setId(2L);
        s2.setName("Riggs Helly");
        s2.setEmail("Test3helly@hesge.ch");

        Student s3 = new Student();
        s3.setId(3L);
        s3.setName("George Dylan");
        s3.setEmail("Test3dylan@hesge.ch");

        Student s4 = new Student();
        s4.setId(4L);
        s4.setName("Bailiff Irving");
        s4.setEmail("Test3irving@hesge.ch");

        Set<Student> students = Set.of(s1, s2, s3, s4);
        return students;
    }


    @Test
    @DisplayName("getSubmissionFileRestructurated - TP with submission returns structured file")
    void testGetSubmissionFileRestructurated() {
        TPDto tpDto = new TPDto(5L, 1, 10L, null, null);

        Submission submission = new Submission();
        submission.setId(99L);
        submission.setPathFileStructured("target/test-zips/63-11/TP1/TP1_RenduRestructuration.zip");

        TP tp = new TP();
        tp.setId(5L);
        tp.setSubmission(submission);

        when(repositoryTP.findById(5L)).thenReturn(Optional.of(tp));

        File result = serviceTP.getSubmissionFileRestructurated(tpDto);

        assertNotNull(result);
        Path expected = Paths.get("target", "test-zips", "63-11", "TP1", "TP1_RenduRestructuration.zip");

        assertEquals(
                expected.toAbsolutePath().normalize(),
                result.toPath().toAbsolutePath().normalize()
        );

        verify(repositoryTP).findById(5L);
    }


}
