package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import entity.*;
import mapping.StudentMapper;
import models.StudentDTO;
import repository.StudentRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StudentService {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentMapper studentMapper;

    private static final Logger LOG = Logger.getLogger(StudentService.class);

    /**
     * Creation d'un etudiant : On laisse la responsabilité à ce
     * service de gérer la création d'un étudiant.
     */
    public StudentDTO addStudent(StudentDTO studentDTO, Course course){

        try {
            Student studentEntity = studentMapper.toEntity(studentDTO);
            // On associe l'étudiant au cours
            //studentEntity.addCours(course);
            studentRepository.persist(studentEntity);
            studentRepository.flush();
            LOG.info("Student created successfully: " + studentEntity.id + " - " + studentEntity.email);
            return studentMapper.toDto(studentEntity);
        } catch (ConstraintViolationException e) {
            LOG.error("Constraint violation while creating student: " + e.getMessage());

        }
        return null;
    }
}
