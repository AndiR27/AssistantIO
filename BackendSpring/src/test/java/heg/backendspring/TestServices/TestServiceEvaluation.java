package heg.backendspring.TestServices;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Evaluation;
import heg.backendspring.entity.Submission;
import heg.backendspring.mapping.MapperEvaluation;
import heg.backendspring.models.EvaluationDto;
import heg.backendspring.repository.RepositoryEvaluation;
import heg.backendspring.service.ServiceEvaluation;
import heg.backendspring.service.serviceSubmission.ServiceSubmission;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileSystemUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TestServiceEvaluation {

    @Autowired
    private ServiceEvaluation serviceEvaluation;

    @Autowired
    private MapperEvaluation mapperEvaluation;

    @MockitoBean
    private RepositoryEvaluation repositoryEvaluation;

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
        // IMPORTANT : faire pointer zipStoragePath vers outputDir
        ReflectionTestUtils.setField(serviceEvaluation, "zipStoragePath", outputDir.toString());

    }


    // =====================================
    //      getEvaluationById
    // =====================================
    @Test
    @DisplayName("getEvaluationById - Evaluation exists (courseId mapped from entity.course.id)")
    void testGetEvaluationById_exists() {
        // ----- Arrange -----
        Evaluation entity = entity(1L, "Eval 1", LocalDate.of(2026, 2, 1), 2, 10L);
        when(repositoryEvaluation.findById(1L)).thenReturn(Optional.of(entity));

        // ----- Act -----
        Optional<EvaluationDto> result = serviceEvaluation.getEvaluationById(1L);

        // ----- Assert -----
        assertTrue(result.isPresent());
        EvaluationDto dto = result.get();
        assertEquals(1L, dto.id());
        assertEquals("Eval 1", dto.name());
        assertEquals(LocalDate.of(2026, 2, 1), dto.date());
        assertEquals(2, dto.coefficient());
        assertEquals(10L, dto.courseId()); // ✅ important ici

        verify(repositoryEvaluation).findById(1L);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("getEvaluationById - Evaluation not found")
    void testGetEvaluationById_notFound() {
        // ----- Arrange -----
        when(repositoryEvaluation.findById(99L)).thenReturn(Optional.empty());

        // ----- Act -----
        Optional<EvaluationDto> result = serviceEvaluation.getEvaluationById(99L);

        // ----- Assert -----
        assertTrue(result.isEmpty());

        verify(repositoryEvaluation).findById(99L);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    // =====================================
    //      getAllEvaluations
    // =====================================

    @Test
    @DisplayName("getAllEvaluations - returns mapped list (courseId filled)")
    void testGetAllEvaluations_returnsList() {
        // ----- Arrange -----
        Evaluation e1 = entity(1L, "Eval 1", LocalDate.of(2026, 2, 1), 2, 10L);
        Evaluation e2 = entity(2L, "Eval 2", LocalDate.of(2026, 2, 2), 3, 11L);

        when(repositoryEvaluation.findAll()).thenReturn(List.of(e1, e2));

        // ----- Act -----
        List<EvaluationDto> result = serviceEvaluation.getAllEvaluations();

        // ----- Assert -----
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).id());
        assertEquals(10L, result.get(0).courseId());

        assertEquals(2L, result.get(1).id());
        assertEquals(11L, result.get(1).courseId());

        verify(repositoryEvaluation).findAll();
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("getAllEvaluations - empty list")
    void testGetAllEvaluations_empty() {
        // ----- Arrange -----
        when(repositoryEvaluation.findAll()).thenReturn(List.of());

        // ----- Act -----
        List<EvaluationDto> result = serviceEvaluation.getAllEvaluations();

        // ----- Assert -----
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repositoryEvaluation).findAll();
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    // =====================================
    //      getAllEvaluationsFromCourse
    // =====================================

    @Test
    @DisplayName("getAllEvaluationsFromCourse - returns mapped list (courseId matches)")
    void testGetAllEvaluationsFromCourse_returnsList() {
        // ----- Arrange -----
        long courseId = 10L;

        Evaluation e1 = entity(1L, "Eval 1", LocalDate.of(2026, 2, 1), 2, courseId);
        Evaluation e2 = entity(2L, "Eval 2", LocalDate.of(2026, 2, 2), 3, courseId);

        when(repositoryEvaluation.findAllByCourseId(courseId)).thenReturn(List.of(e1, e2));

        // ----- Act -----
        List<EvaluationDto> result = serviceEvaluation.getAllEvaluationsFromCourse(courseId);

        // ----- Assert -----
        assertEquals(2, result.size());
        assertEquals(courseId, result.get(0).courseId());
        assertEquals(courseId, result.get(1).courseId());

        verify(repositoryEvaluation).findAllByCourseId(courseId);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("getAllEvaluationsFromCourse - empty list")
    void testGetAllEvaluationsFromCourse_empty() {
        // ----- Arrange -----
        long courseId = 999L;
        when(repositoryEvaluation.findAllByCourseId(courseId)).thenReturn(List.of());

        // ----- Act -----
        List<EvaluationDto> result = serviceEvaluation.getAllEvaluationsFromCourse(courseId);

        // ----- Assert -----
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repositoryEvaluation).findAllByCourseId(courseId);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    // =====================================
    //      createEvaluation
    // =====================================

    @Test
    @DisplayName("createEvaluation - saves entity (course ignored by mapper) and returns dto from saved entity")
    void testCreateEvaluation_success() {
        // ----- Arrange -----
        EvaluationDto input = dto(null, "New Eval", LocalDate.of(2026, 2, 3), 4, 10L);

        // On simule la base: elle renvoie une entity persistée AVEC id et course set (souvent fait côté service/repo réel)
        Evaluation saved = entity(123L, "New Eval", LocalDate.of(2026, 2, 3), 4, 10L);

        when(repositoryEvaluation.save(any(Evaluation.class))).thenReturn(saved);

        // ----- Act -----
        EvaluationDto result = serviceEvaluation.createEvaluation(input);

        // ----- Assert -----
        assertNotNull(result);
        assertEquals(123L, result.id());
        assertEquals("New Eval", result.name());
        assertEquals(LocalDate.of(2026, 2, 3), result.date());
        assertEquals(4, result.coefficient());
        assertEquals(10L, result.courseId()); // ✅ grâce au saved.course.id

        // Bonus: vérifier que le mapper a bien ignoré course lors du save (optionnel mais utile)
        // On capture l'entité passée à save et on check que course == null
        var captor = org.mockito.ArgumentCaptor.forClass(Evaluation.class);
        verify(repositoryEvaluation).save(captor.capture());
        Evaluation toSave = captor.getValue();

        // Comme toEntity ignore course, l'entité issue du dto ne devrait PAS avoir course
        assertNull(toSave.getCourse(), "MapperEvaluation.toEntity() ignore 'course' => course doit rester null");

        verifyNoMoreInteractions(repositoryEvaluation);
    }

    // =====================================
    //      updateEvaluation
    // =====================================

    @Test
    @DisplayName("updateEvaluation - exists and is updated (course untouched/ignored)")
    void testUpdateEvaluation_exists() {
        // ----- Arrange -----
        // existing a déjà un course (important pour que toDto sorte courseId)
        Evaluation existing = entity(1L, "Before", LocalDate.of(2026, 2, 1), 1, 10L);

        when(repositoryEvaluation.findById(1L)).thenReturn(Optional.of(existing));
        when(repositoryEvaluation.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluationDto input = dto(1L, "Updated", LocalDate.of(2026, 2, 4), 5, 999L /* ignoré */);

        // ----- Act -----
        EvaluationDto result = serviceEvaluation.updateEvaluation(input);

        // ----- Assert -----
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Updated", result.name());
        assertEquals(LocalDate.of(2026, 2, 4), result.date());
        assertEquals(5, result.coefficient());

        // courseId doit rester celui de l'entité existante, car updateEntityFromDto ignore course
        assertEquals(10L, result.courseId());

        verify(repositoryEvaluation).findById(1L);
        verify(repositoryEvaluation).save(existing);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("updateEvaluation - not found throws EntityNotFoundException")
    void testUpdateEvaluation_notFound() {
        // ----- Arrange -----
        when(repositoryEvaluation.findById(999L)).thenReturn(Optional.empty());
        EvaluationDto input = dto(999L, "Does not matter", LocalDate.of(2026, 2, 1), 1, 10L);

        // ----- Act + Assert -----
        assertThrows(EntityNotFoundException.class, () -> serviceEvaluation.updateEvaluation(input));

        verify(repositoryEvaluation).findById(999L);
        verify(repositoryEvaluation, never()).save(any());
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    // =====================================
    //      deleteEvaluation
    // =====================================

    @Test
    @DisplayName("deleteEvaluation - exists => deleteById called")
    void testDeleteEvaluation_exists() {
        // ----- Arrange -----
        when(repositoryEvaluation.existsById(1L)).thenReturn(true);

        // ----- Act -----
        serviceEvaluation.deleteEvaluation(1L);

        // ----- Assert -----
        verify(repositoryEvaluation).existsById(1L);
        verify(repositoryEvaluation).deleteById(1L);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("deleteEvaluation - not exists => nothing happens")
    void testDeleteEvaluation_notExists() {
        // ----- Arrange -----
        when(repositoryEvaluation.existsById(99L)).thenReturn(false);

        // ----- Act -----
        serviceEvaluation.deleteEvaluation(99L);

        // ----- Assert -----
        verify(repositoryEvaluation).existsById(99L);
        verify(repositoryEvaluation, never()).deleteById(anyLong());
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("addSubmissionToEvaluation - evaluation exists => zip copied, submission set, saved and dto returned")
    void testAddSubmissionToEvaluation_exists() throws Exception {
        // Arrange
        Long evalId = 1L;

        Course course = new Course();
        course.setId(10L);
        course.setCode("63-31");

        Evaluation evalEntity = new Evaluation();
        evalEntity.setId(evalId);
        evalEntity.setName("CC1");
        evalEntity.setCourse(course);

        when(repositoryEvaluation.findById(evalId)).thenReturn(Optional.of(evalEntity));
        when(repositoryEvaluation.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Créer le dossier attendu (car la méthode ne fait pas createDirectories)
        Path expectedFolder = outputDir.resolve("63-31").resolve("CC1");
        Files.createDirectories(expectedFolder);

        byte[] zipBytes = "fake-zip-content".getBytes();
        InputStream zipStream = new ByteArrayInputStream(zipBytes);

        // Act
        EvaluationDto result = serviceEvaluation.addSubmissionToEvaluation(evalId, zipStream);

        // Assert
        assertNotNull(result);

        // Vérifier que save a été appelé avec une Evaluation qui a une submission
        var captor = org.mockito.ArgumentCaptor.forClass(Evaluation.class);
        verify(repositoryEvaluation).save(captor.capture());
        Evaluation saved = captor.getValue();

        assertNotNull(saved.getSubmission(), "Submission doit être créée et attachée à l'évaluation");

        String expectedFileName = "CC1_RenduCyberlearn.zip";
        Path expectedZipPath = expectedFolder.resolve(expectedFileName);

        assertEquals(expectedFileName, saved.getSubmission().getFileName());
        assertEquals(expectedZipPath.toString(), saved.getSubmission().getPathStorage());

        // Vérifier que le zip existe réellement (copie)
        assertTrue(Files.exists(expectedZipPath), "Le fichier zip doit avoir été copié");
        assertArrayEquals(zipBytes, Files.readAllBytes(expectedZipPath));

        verify(repositoryEvaluation).findById(evalId);
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("addSubmissionToEvaluation - evaluation not found => returns null, no save")
    void testAddSubmissionToEvaluation_notFound() {
        Long evalId = 999L;
        when(repositoryEvaluation.findById(evalId)).thenReturn(Optional.empty());

        EvaluationDto result = serviceEvaluation.addSubmissionToEvaluation(evalId, new ByteArrayInputStream(new byte[]{1}));

        assertNull(result);

        verify(repositoryEvaluation).findById(evalId);
        verify(repositoryEvaluation, never()).save(any());
        verifyNoMoreInteractions(repositoryEvaluation);
    }

    @Test
    @DisplayName("startZipProcess - evaluation exists => calls processZipSubmissionForEvaluation")
    void testStartZipProcess_exists_callsProcess() throws Exception {
        Long evalId = 1L;

        Course course = new Course();
        course.setId(10L);
        course.setCode("63-31");

        Evaluation evalEntity = new Evaluation();
        evalEntity.setId(evalId);
        evalEntity.setName("CC1");
        evalEntity.setCourse(course);

        when(repositoryEvaluation.findById(evalId)).thenReturn(Optional.of(evalEntity));

        serviceEvaluation.startZipProcess(evalId);

        verify(repositoryEvaluation).findById(evalId);
        verify(serviceSubmission).processZipSubmissionForEvaluation(course, evalEntity);
        verifyNoMoreInteractions(repositoryEvaluation);
        verifyNoMoreInteractions(serviceSubmission);
    }

    @Test
    @DisplayName("startZipProcess - evaluation not found => throws EntityNotFoundException")
    void testStartZipProcess_notFound_throws() {
        Long evalId = 999L;
        when(repositoryEvaluation.findById(evalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceEvaluation.startZipProcess(evalId));

        verify(repositoryEvaluation).findById(evalId);
        verifyNoMoreInteractions(repositoryEvaluation);
        verifyNoInteractions(serviceSubmission);
    }

    @Test
    @DisplayName("getSubmissionFileRestructurated - submission has pathFileStructured => returns file")
    void testGetSubmissionFileRestructurated_returnsFile() throws Exception {
        Long evalId = 1L;

        Path structured = outputDir.resolve("structured.zip");
        Files.write(structured, "zip".getBytes());

        Submission submission = new Submission();
        submission.setPathFileStructured(structured.toString());

        Evaluation evalEntity = new Evaluation();
        evalEntity.setId(evalId);
        evalEntity.setSubmission(submission);

        when(repositoryEvaluation.findById(evalId)).thenReturn(Optional.of(evalEntity));

        File result = serviceEvaluation.getSubmissionFileRestructurated(evalId);

        assertNotNull(result);
        assertEquals(structured.toFile().getPath(), result.getPath());
        assertTrue(result.exists());

        verify(repositoryEvaluation).findById(evalId);
        verifyNoMoreInteractions(repositoryEvaluation);
    }





    // =====================================
    // Helpers
    // =====================================

    private EvaluationDto dto(Long id, String name, LocalDate date, int coefficient, Long courseId) {
        return new EvaluationDto(id, name, date, coefficient, courseId, null, null);
    }

    /**
     * Crée une Evaluation avec un Course non-null pour que mapperEvaluation.toDto()
     * puisse mapper course.id -> courseId.
     */
    private Evaluation entity(Long id, String name, LocalDate date, int coefficient, Long courseId) {
        Course course = new Course();
        course.setId(courseId);

        Evaluation e = new Evaluation();
        e.setId(id);
        e.setName(name);
        e.setDate(date);
        e.setCoefficient(coefficient);
        e.setCourse(course);
        e.setSubmission(null);

        return e;
    }
}
