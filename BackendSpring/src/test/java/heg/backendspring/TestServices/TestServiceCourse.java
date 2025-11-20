package heg.backendspring.TestServices;

import heg.backendspring.entity.Course;
import heg.backendspring.enums.SemesterType;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.models.CourseDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.service.ServiceCourse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.Provider;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TestServiceCourse {

    @Autowired
    private ServiceCourse serviceCourse;

    @MockitoBean
    private RepositoryCourse repositoryCourse;

    @Autowired
    private MapperCourse mapperCourse;

    @Test
    @DisplayName("findCourseById - course exists")
    void testFindCourseById_CourseExists() {
        CourseDto dto = new CourseDto(1L, "Prog java 3", "63-31", SemesterType.AUTOMNE, 2025, "Stettler",
                null, null, null);
        Course entity = mapperCourse.toEntity(dto);

        when(repositoryCourse.save(any(Course.class))).thenReturn(entity);
        when(repositoryCourse.findById(1L)).thenReturn(Optional.of(entity));

        Optional<CourseDto> result = serviceCourse.findCourseById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        assertEquals("63-31", result.get().code());
        verify(repositoryCourse).findById(1L);
    }

    @Test
    @DisplayName("findCourseById - course not found")
    void testFindCourseById_notFound() {
        when(repositoryCourse.findById(99L)).thenReturn(Optional.empty());

        Optional<CourseDto> result = serviceCourse.findCourseById(99L);

        assertTrue(result.isEmpty());
        verify(repositoryCourse).findById(99L);
    }


    @Test
    @DisplayName("findAllCourses - returns list of dtos")
    void testFindAllCourses() {
        CourseDto dto1 = new CourseDto(1L, "Python 1", "63-11", null, 2024, "Dupont",
                null, null, null);
        CourseDto dto2 = new CourseDto(2L, "Java JEE", "63-41", SemesterType.PRINTEMPS, 2025, "Durand",
                null, null, null);

        Course e1 = mapperCourse.toEntity(dto1);
        Course e2 = mapperCourse.toEntity(dto2);

        when(repositoryCourse.save(any(Course.class))).thenReturn(e1).thenReturn(e2);
        when(repositoryCourse.findAll()).thenReturn(List.of(e1, e2));

        // Ajout des entités puis récupération des DTOs
        serviceCourse.addCourse(dto1);
        serviceCourse.addCourse(dto2);
        List<CourseDto> result = serviceCourse.findAllCourses();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("63-11", result.get(0).code());
        assertEquals("63-41", result.get(1).code());

        verify(repositoryCourse, Mockito.times(2)).save(any(Course.class));
        verify(repositoryCourse).findAll();
    }


    @Test
    @DisplayName("addCourse - saves an entity and returns dto with id")
    void testAddCourse() {
        CourseDto input = new CourseDto(null, "Java2", "63-21", SemesterType.PRINTEMPS, 2024, "Dupont",
                null, null, null);

        // le service va mapper le DTO -> entity, on renvoie la même instance avec un id
        when(repositoryCourse.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CourseDto result = serviceCourse.addCourse(input);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(1L, result.id());
        assertEquals("63-21", result.code());

        verify(repositoryCourse).save(any(Course.class));
    }
}
