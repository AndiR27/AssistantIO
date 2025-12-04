package heg.backendspring.TestServices;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Student;
import heg.backendspring.entity.TP;
import heg.backendspring.enums.CourseType;
import heg.backendspring.enums.SemesterType;
import heg.backendspring.enums.StudyType;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.mapping.MapperStudent;
import heg.backendspring.models.CourseDto;
import heg.backendspring.models.StudentDto;
import heg.backendspring.models.TPDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.repository.RepositoryStudent;
import heg.backendspring.service.ServiceCourse;
import heg.backendspring.service.ServiceStudent;
import heg.backendspring.service.ServiceSubmission;
import heg.backendspring.service.ServiceTP;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TestServiceCourse {

    @Autowired
    private ServiceCourse serviceCourse;
    @Autowired
    private ServiceStudent serviceStudent;

    @MockitoBean
    private RepositoryCourse repositoryCourse;
    @MockitoBean
    private RepositoryStudent repositoryStudent;
    @MockitoBean
    private ServiceSubmission serviceSubmission;
    @MockitoBean
    private ServiceTP serviceTP;

    @Autowired
    private MapperCourse mapperCourse;
    @Autowired
    private MapperStudent mapperStudent;





    @Test
    @DisplayName("findCourseById - course exists")
    void testFindCourseById_CourseExists() {
        CourseDto dto = new CourseDto(1L, "Prog java 3", "63-31", SemesterType.AUTOMNE, CourseType.JAVA, 2025, "Stettler",
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
        CourseDto dto1 = new CourseDto(1L, "Python 1", "63-11", null, null,2024, "Dupont",
                null, null, null);
        CourseDto dto2 = new CourseDto(2L, "Java JEE", "63-41", SemesterType.PRINTEMPS, CourseType.JAVA_JEE, 2025, "Durand",
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
        CourseDto input = new CourseDto(null, "Java2", "63-21", SemesterType.PRINTEMPS, CourseType.JAVA, 2024, "Dupont",
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

    @Test
    @DisplayName("addStudent - existing student not in course")
    void testAddStudent_existingStudent_notInCourse() {

        // DTO reçu
        StudentDto dto = new StudentDto(
                10L, "Alice", "alice@etu.ch", StudyType.TEMPS_PLEIN, null
        );

        // Étudiant déjà existant en BD
        Student entity = mapperStudent.toEntity(dto);

        // Cours existant
        CourseDto cDto = new CourseDto(
                1L, "Java", "63-21", SemesterType.PRINTEMPS,CourseType.JAVA, 2024,
                "Dupont", null, null, null
        );
        Course course = mapperCourse.toEntity(cDto);

        // Mocks
        when(repositoryCourse.findById(1L)).thenReturn(Optional.of(course));
        when(repositoryStudent.findStudentByEmail("alice@etu.ch")).thenReturn(Optional.of(entity));
        when(repositoryStudent.save(any(Student.class))).thenReturn(entity);

        // Act
        StudentDto result = serviceCourse.addStudent(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.id());
        assertTrue(course.getStudents().contains(entity));
        assertTrue(entity.getStudentCourses().contains(course));

        verify(repositoryCourse).findById(1L);
        verify(repositoryStudent).findStudentByEmail("alice@etu.ch");
        verify(repositoryStudent).save(any(Student.class));
    }

    @Test
    @DisplayName("addStudent - new student is created and added to course")
    void testAddStudent_newStudent() {

        Course course = new Course();
        course.setId(2L);
        when(repositoryCourse.findById(2L)).thenReturn(Optional.of(course));

        StudentDto input = new StudentDto(null, "Bob", "bob@etu.ch", StudyType.TEMPS_PLEIN, null);

        when(repositoryStudent.save(any(Student.class)))
                .thenAnswer(invocation -> {
                    Student s = invocation.getArgument(0);
                    s.setId(20L);
                    return s;
                });

        StudentDto result = serviceCourse.addStudent(2L, input);

        assertNotNull(result);
        assertEquals(20L, result.id());
        //test des relations
        assertEquals(1, course.getStudents().size());
        assertTrue(course.getStudents().contains(mapperStudent.toEntity(result)));

        verify(repositoryCourse).findById(2L);
        verify(repositoryStudent, Mockito.times(2)).save(any(Student.class));
    }

    @Test
    @DisplayName("addAllStudentsToCourse - all students are created and added to course")
    void testAddAllStudentsToCourse() {

        Course course = new Course();
        course.setId(2L);
        when(repositoryCourse.findById(2L)).thenReturn(Optional.of(course));

        StudentDto s1 = new StudentDto(null, "Bob", "bob@etu.ch", StudyType.TEMPS_PLEIN, null);
        StudentDto s2 = new StudentDto(null, "Alice", "alice@etu.ch", StudyType.TEMPS_PLEIN, null);

        // Aucun étudiant n'existe encore
        when(repositoryStudent.findStudentByEmail("bob@etu.ch")).thenReturn(Optional.empty());
        when(repositoryStudent.findStudentByEmail("alice@etu.ch")).thenReturn(Optional.empty());

        // Simuler save() → met un ID + renvoie l'instance
        when(repositoryStudent.save(any(Student.class)))
                .thenAnswer(invocation -> {
                    Student s = invocation.getArgument(0);
                    if (s.getEmail().equals("bob@etu.ch")) s.setId(10L);
                    if (s.getEmail().equals("alice@etu.ch")) s.setId(11L);
                    return s;
                });

        CourseDto result = serviceCourse.addAllStudendsToCourse(2L, List.of(s1, s2));

        assertNotNull(result);
        assertEquals(2L, result.id());

        assertEquals(2, course.getStudents().size());
        assertTrue(course.getStudents().stream().anyMatch(s -> s.getId() == 10L));
        assertTrue(course.getStudents().stream().anyMatch(s -> s.getId() == 11L));

        verify(repositoryCourse, times(3)).findById(2L);
        verify(repositoryStudent).findStudentByEmail("bob@etu.ch");
        verify(repositoryStudent).findStudentByEmail("alice@etu.ch");
        verify(repositoryStudent, times(4)).save(any(Student.class));
    }

    @Test
    @DisplayName("addAllStudentsFromFile - all students are created from txt file and added to course")
    void testAddAllStudentsFromFile() {

        Course course = new Course();
        course.setId(1L);
        when(repositoryCourse.findById(1L)).thenReturn(Optional.of(course));
        String[] data = {
                "Walter White;walter@hesge.ch;TEMPS_PLEIN",
                "Jesse Pinkman;jesse@hesge.ch;TEMPS_PARTIEL"
        };

        when(repositoryStudent.findStudentByEmail(anyString()))
                .thenReturn(Optional.empty());

        when(repositoryStudent.save(any(Student.class)))
                .thenAnswer(invocation -> {
                    Student s = invocation.getArgument(0);
                    if (s.getId() == null) {
                        if ("walter@hesge.ch".equals(s.getEmail())) {
                            s.setId(10L);
                        } else if ("jesse@hesge.ch".equals(s.getEmail())) {
                            s.setId(11L);
                        } else {
                            s.setId(99L);
                        }
                    }
                    return s;
                });

        List<StudentDto> result = serviceCourse.addAllStudentsFromFile(1L, data);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, course.getStudents().size());
        assertTrue(course.getStudents().stream().anyMatch(s -> "walter@hesge.ch".equals(s.getEmail())));
        assertTrue(course.getStudents().stream().anyMatch(s -> "jesse@hesge.ch".equals(s.getEmail())));

        verify(repositoryCourse, times(2)).findById(1L);           // 2x addStudent + 1x findCourseById
        verify(repositoryStudent, times(2)).findStudentByEmail(anyString());
        verify(repositoryStudent, times(4)).save(any(Student.class));
    }

    @Test
    @DisplayName("getAllStudentsFromCourse - returns mapped list of students")
    void testGetAllStudentsFromCourse() {
        Long courseId = 1L;

        Student s1 = new Student();
        s1.setId(10L);
        s1.setName("Walter White");
        s1.setEmail("walter@hesge.ch");
        s1.setStudyType(StudyType.TEMPS_PLEIN);

        Student s2 = new Student();
        s2.setId(11L);
        s2.setName("Jesse Pinkman");
        s2.setEmail("jesse@hesge.ch");
        s2.setStudyType(StudyType.TEMPS_PARTIEL);

        when(repositoryCourse.findStudentsByCourseId(courseId))
                .thenReturn(Set.of(s1, s2));

        List<StudentDto> result = serviceCourse.getAllStudentsFromCourse(courseId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.id().equals(10L) && s.email().equals("walter@hesge.ch")));
        assertTrue(result.stream().anyMatch(s -> s.id().equals(11L) && s.email().equals("jesse@hesge.ch")));

        verify(repositoryCourse).findStudentsByCourseId(courseId);
    }

    @Test
    @DisplayName("removeStudentFromCourse - student is removed from course")
    void testRemoveStudentFromCourse() {

        Course course = new Course();
        course.setId(1L);

        Student student = new Student();
        student.setId(10L);

        course.getStudents().add(student);
        student.getStudentCourses().add(course);

        when(repositoryCourse.findById(1L)).thenReturn(Optional.of(course));
        when(repositoryStudent.findById(10L)).thenReturn(Optional.of(student));
        when(repositoryStudent.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        serviceCourse.removeStudentFromCourse(1L, 10L);

        assertTrue(course.getStudents().isEmpty());
        assertTrue(student.getStudentCourses().isEmpty());

        verify(repositoryCourse).findById(1L);
        verify(repositoryStudent).findById(10L);
        verify(repositoryStudent).save(any(Student.class));
    }

    @Test
    @DisplayName("addTPtoCourse – TP is created and added to course")
    void testAddTPtoCourse() {

        Course course = new Course();
        course.setId(1L);
        course.setCode("63-11");
        course.setName("Prog Java");

        when(repositoryCourse.findById(1L)).thenReturn(Optional.of(course));
        when(repositoryCourse.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // On espionne le service pour mocker creerDossierZip sans créer de vrai dossier
        ServiceCourse spyService = Mockito.spy(serviceCourse);
        //doNothing().when(spyService).creerDossierZip(anyString(), anyString());

        TPDto result = spyService.addTPtoCourse(1L, 1);

        assertNotNull(result);
        assertEquals(1, result.no());
        assertEquals(1, course.getTps().size());

        TP addedTp = course.getTps().iterator().next();
        assertEquals(1, addedTp.getNo());
        assertEquals(course, addedTp.getCourse());

        verify(repositoryCourse).findById(1L);
        verify(repositoryCourse).save(course);
        //verify(spyService).creerDossierZip(eq("TP1"), eq("DocumentsZip/63-11"));
    }

    @Test
    @DisplayName("getAllTPsFromCourse - returns mapped list of TPs")
    void testGetAllTPsFromCourse() {
        Long courseId = 1L;

        TP tp1 = new TP();
        tp1.setId(10L);
        tp1.setNo(1);

        TP tp2 = new TP();
        tp2.setId(11L);
        tp2.setNo(2);

        when(repositoryCourse.findAllTPsByCourseId(courseId))
                .thenReturn(Set.of(tp1, tp2));

        List<TPDto> result = serviceCourse.getAllTPsFromCourse(courseId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tp -> tp.id().equals(10L) && tp.no() == 1));
        assertTrue(result.stream().anyMatch(tp -> tp.id().equals(11L) && tp.no() == 2));

        verify(repositoryCourse).findAllTPsByCourseId(courseId);
    }


    @Test
    @DisplayName("deleteTPFromCourseByNo - TP is removed from course and course is saved")
    void testDeleteTPFromCourseByNo() {

        Course course = new Course();
        course.setId(1L);

        TP tp = new TP();
        tp.setId(10L);
        tp.setNo(1);
        tp.setCourse(course);
        course.getTps().add(tp);

        when(repositoryCourse.findTPByCourseIdAndNo(1L, 1))
                .thenReturn(Optional.of(tp));
        when(repositoryCourse.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        serviceCourse.deleteTPFromCourseByNo(1L, 1);

        assertTrue(course.getTps().isEmpty());

        verify(repositoryCourse).findTPByCourseIdAndNo(1L, 1);
        verify(repositoryCourse).save(course);
    }

    @Test
    @DisplayName("updateTPfromCourseByNo - TP is updated and saved")
    void testUpdateTPfromCourseByNo() {
        Course course = new Course();
        course.setId(1L);

        TP tp = new TP();
        tp.setId(10L);
        tp.setNo(1);
        tp.setCourse(course);
        course.getTps().add(tp);

        TPDto tpDtoInput = new TPDto(10L, 2, 1L, null, null);
        when(repositoryCourse.findTPByCourseIdAndNo(1L, 1))
                .thenReturn(Optional.of(tp));
        when(repositoryCourse.findById(1L))
                .thenReturn(Optional.of(course));
        when(repositoryCourse.save(course))
                .thenReturn(course);

        Optional<TPDto> result =
                serviceCourse.updateTPfromCourseByNo(1L, 1, tpDtoInput);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().no());
        assertEquals(1L, result.get().courseId());
        assertEquals(1, course.getTps().size());
        assertEquals(2, course.getTps().iterator().next().getNo());

        verify(repositoryCourse).findTPByCourseIdAndNo(1L, 1);
        verify(repositoryCourse).findById(1L);
        verify(repositoryCourse).save(course);

    }

    @Test
    @DisplayName("startZipProcess - calls submissionService with course and TP")
    void testStartZipProcess_callsSubmissionService() throws IOException {
        Long courseId = 10L;
        int tpNo = 1;

        Course course = new Course();
        course.setId(courseId);
        course.setCode("62-21");
        course.setName("Approfondissement");

        TP tp = new TP();
        tp.setId(5L);
        tp.setNo(tpNo);
        tp.setCourse(course);
        course.getTps().add(tp);

        when(repositoryCourse.findById(courseId)).thenReturn(Optional.of(course));
        when(repositoryCourse.findTPByCourseIdAndNo(courseId, tpNo)).thenReturn(Optional.of(tp));

        serviceCourse.startZipProcess(courseId, tpNo);

        verify(repositoryCourse).findById(courseId);
        verify(repositoryCourse).findTPByCourseIdAndNo(courseId, tpNo);
        verify(serviceSubmission).processZipSubmission(course, tp);
        verifyNoMoreInteractions(serviceSubmission);
    }

    @Test
    @DisplayName("manageSubmissionsTP - calls serviceTP with correct args")
    void testManageSubmissionsTP_callsServiceTP() {

        Long courseId = 1L;
        int tpNo = 1;

        Course course = new Course();
        course.setId(courseId);

        Student student = new Student();
        student.setId(10L);
        course.getStudents().add(student);

        TP tp = new TP();
        tp.setId(5L);
        tp.setNo(tpNo);
        tp.setCourse(course);

        when(repositoryCourse.findById(courseId)).thenReturn(Optional.of(course));
        when(repositoryCourse.findTPByCourseIdAndNo(courseId, tpNo)).thenReturn(Optional.of(tp));

        TPDto tpDto = new TPDto(5L, tpNo, courseId, null, null); // adapte si ta signature est différente
        when(serviceTP.addTPStatusListToTP(tp, course.getStudents())).thenReturn(tpDto);

        TPDto result = serviceCourse.manageSubmissionsTP(courseId, tpNo);

        assertNotNull(result);
        assertEquals(tpDto, result);

        verify(repositoryCourse).findById(courseId);
        verify(repositoryCourse).findTPByCourseIdAndNo(courseId, tpNo);
        verify(serviceTP).addTPStatusListToTP(tp, course.getStudents());
    }


    @Test
    @DisplayName("getTPSubmissionRestructurated - TP found -> delegate to ServiceTP and return file")
    void testGetTPSubmissionRestructurated_found() {
        Long courseId = 1L;
        int tpNo = 1;

        Course course = new Course();
        course.setId(courseId);

        TP tp = new TP();
        tp.setId(5L);
        tp.setNo(tpNo);
        tp.setCourse(course);

        when(repositoryCourse.findTPByCourseIdAndNo(courseId, tpNo))
                .thenReturn(Optional.of(tp));

        File fakeFile = new File("target/test-zips/fake.zip");
        when(serviceTP.getSubmissionFileRestructurated(any(TPDto.class)))
                .thenReturn(fakeFile);

        File result = serviceCourse.getTPSubmissionRestructurated(courseId, tpNo);

        assertNotNull(result);
        assertEquals(fakeFile, result);

        verify(repositoryCourse).findTPByCourseIdAndNo(courseId, tpNo);
        verify(serviceTP).getSubmissionFileRestructurated(
                argThat(dto ->
                        dto.id().equals(5L)
                                && dto.courseId().equals(courseId)
                                && dto.no() == tpNo
                )
        );
    }




}
