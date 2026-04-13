package heg.backendspring.service.serviceCourse;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Evaluation;
import heg.backendspring.mapping.MapperEvaluation;
import heg.backendspring.models.EvaluationDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.service.ServiceEvaluation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing evaluations in courses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCourseEvaluation {

    private final RepositoryCourse repositoryCourse;
    private final ServiceEvaluation serviceEvaluation;
    private final MapperEvaluation mapperEvaluation;

    /**
     * Methode permettant de recuperer les evaluations d'un cours
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    public List<EvaluationDto> getAllEvaluationsFromCourse(Long idCourse) {
        return serviceEvaluation.getAllEvaluationsFromCourse(idCourse);
    }

    /**
     * Methode permettant d'ajouter une evaluation a un cours (en la creant)
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    @Transactional
    public EvaluationDto addEvaluationToCourse(Long idCourse, EvaluationDto evaluationDto) {
        Optional<Course> courseOpt = repositoryCourse.findById(idCourse);
        if (courseOpt.isEmpty()) {
            throw new EntityNotFoundException("Course not found with id: " + idCourse);
        }
        Evaluation eval = mapperEvaluation.toEntity(evaluationDto);
        courseOpt.get().getEvaluations().add(eval);
        eval.setCourse(courseOpt.get());
        repositoryCourse.save(courseOpt.get());
        log.info("Added evaluation {} to course {}", evaluationDto.name(), idCourse);
        return mapperEvaluation.toDto(eval);
    }
}
