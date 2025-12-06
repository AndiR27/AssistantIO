package heg.backendspring.TestServices;

import heg.backendspring.entity.Student;
import heg.backendspring.entity.TP;
import heg.backendspring.entity.TPStatus;
import heg.backendspring.mapping.MapperTPStatus;
import heg.backendspring.models.TPStatusDto;
import heg.backendspring.repository.RepositoryTPStatus;
import heg.backendspring.service.ServiceTPStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static heg.backendspring.enums.StudentSubmissionType.DONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TestServiceTPStatus {

    @Autowired
    private ServiceTPStatus serviceTPStatus;

    @Autowired
    private MapperTPStatus mapperTPStatus;

    @MockitoBean
    private RepositoryTPStatus repositoryTPStatus;

    // =====================================
    //      findTPStatus
    // =====================================

    @Test
    @DisplayName("findTPStatus - TPStatus exists")
    void testFindTPStatus_exists() {
        // ----- Arrange -----
        TPStatus status = new TPStatus();
        status.setId(1L);

        Student student = new Student();
        student.setId(10L);
        status.setStudent(student);

        TP tp = new TP();
        tp.setId(20L);
        status.setTp(tp);

        status.setStudentSubmission(DONE);

        when(repositoryTPStatus.findById(1L)).thenReturn(Optional.of(status));

        // ----- Act -----
        Optional<TPStatusDto> result = serviceTPStatus.findTPStatus(1L);

        // ----- Assert -----
        assertTrue(result.isPresent());
        TPStatusDto dto = result.get();
        assertEquals(1L, dto.id());
        assertEquals(10L, dto.studentId());
        assertEquals(20L, dto.tpId());
        assertEquals(DONE, dto.studentSubmission());

        verify(repositoryTPStatus).findById(1L);
    }

    @Test
    @DisplayName("findTPStatus - TPStatus not found")
    void testFindTPStatus_notFound() {
        // ----- Arrange -----
        when(repositoryTPStatus.findById(99L)).thenReturn(Optional.empty());

        // ----- Act -----
        Optional<TPStatusDto> result = serviceTPStatus.findTPStatus(99L);

        // ----- Assert -----
        assertTrue(result.isEmpty());
        verify(repositoryTPStatus).findById(99L);
    }

    // =====================================
    //      updateTPStatus
    // =====================================

    @Test
    @DisplayName("updateTPStatus - status exists and is updated")
    void testUpdateTPStatus_exists() {
        // ----- Arrange -----
        TPStatus existing = new TPStatus();
        existing.setId(1L);
        existing.setStudentSubmission(null);

        //add TPstatus

        when(repositoryTPStatus.findById(1L)).thenReturn(Optional.of(existing));
        when(repositoryTPStatus.save(any(TPStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TPStatusDto dto = new TPStatusDto(
                1L,
                10L,
                20L,
                null
        );

        // ----- Act -----
        serviceTPStatus.updateTPStatus(1L, DONE);

        // ----- Assert -----
        assertEquals(DONE, existing.getStudentSubmission());
        verify(repositoryTPStatus).findById(1L);
        verify(repositoryTPStatus).save(existing);
    }

    @Test
    @DisplayName("updateTPStatus - status not found, no save")
    void testUpdateTPStatus_notFound() {
        // ----- Arrange -----
        when(repositoryTPStatus.findById(999L)).thenReturn(Optional.empty());

        TPStatusDto dto = new TPStatusDto(
                999L,
                10L,
                20L,
                null
        );

        // ----- Act -----
        serviceTPStatus.updateTPStatus(999L, DONE);

        // ----- Assert -----
        verify(repositoryTPStatus).findById(999L);
        verify(repositoryTPStatus, never()).save(any(TPStatus.class));
    }

    // =====================================
    //      deleteTPStatus
    // =====================================

    @Test
    @DisplayName("deleteTPStatus - existing id, delete called")
    void testDeleteTPStatus_exists() {
        // ----- Arrange -----
        when(repositoryTPStatus.existsById(1L)).thenReturn(true);

        // ----- Act -----
        serviceTPStatus.deleteTPStatus(1L);

        // ----- Assert -----
        verify(repositoryTPStatus).existsById(1L);
        verify(repositoryTPStatus).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTPStatus - id not found, no delete")
    void testDeleteTPStatus_notExists() {
        // ----- Arrange -----
        when(repositoryTPStatus.existsById(1L)).thenReturn(false);

        // ----- Act -----
        serviceTPStatus.deleteTPStatus(1L);

        // ----- Assert -----
        verify(repositoryTPStatus).existsById(1L);
        verify(repositoryTPStatus, never()).deleteById(anyLong());
    }

    // =====================================
    //      checkExistingTPstatus
    // =====================================

    @Test
    @DisplayName("checkExistingTPstatus - existing TPStatus returned, no creation")
    void testCheckExistingTPstatus_existing() {
        // ----- Arrange -----
        Student student = new Student();
        student.setId(10L);

        TP tp = new TP();
        tp.setId(20L);

        TPStatus existing = new TPStatus();
        existing.setId(1L);
        existing.setStudent(student);
        existing.setTp(tp);
        existing.setStudentSubmission(DONE);

        when(repositoryTPStatus.findByStudentIdAndTpId(10L, 20L))
                .thenReturn(Optional.of(existing));

        TPStatusDto expectedDto = mapperTPStatus.toDto(existing);

        // ----- Act -----
        TPStatusDto result = serviceTPStatus.checkExistingTPstatus(student, tp);

        // ----- Assert -----
        assertNotNull(result);
        assertEquals(expectedDto, result);  // même contenu que l'entité existante
        verify(repositoryTPStatus).findByStudentIdAndTpId(10L, 20L);
        verify(repositoryTPStatus, never()).save(any(TPStatus.class));
    }

    @Test
    @DisplayName("checkExistingTPstatus - no existing status, new one created and saved")
    void testCheckExistingTPstatus_new() {
        // ----- Arrange -----
        Student student = new Student();
        student.setId(10L);

        TP tp = new TP();
        tp.setId(20L);

        when(repositoryTPStatus.findByStudentIdAndTpId(10L, 20L))
                .thenReturn(Optional.empty());

        when(repositoryTPStatus.save(any(TPStatus.class))).thenAnswer(invocation -> {
            TPStatus s = invocation.getArgument(0);
            s.setId(42L);
            return s;
        });

        // ----- Act -----
        TPStatusDto result = serviceTPStatus.checkExistingTPstatus(student, tp);

        // ----- Assert -----
        assertNotNull(result);
        assertEquals(42L, result.id());
        assertEquals(10L, result.studentId());
        assertEquals(20L, result.tpId());

        verify(repositoryTPStatus).findByStudentIdAndTpId(10L, 20L);
        verify(repositoryTPStatus).save(any(TPStatus.class));
    }
}
