package heg.backendspring.service;


import heg.backendspring.entity.Course;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.models.CourseDto;
import heg.backendspring.repository.RepositoryCourse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCourse {

    @Value("${zip-storage.path}")
    private String zipStoragePath;


    //==============================
    //       DEPENDANCES
    //==============================
    private final MapperCourse mapperCourse;
    private final RepositoryCourse repositoryCourse;


    //==============================
    //    CRUD COURSE METHODS
    //==============================

    //Trouver un cours par son id
    public Optional<CourseDto> findCourseById(Long courseId) {
        return repositoryCourse.findById(courseId)
                .map(mapperCourse::toDto);
    }

    //Trouver tous les cours
    public List<CourseDto> findAllCourses() {
        return repositoryCourse.findAll()
                .stream()
                .map(mapperCourse::toDto)
                .toList();
    }

    //Ajouter un cours
    @Transactional
    public CourseDto addCourse(CourseDto courseDto){
        Course c = mapperCourse.toEntity(courseDto);
        Course savedCourse = repositoryCourse.save(c);
        log.info("Added course with id: {}", savedCourse.getId());

        //Creation du repertoire servant de depot pour les zips grâce au code
        //On est dans un cours, donc le path est celui de base : "DocumentsZip/"
        creerDossierZip(savedCourse.getCode(), zipStoragePath);

        return mapperCourse.toDto(savedCourse);
    }


    //==============================
    //       PRIVATE METHODS
    //==============================
    /**
     * Methode permettant de créer un dossier pour stocker les zips
     */
    private void creerDossierZip(String nomDossier, String path) {
        Path parentPath = Paths.get(path);

        Path newDirectory = parentPath.resolve(nomDossier);
        if (Files.notExists(newDirectory)) {
            try {
                Files.createDirectory(newDirectory);
            } catch (Exception e) {
                log.error("Failed to create directory {}\n--> {}", newDirectory, e.getMessage());
            }
        } else {
            log.info("Folder {} already exists", nomDossier);
        }
    }

}
