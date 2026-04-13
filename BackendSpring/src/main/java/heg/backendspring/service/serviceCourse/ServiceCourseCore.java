package heg.backendspring.service.serviceCourse;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Student;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.models.CourseDto;
import heg.backendspring.repository.RepositoryCourse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Service for Course CRUD operations and folder management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCourseCore {

    @Value("${zip-storage.path}")
    private String zipStoragePath;

    private final MapperCourse mapperCourse;
    private final RepositoryCourse repositoryCourse;

    /**
     * Trouver un cours par son id
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'READ')")
    public Optional<CourseDto> findCourseById(Long courseId) {
        return repositoryCourse.findById(courseId)
                .map(mapperCourse::toDto);
    }

    /**
     * Trouver tous les cours
     */
    @Transactional(readOnly = true)
    public List<CourseDto> findAllCourses() {
        return repositoryCourse.findAll()
                .stream()
                .map(mapperCourse::toDto)
                .toList();
    }

    /**
     * Trouver tous les cours par une liste d'id
     */
    @Transactional(readOnly = true)
    public List<CourseDto> findAllById(List<Long> ids) {
        return repositoryCourse.findAllById(ids)
                .stream()
                .map(mapperCourse::toDto)
                .toList();
    }

    /**
     * Ajouter un cours
     */
    @Transactional
    public CourseDto addCourse(CourseDto courseDto) {
        Course c = mapperCourse.toEntity(courseDto);
        Course savedCourse = repositoryCourse.save(c);
        log.info("Added course with id: {}", savedCourse.getId());

        // Creation du repertoire servant de depot pour les zips grace au code
        creerDossierZip(savedCourse.getCode(), zipStoragePath);

        return mapperCourse.toDto(savedCourse);
    }

    /**
     * Mettre à jour un cours
     */
    @PreAuthorize("@courseSecurity.hasAccess(#courseDto.id(), 'OWNER')")
    @Transactional
    public Optional<CourseDto> updateCourse(CourseDto courseDto) {
        Optional<Course> courseOpt = repositoryCourse.findById(courseDto.id());
        if (courseOpt.isEmpty()) {
            return Optional.empty();
        }

        Course course = courseOpt.get();
        // Gerer le renommage du dossier si le code a change
        manageFoldersOnUpdate(course.getCode(), courseDto.code());
        // Mettre a jour l'entite via le mapper
        mapperCourse.updateEntity(courseDto, course);

        Course updated = repositoryCourse.save(course);
        log.info("Updated course id={} ({})", updated.getId(), updated.getCode());

        return Optional.of(mapperCourse.toDto(updated));
    }

    /**
     * Supprimer un cours
     */
    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = repositoryCourse.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        // relation bidirectionnelle Course <-> Student
        for (Student student : new HashSet<>(course.getStudents())) {
            student.getStudentCourses().remove(course);
        }
        course.getStudents().clear();
        repositoryCourse.deleteById(courseId);
        log.info("Deleted course with id: {}", courseId);
    }

    /**
     * Recuperer une entite Course par son id (usage interne entre services).
     */
    public Optional<Course> findCourseEntityById(Long courseId) {
        return repositoryCourse.findById(courseId);
    }

    /**
     * Sauvegarder une entite Course (usage interne entre services).
     */
    public Course saveCourse(Course course) {
        return repositoryCourse.save(course);
    }

    /**
     * Valide qu'un chemin reste bien dans le repertoire de base autorise.
     * Protection contre les attaques de type path traversal.
     */
    public boolean isPathSafe(Path path, Path allowedBase) {
        Path normalizedPath = path.normalize().toAbsolutePath();
        Path normalizedBase = allowedBase.normalize().toAbsolutePath();
        return normalizedPath.startsWith(normalizedBase);
    }

    /**
     * Methode permettant de creer un dossier pour stocker les zips
     */
    public void creerDossierZip(String nomDossier, String path) {
        Path parentPath = Paths.get(path);
        Path newDirectory = parentPath.resolve(nomDossier).normalize();

        // Anti path traversal
        Path baseStorage = Paths.get(zipStoragePath);
        if (!isPathSafe(newDirectory, baseStorage)) {
            log.error("Path traversal attempt detected for folder: {}", nomDossier);
            throw new IllegalArgumentException("Invalid folder name");
        }

        if (Files.notExists(newDirectory)) {
            try {
                Files.createDirectories(newDirectory);
            } catch (Exception e) {
                log.error("Failed to create directory {}\n--> {}", newDirectory, e.getMessage());
            }
        } else {
            log.info("Folder {} already exists", nomDossier);
        }
    }

    /**
     * Permet de gerer les dossiers lors de la mise a jour d'un cours
     */
    private void manageFoldersOnUpdate(String oldCode, String newCode) {
        if (!oldCode.equals(newCode)) {
            Path baseStorage = Paths.get(zipStoragePath);
            Path oldPath = Paths.get(zipStoragePath, oldCode).normalize();
            Path newPath = Paths.get(zipStoragePath, newCode).normalize();

            // Anti path traversal
            if (!isPathSafe(oldPath, baseStorage) || !isPathSafe(newPath, baseStorage)) {
                log.error("Path traversal attempt detected: {} -> {}", oldCode, newCode);
                throw new IllegalArgumentException("Invalid course code");
            }

            try {
                Files.move(oldPath, newPath);
                log.info("Renamed folder from {} to {}", oldCode, newCode);
            } catch (IOException e) {
                log.error("Failed to rename folder from {} to {}\n--> {}", oldCode, newCode, e.getMessage());
            }
        }
    }

    public String getZipStoragePath() {
        return zipStoragePath;
    }
}
