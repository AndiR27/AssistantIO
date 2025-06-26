package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.acme.entity.Student;
import org.acme.mapping.StudentMapper;
import org.acme.models.StudentDTO;
import org.acme.repository.StudentRepository;
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
    public StudentDTO addEtudiant(StudentDTO studentDTO){

        try {
            Student studentEntity = studentMapper.toEntity(studentDTO);
            studentRepository.persist(studentEntity);

            LOG.info("Student created successfully: " + studentDTO.getId());
            return studentMapper.toDto(studentEntity);
        } catch (ConstraintViolationException e) {
            LOG.error("Constraint violation while creating student: " + e.getMessage());

        }
        return null;
    }
}
