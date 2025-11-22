package heg.backendspring.service;

import heg.backendspring.entity.Student;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.mapping.MapperStudent;
import heg.backendspring.models.StudentDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.repository.RepositoryStudent;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceStudent {

    //==============================
    //       DEPENDANCES
    //==============================
    private final MapperStudent mapperStudent;
    private final RepositoryStudent repositoryStudent;


    //==============================
    //    CRUD STUDENT METHODS
    //==============================
    /**
     * Creation d'un étudiant : c'est le service qui gère cela
     */
    public StudentDto addStudent(StudentDto studentDto){
        try {
            Student student = repositoryStudent.save(mapperStudent.toEntity(studentDto));
            log.info("Created student {} ({}) with id: {}", studentDto.name(), studentDto.email(), student.getId());
            return mapperStudent.tDto(student);
        } catch (ConstraintViolationException e) {
            log.error("Constraint violation while creating student: {}", e.getMessage());
        }
        catch(EntityExistsException e){
            log.error("Entity exists while creating student: {}", e.getMessage());
        }
        return studentDto;
    }
}
