package heg.backendspring.service;


import heg.backendspring.entity.Course;
import heg.backendspring.entity.Student;
import heg.backendspring.entity.TP;
import heg.backendspring.enums.StudyType;
import heg.backendspring.exception.CourseErrorCode;
import heg.backendspring.exception.CourseException;
import heg.backendspring.mapping.MapperCourse;
import heg.backendspring.mapping.MapperStudent;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.models.CourseDto;
import heg.backendspring.models.StudentDto;
import heg.backendspring.models.SubmissionDto;
import heg.backendspring.models.TPDto;
import heg.backendspring.repository.RepositoryCourse;
import heg.backendspring.repository.RepositoryStudent;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private final MapperStudent mapperStudent;
    private final RepositoryCourse repositoryCourse;
    private final ServiceStudent serviceStudent;
    private final RepositoryStudent repositoryStudent;
    private final MapperTP mapperTP;
    private final ServiceSubmission serviceSubmission;
    private final ServiceTP serviceTP;


    //==============================
    //    CRUD COURSE METHODS
    //==============================

    /**
     * Trouver un cours par son id
     */
    public Optional<CourseDto> findCourseById(Long courseId) {
        return repositoryCourse.findById(courseId)
                .map(mapperCourse::toDto);
    }

    /**
     * Trouver tous les cours
     */
    public List<CourseDto> findAllCourses() {
        return repositoryCourse.findAll()
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

        //Creation du repertoire servant de depot pour les zips grâce au code
        //On est dans un cours, donc le path est celui de base : "DocumentsZip/"
        creerDossierZip(savedCourse.getCode(), zipStoragePath);

        return mapperCourse.toDto(savedCourse);
    }

    /**
     * Mettre à jour un cours
     */
    @Transactional
    public Optional<CourseDto> updateCourse(CourseDto courseDto) {
        Optional<Course> courseOpt = repositoryCourse.findById(courseDto.id());
        if (courseOpt.isEmpty()) {
            return Optional.empty();
        }

        Course course = courseOpt.get();
        // Mettre à jour l’entité via le mapper
        mapperCourse.updateEntity(courseDto, course);

        // Sauvegarder
        Course updated = repositoryCourse.save(course);
        log.info("Updated course id={} ({})", updated.getId(), updated.getCode());

        return Optional.of(mapperCourse.toDto(updated));
    }

    /**
     * Supprimer un cours
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        repositoryCourse.deleteById(courseId);
        log.info("Deleted course with id: {}", courseId);
    }



    /**
     * Ajouter un student à un cours
     * 3 cas possibles : l'étudiant n'existe pas (on le crée), l'étudiant existe déjà dans le cours (on ne fait rien),
     * ou l'étudiant existe, mais pas dans le cours (on l'ajoute au cours
     */
    @Transactional
    public StudentDto addStudent(Long idCourse, StudentDto studentDto) {
        Course course = repositoryCourse.findById(idCourse)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + idCourse));

        // 1. Récupérer ou créer l'étudiant
        Student student = repositoryStudent.findStudentByEmail(studentDto.email())
                .orElseGet(() -> mapperStudent.toEntity(serviceStudent.addStudent(studentDto)));

        // 2. Étudiant déjà inscrit → rien à faire
        if (course.getStudents().contains(student)) {
            log.info("Student {} already enrolled in course {}", student.getEmail(), idCourse);
            return mapperStudent.tDto(student);
        }

        // 3. Ajouter l'étudiant au cours
        student.getStudentCourses().add(course);
        course.getStudents().add(student);

        Student saved = repositoryStudent.save(student);

        log.info("Added student {} ({}) to course {}", saved.getName(), saved.getEmail(), idCourse);
        return mapperStudent.tDto(saved);
    }

    /**
     * Methode permettant de récupérer un étudiant d'un cours par son id
     */
    public Optional<StudentDto> getStudentFromCourse(Long courseId, Long studentId) {
        return repositoryCourse.findStudentByCourseIdAndStudentId(courseId, studentId)
                .map(mapperStudent::tDto);
    }
    /**
     * Methode permettant d'ajouter une liste d'étudiants à un cours
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
     * Methode permettant d'ajouter une liste d'étudiants à un cours
     * à partir d'un fichier TXT
     */
    @Transactional
    public List<StudentDto> addAllStudentsFromFile(Long idCourse, String[] data) {
        log.debug("Starting to add studends for course id={} with file (number of values : {})", idCourse, data.length);
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
     * Methode permettant de récupérer tous les étudiants d'un cours
     */
    public List<StudentDto> getAllStudentsFromCourse(Long idCourse) {
        Set<Student> students = repositoryCourse.findStudentsByCourseId(idCourse);
        return students.stream().map(mapperStudent::tDto).toList();
    }

    /**
     * Methode permettant de supprimer un étudiant d'un cours (désinscription)
     */
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


    /**
     * Methode permettant d'ajouter un TP à un cours en le créant
     * Si un TP est créé, il faudra aussi y ajouter un dossier pour les rendus
     *
     * @param no : numéro du TP (pas l'id)
     */
    public TPDto addTPtoCourse(Long idCourse, int no) {
        Course course = repositoryCourse.findById(idCourse).get();
        //throw exception si le TP existe déjà
        if(repositoryCourse.findTPByCourseIdAndNo(idCourse, no).isPresent()){
            throw new CourseException(
                    "Le TP" + no + "' est déjà associé au cours.",
                    CourseErrorCode.TP_ALREADY_EXISTS,
                    course.getId()
            );
        }
        TP tp = new TP(no, course, null);
        course.getTps().add(tp);
        repositoryCourse.save(course);
        log.info("Added TP {} to course {}", no, course.getName());

        creerDossierZip("TP" + no, zipStoragePath + "/" + course.getCode());

        return mapperTP.toDto(tp);
    }

    /**
     * Methode permettant de récupérer tous les TPs d'un cours
     */
    public List<TPDto> getAllTPsFromCourse(Long idCourse) {
        return repositoryCourse.findAllTPsByCourseId(idCourse).stream().map(mapperTP::toDto).toList();
    }

    /**
     * Methode permettant de récupérer un TP d'un cours par son numéro
     */
    public Optional<TPDto> findTPFromCourseByNo(Long idCourse, int no) {
        Optional<TP> tpOpt = repositoryCourse.findTPByCourseIdAndNo(idCourse, no);
        return tpOpt.map(mapperTP::toDto);
    }

    /**
     * Methode permettant de supprimer un TP d'un cours par son numéro (et donc de la base)
     */
    @Transactional
    public void deleteTPFromCourseByNo(Long idCourse, int no) {
        Optional<TP> tpOpt = repositoryCourse.findTPByCourseIdAndNo(idCourse, no);
        if (tpOpt.isPresent()) {
            TP tp = tpOpt.get();
            Course course = tp.getCourse();
            course.getTps().remove(tp);
            repositoryCourse.save(course);
            log.info("Deleted TP {} from course {}", no, idCourse);
            //Delete the TP entity and manage folders if needed
            serviceTP.deleteTP(tp.getId());
        }
    }

    /**
     * Methode permettant de mettre à jour un TP d'un cours par son numéro
     */
    @Transactional
    public Optional<TPDto> updateTPfromCourseByNo(Long courseId, Integer tpNumber, TPDto tpDto) {
        Optional<TP> tpOpt = repositoryCourse.findTPByCourseIdAndNo(courseId, tpNumber);
        Optional<Course> courseOpt = repositoryCourse.findById(courseId);
        if (tpOpt.isEmpty()) {
            return Optional.empty();
        }
        TP tp = tpOpt.get();
        // Mettre à jour l’entité via le mapper
        mapperTP.updateEntityFromDto(tpDto, tp);

        // Mettre à jour le cours qui possède le TP
        repositoryCourse.save(courseOpt.get());

        return Optional.of(mapperTP.toDto(tp));

    }

    //TODO : Méthodes pour gérer les évaluations, exams...

    /**
     * Méthode permettant d'ajouter un rendu zip pour un TP d'un cours
     */
    @Transactional
    public SubmissionDto addSubmissionToTP(Long idCourse, int tpNo, InputStream zipFile) {
        Optional<Course> courseOpt = repositoryCourse.findById(idCourse);
        if (courseOpt.isEmpty()) {
            throw new EntityNotFoundException("Course not found with id: " + idCourse);
        }
        else{
            log.info("Adding submission to TP no={} for course id={}", tpNo, idCourse);
            Optional<TP> tpOpt = repositoryCourse.findTPByCourseIdAndNo(idCourse, tpNo);
            TPDto tpDto = serviceTP.addSubmissionToTP(tpOpt.get().getId(), zipFile);
            return tpDto.submission();
        }

    }

    /**
     * Méthode permettant de lancer le traitement du rendu pour un TP
     */
    @Transactional
    public void startZipProcess(Long idCours, int idTp) throws IOException {
        Course course = repositoryCourse.findById(idCours)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + idCours));

        TP tp = repositoryCourse.findTPByCourseIdAndNo(idCours, idTp)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TP number " + idTp + " not found for course id: " + idCours
                ));
        log.info("Starting zip process for course id={} tp no={}", idCours, idTp);
        serviceSubmission.processZipSubmission(course, tp);

    }

    /**
     * Méthode permettant de vérifier les rendus des étudiants et gérer la liste des
     * statuts des rendus pour un cours et un TP donné
     * <p>
     * 1. Récupérer la liste des rendus pour un TP donné : utilisation de ServiceRendu
     * 2. Pour chaque étudiant de la liste, créer un TP_Status et l'ajouter à la liste
     * 3. Vérifier dans la liste des rendus et mettre à jour le statut du TP_Status
     * 4. Persister la liste des TP_Status
     */
    @Transactional
    public TPDto manageSubmissionsTP(Long idCourse, int tp_no) {
        log.info("Gestion des rendus pour le TP numéro : {} du cours id : {}", tp_no, idCourse);
        Optional<Course> course = repositoryCourse.findById(idCourse);
        if(course.isEmpty()){
            throw new EntityNotFoundException("Course not found with id: " + idCourse);
        }

        //Récupérer le TP
        Optional<TP> tpOpt = repositoryCourse.findTPByCourseIdAndNo(idCourse, tp_no);
        if(tpOpt.isEmpty()){
            log.info("not found");
            throw new EntityNotFoundException("TP not found for course id=" + idCourse + " and tp no=" + tp_no);
        }
        return serviceTP.addTPStatusListToTP(tpOpt.get(), course.get().getStudents());
    }

    /**
     * Méthode permettant de récupérer un fichier zip restructuré des rendus d'un TP
     */
    public File getTPSubmissionRestructurated(Long idCourse, int tp_no) {
        Optional<TPDto> tpDtoOpt = findTPFromCourseByNo(idCourse, tp_no);
        if (tpDtoOpt.isPresent()) {
            return serviceTP.getSubmissionFileRestructurated(tpDtoOpt.get());
        }
        log.error("TP not found for course id={} tp no={}", idCourse, tp_no);
        return null;
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
                Files.createDirectories(newDirectory);
            } catch (Exception e) {
                log.error("Failed to create directory {}\n--> {}", newDirectory, e.getMessage());
            }
        } else {
            log.info("Folder {} already exists", nomDossier);
        }
    }
}


