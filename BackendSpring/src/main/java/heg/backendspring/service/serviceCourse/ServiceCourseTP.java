package heg.backendspring.service.serviceCourse;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.TP;
import heg.backendspring.exception.CourseErrorCode;
import heg.backendspring.exception.CourseException;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.models.SubmissionDto;
import heg.backendspring.models.TPDto;
import heg.backendspring.models.TPStatusDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.repository.RepositoryTP;
import heg.backendspring.service.ServiceSubmission;
import heg.backendspring.service.ServiceTP;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing TPs in courses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCourseTP {

    private final RepositoryCourse repositoryCourse;
    private final RepositoryTP repositoryTP;
    private final MapperTP mapperTP;
    private final ServiceTP serviceTP;
    private final ServiceSubmission serviceSubmission;
    private final ServiceCourseCore serviceCourseCore;

    /**
     * Methode permettant d'ajouter un TP a un cours en le creant
     * Si un TP est cree, il faudra aussi y ajouter un dossier pour les rendus
     *
     * @param no : numero du TP (pas l'id)
     */
    @Transactional
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    public TPDto addTPtoCourse(Long idCourse, int no) {
        Course course = repositoryCourse.findById(idCourse).get();
        // throw exception si le TP existe deja
        if (repositoryTP.findByCourseIdAndNo(idCourse, no).isPresent()) {
            throw new CourseException(
                    "Le TP" + no + "' est deja associe au cours.",
                    CourseErrorCode.TP_ALREADY_EXISTS,
                    course.getId()
            );
        }
        TP tp = new TP(no, course, null);
        course.getTps().add(tp);
        repositoryCourse.save(course);
        log.info("Added TP {} to course {}", no, course.getName());

        serviceCourseCore.creerDossierZip("TP" + no,
                serviceCourseCore.getZipStoragePath() + "/" + course.getCode());

        return mapperTP.toDto(tp);
    }

    /**
     * Methode permettant de recuperer tous les TPs d'un cours
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    public List<TPDto> getAllTPsFromCourse(Long idCourse) {
        return repositoryTP.findByCourseId(idCourse).stream().map(mapperTP::toDto).toList();
    }

    /**
     * Methode permettant de recuperer un TP d'un cours par son numero
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    public Optional<TPDto> findTPFromCourseByNo(Long idCourse, int no) {
        Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(idCourse, no);
        return tpOpt.map(mapperTP::toDto);
    }

    /**
     * Methode permettant de supprimer un TP d'un cours par son numero (et donc de la base)
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    @Transactional
    public void deleteTPFromCourseByNo(Long idCourse, int no) {
        Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(idCourse, no);
        if (tpOpt.isPresent()) {
            TP tp = tpOpt.get();
            Course course = tp.getCourse();
            course.getTps().remove(tp);
            repositoryCourse.save(course);
            log.info("Deleted TP {} from course {}", no, idCourse);
            // Delete the TP entity and manage folders if needed
            serviceTP.deleteTP(tp.getId());
        }
    }

    /**
     * Methode permettant de mettre a jour un TP d'un cours par son numero
     */
    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'WRITE')")
    @Transactional
    public Optional<TPDto> updateTPfromCourseByNo(Long courseId, Integer tpNumber, TPDto tpDto) {
        Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(courseId, tpNumber);
        Optional<Course> courseOpt = repositoryCourse.findById(courseId);
        if (tpOpt.isEmpty()) {
            return Optional.empty();
        }
        TP tp = tpOpt.get();
        // Mettre a jour l'entite via le mapper
        mapperTP.updateEntityFromDto(tpDto, tp);

        // Mettre a jour le cours qui possede le TP
        repositoryCourse.save(courseOpt.get());

        return Optional.of(mapperTP.toDto(tp));
    }

    /**
     * Methode permettant d'ajouter un rendu zip pour un TP d'un cours
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'WRITE')")
    @Transactional
    public SubmissionDto addSubmissionToTP(Long idCourse, int tpNo, InputStream zipFile) {
        Optional<Course> courseOpt = repositoryCourse.findById(idCourse);
        if (courseOpt.isEmpty()) {
            throw new EntityNotFoundException("Course not found with id: " + idCourse);
        } else {
            log.info("Adding submission to TP no={} for course id={}", tpNo, idCourse);
            Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(idCourse, tpNo);
            TPDto tpDto = serviceTP.addSubmissionToTP(tpOpt.get().getId(), zipFile);
            return tpDto.submission();
        }
    }

    /**
     * Methode permettant de lancer le traitement du rendu pour un TP
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCours, 'WRITE')")
    @Transactional
    public void startZipProcess(Long idCours, int idTp) throws IOException {
        Course course = repositoryCourse.findById(idCours)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + idCours));

        TP tp = repositoryTP.findByCourseIdAndNo(idCours, idTp)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TP number " + idTp + " not found for course id: " + idCours
                ));
        log.info("Starting zip process for course id={} tp no={}", idCours, idTp);
        serviceSubmission.processZipSubmissionForTP(course, tp);
    }

    /**
     * Methode permettant de verifier les rendus des etudiants et gerer la liste des
     * statuts des rendus pour un cours et un TP donne
     */
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    @Transactional
    public TPDto manageSubmissionsTP(Long idCourse, int tp_no) {
        log.info("Gestion des rendus pour le TP numero : {} du cours id : {}", tp_no, idCourse);
        // Use findByIdWithStudents to avoid LazyInitializationException
        Optional<Course> course = repositoryCourse.findByIdWithStudents(idCourse);
        if (course.isEmpty()) {
            throw new EntityNotFoundException("Course not found with id: " + idCourse);
        }

        // Recuperer le TP
        Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(idCourse, tp_no);
        if (tpOpt.isEmpty()) {
            log.info("not found");
            throw new EntityNotFoundException("TP not found for course id=" + idCourse + " and tp no=" + tp_no);
        }
        return serviceTP.addTPStatusListToTP(tpOpt.get(), course.get().getStudents());
    }

    /**
     * Methode permettant de recuperer un fichier zip restructure des rendus d'un TP
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    public File getTPSubmissionRestructurated(Long idCourse, int tp_no) {
        Optional<TPDto> tpDtoOpt = findTPFromCourseByNo(idCourse, tp_no);
        if (tpDtoOpt.isPresent()) {
            return serviceTP.getSubmissionFileRestructurated(tpDtoOpt.get());
        }
        log.error("TP not found for course id={} tp no={}", idCourse, tp_no);
        return null;
    }

    /**
     * Methode permettant de recuperer tous les TPStatus d'un TP d'un cours
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#idCourse, 'READ')")
    public List<TPStatusDto> getAllTPStatusFromTP(Long idCourse, int tp_no) {
        Optional<Course> courseOpt = repositoryCourse.findById(idCourse);
        if (courseOpt.isEmpty()) {
            throw new EntityNotFoundException("Course not found with id: " + idCourse);
        }
        Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(idCourse, tp_no);
        if (tpOpt.isEmpty()) {
            throw new EntityNotFoundException("TP not found for course id=" + idCourse + " and tp no=" + tp_no);
        } else {
            TP tp = tpOpt.get();
            return serviceTP.getAllTPStatusByTPId(tp.getId());
        }
    }

    /**
     * Methode permettant de lancer la mise a jour des statuts des rendus d'un TP d'un cours
     */
    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'WRITE')")
    @Transactional
    public List<TPStatusDto> refreshTPStatusesFromTP(Long courseId, Integer tpNumber) {
        // Use findByIdWithStudents to avoid LazyInitializationException
        Optional<Course> courseOpt = repositoryCourse.findByIdWithStudents(courseId);
        if (courseOpt.isEmpty()) {
            throw new EntityNotFoundException("Course not found with id: " + courseId);
        }
        Optional<TP> tpOpt = repositoryTP.findByCourseIdAndNo(courseId, tpNumber);
        if (tpOpt.isEmpty()) {
            throw new EntityNotFoundException("TP not found for course id=" + courseId + " and tp no=" + tpNumber);
        }
        TP tp = tpOpt.get();
        log.info("Refreshing TPStatuses for course id={} tp no={}", courseId, tpNumber);
        serviceTP.addTPStatusListToTP(tp, courseOpt.get().getStudents());
        return serviceTP.getAllTPStatusByTPId(tp.getId());
    }
}
