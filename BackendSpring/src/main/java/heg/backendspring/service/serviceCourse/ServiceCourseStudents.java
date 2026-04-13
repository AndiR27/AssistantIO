package heg.backendspring.service.serviceCourse;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Student;
import heg.backendspring.enums.StudyType;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.mapping.MapperStudent;
import heg.backendspring.models.CourseDto;
import heg.backendspring.models.StudentDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.repository.RepositoryStudent;
import heg.backendspring.service.ServiceStudent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing students in courses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCourseStudents {

    private final MapperStudent mapperStudent;
    private final MapperCourse mapperCourse;
    private final RepositoryCourse repositoryCourse;
    private final RepositoryStudent repositoryStudent;
    private final ServiceStudent serviceStudent;

    /**
     * Ajouter un student a un cours
     * 3 cas possibles : l'etudiant n'existe pas (on le cree), l'etudiant existe deja dans le cours (on ne fait rien),
     * ou l'etudiant existe, mais pas dans le cours (on l'ajoute au cours)
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    @Transactional
    public StudentDto addStudent(Long idCourse, StudentDto studentDto) {
        Course course = repositoryCourse.findById(idCourse)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + idCourse));

        // 1. Recuperer ou creer l'etudiant
        Student student = repositoryStudent.findStudentByEmail(studentDto.email())
                .orElseGet(() -> mapperStudent.toEntity(serviceStudent.addStudent(studentDto)));

        // 2. Etudiant deja inscrit -> rien a faire
        if (course.getStudents().contains(student)) {
            log.info("Student {} already enrolled in course {}", student.getEmail(), idCourse);
            return mapperStudent.tDto(student);
        }

        // 3. Ajouter l'etudiant au cours
        student.getStudentCourses().add(course);
        course.getStudents().add(student);

        Student saved = repositoryStudent.save(student);

        log.info("Added student {} ({}) to course {}", saved.getName(), saved.getEmail(), idCourse);
        return mapperStudent.tDto(saved);
    }

    /**
     * Methode permettant de recuperer un etudiant d'un cours par son id
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'READ')")
    public Optional<StudentDto> getStudentFromCourse(Long courseId, Long studentId) {
        return repositoryCourse.findStudentByCourseIdAndStudentId(courseId, studentId)
                .map(mapperStudent::tDto);
    }

    /**
     * Methode permettant d'ajouter une liste d'etudiants a un cours
     */
    @Transactional
    public CourseDto addAllStudendsToCourse(Long idCourse, List<StudentDto> studentDtoList) {
        for (StudentDto studentDto : studentDtoList) {
            StudentDto addedStudent = addStudent(idCourse, studentDto);
            log.info("Student {} added to course {}", addedStudent.email(), idCourse);
        }
        return mapperCourse.toDto(repositoryCourse.findById(idCourse).get());
    }

    /**
     * Methode permettant d'ajouter une liste d'etudiants a un cours
     * a partir d'un fichier TXT
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    @Transactional
    public List<StudentDto> addAllStudentsFromFile(Long idCourse, String[] data) {
        log.debug("Starting to add students for course id={} with file (number of values : {})", idCourse, data.length);
        List<StudentDto> studentDtoList = new ArrayList<>();
        for (String line : data) {
            String[] studentData = line.split(";");
            StudentDto studentDto = new StudentDto(
                    null,
                    studentData[0].trim(),
                    studentData[1].trim(),
                    StudyType.valueOf(studentData[2]),
                    new HashSet<>()
            );
            StudentDto addedStudent = addStudent(idCourse, studentDto);
            studentDtoList.add(addedStudent);
        }
        log.info("All students from file added to course id={}", idCourse);
        return studentDtoList;
    }

    /**
     * Methode permettant de recuperer tous les etudiants d'un cours
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    public List<StudentDto> getAllStudentsFromCourse(Long idCourse) {
        Set<Student> students = repositoryCourse.findStudentsByCourseId(idCourse);
        return students.stream().map(mapperStudent::tDto).toList();
    }

    /**
     * Methode permettant de supprimer un etudiant d'un cours (desinscription)
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    @Transactional
    public void removeStudentFromCourse(Long idCourse, Long idStudent) {
        Optional<Course> optCourse = repositoryCourse.findById(idCourse);
        Optional<Student> optStudent = repositoryStudent.findById(idStudent);

        if (optCourse.isEmpty() || optStudent.isEmpty()) {
            throw new EntityNotFoundException("Course or Student not found");
        }

        Course course = optCourse.get();
        Student student = optStudent.get();

        course.getStudents().remove(student);
        student.getStudentCourses().remove(course);

        repositoryStudent.save(student);
        log.info("Removed student {} from course {}", idStudent, idCourse);
    }
}
