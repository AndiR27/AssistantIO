package heg.backendspring.service;

import static heg.backendspring.utils.StudentNameUtils.*;
import heg.backendspring.entity.*;
import heg.backendspring.enums.StudentSubmissionType;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.mapping.MapperTPStatus;
import heg.backendspring.models.TPDto;
import heg.backendspring.models.TPStatusDto;
import heg.backendspring.repository.RepositoryTP;
import heg.backendspring.repository.RepositoryTPStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceTP {

    private final ServiceSubmission serviceSubmission;
    @Value("${zip-storage.path}")
    private String zipStoragePath;

    //==============================
    //       DEPENDANCES
    //==============================
    private final RepositoryTP repositoryTP;
    private final RepositoryTPStatus repositoryTPStatus;
    //private final ServiceCourse serviceCourse;
    private final ServiceTPStatus serviceTPStatus;


    private final MapperTP mapperTP;
    private final MapperTPStatus mapperTPStatus;


    //==============================
    //    CRUD TP METHODS
    //==============================

    /**
     * Trouver un TP par son id
     */
    public Optional<TPDto> findTP(Long id) {
        return repositoryTP.findById(id).map(mapperTP::toDto);
    }

    /**
     * Methode permettant d'ajouter un rendu au TP afin de pouvoir le stocker
     * On va utiliser un InputStream pour stocker le fichier zip : cela permet de ne pas
     * stocker le fichier en mémoire et de le stocker directement sur le disque
     * <p>
     * De plus, la couche service ne dois pas connaitre le protocole HTTP ou le format
     * multipart, c'est pourquoi on utilise un InputStream
     */
    @Transactional
    public TPDto addSubmissionToTP(Long idTP, InputStream zipFile) {
        Optional<TP> tpOpt = repositoryTP.findById(idTP);
        if (tpOpt.isPresent()) {
            TP tp = tpOpt.get();
            //nom du fichier
            String nomFichier = "TP" + tp.getNo() + "_RenduCyberlearn.zip";
            String codeCours = tp.getCourse().getCode();
            //chemin vers le fichier
            Path tpFolder = Paths.get(zipStoragePath, codeCours, "TP" + tp.getNo());

            //Chemin complet vers le fichier
            Path cheminVersZip = tpFolder.resolve(nomFichier);
            //Copier le stream
            try {
                if (zipFile == null) {
                    log.info("Zip file is null");
                }
                Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Unable to copy zip file", e);
            }

            //Creer le rendu
            tp.setSubmission(new Submission(nomFichier, cheminVersZip.toString()
                    , null));
            repositoryTP.save(tp);
            return mapperTP.toDto(tp);
        }
        return null;
    }

    /**
     * Méthode permettant de récupérer tous les TPStatus d'un TP
     */
    public List<TPStatusDto> getAllTPStatusByTPId(Long tpId) {
        Optional<TP> tpOpt = repositoryTP.findById(tpId);
        if (tpOpt.isPresent()) {
            TP tp = tpOpt.get();
            return tp.getStatusStudents().stream()
                    .map(mapperTPStatus::toDto)
                    .collect(Collectors.toList());
        }
        throw new EntityNotFoundException("TP not found with id: " + tpId);
    }

    /**
     * Méthode permettant de créer la liste des status des TP pour chaque étudiant
     */
    @Transactional
    public TPDto addTPStatusListToTP(TP tp, Set<Student> students) {

        List<String> studentsSubmission = serviceSubmission.getStudentsSubmission(tp.getSubmission());
        for (Student student : students) {
            //Créer le TPStatus pour chaque étudiant
            log.info("Infos TPStatus : TP {} - Étudiant : {}", tp.getNo(), student.getEmail());
            //Vérifier si le status existe déjà pour cet étudiant et ce TP via le service TPstatus
            //Si oui, on le récupère pour ne pas le dupliquer en base de données

            TPStatus tpStatus = serviceTPStatus.getOrCreateTPStatus(student, tp);
            //Si le status est deja existant (car ajouté à la main), on skip la suite
            if(tpStatus.getId() != null){
                continue;
            }
            //Vérifier si l'étudiant a rendu son TP
            if (matchesStudentNameInSubmissions(studentsSubmission, student.getName())) {
                //Mettre à jour le TPStatus avec le status DONE
                tpStatus.setStudentSubmission(StudentSubmissionType.DONE);
            } else {
                tpStatus.setStudentSubmission(StudentSubmissionType.NOT_DONE_MISSING);
            }

            //Associer le TPStatus au TP
            tp.getStatusStudents().add(tpStatus);
        }
        repositoryTP.save(tp);
        return mapperTP.toDto(tp);
    }

    /**
     * Méthode permettant de mettre à jour les status des TPStatus d'un TP
     * Permet un "refresh" des status en fonction des rendus sans créer de doublons de TPStatus
     */
//    @Transactional
//    public List<TPStatusDto> updateTPStatusListToTP(TP tp, Set<Student> students) {
//        List<String> studentsSubmission = serviceSubmission.getStudentsSubmission(tp.getSubmission());
//        for(Student student : students){
//            //TPStatusDto tpStatusDto = serviceTPStatus.checkExistingTPstatus(student, tp);
//            TPStatus tpStatus = serviceTPStatus.getOrCreateTPStatus(student, tp);
//            //Mettre à jour le status si l'étudiant a rendu son TP
//            if(checkStudentName(studentsSubmission, student.getName())) {
//                serviceTPStatus.updateTPStatus(tpStatus.getId(), StudentSubmissionType
//                        .DONE);
//            }
//            else{
//                serviceTPStatus.updateTPStatus(tpStatus.getId(), StudentSubmissionType
//                        .NOT_DONE_MISSING);
//            }
//            tp.getStatusStudents().add(tpStatus);
//        }
//        repositoryTP.save(tp);
//        return this.getAllTPStatusByTPId(tp.getId());
//    }

    /**
     * Méthode permettant de récupérer le fichier zip restructuré pour un TP donné
     */
    public File getSubmissionFileRestructurated(TPDto tpDto) {
        //Récupérer le TP
        TP tp = repositoryTP.findById(tpDto.id()).orElseThrow();
        //Récupérer le rendu
        Submission submission = tp.getSubmission();
        if (submission != null) {
            log.info("Path to restructurated file is not null : {}", submission.getPathFileStructured());
            Path pathFile = Paths.get(submission.getPathFileStructured());
            return pathFile.toFile();
        }
        return null;
    }

    /**
     * Méthode permettant de supprimer un TP par son id
     * Supprimer les status associés et surtout les fichiers sur le disque
     */
    @Transactional
    public void deleteTP(Long tpId) {
        TP tp = repositoryTP.findById(tpId)
                .orElseThrow(() -> new EntityNotFoundException("TP not found with id: " + tpId));

        Course course = tp.getCourse();

        // Supprimer le dossier du TP sur le disque
        deleteTpDirectoryIfExists(tp, course);

        // 2) Supprimer les éventuels statuts
        if (tp.getStatusStudents() != null && !tp.getStatusStudents().isEmpty()) {
            repositoryTPStatus.deleteAll(tp.getStatusStudents());
            tp.getStatusStudents().clear();
        }
        // Supprimer le TP lui-même
        repositoryTP.delete(tp);

        log.info("Deleted TP {} (id={}) from course {}",
                tp.getNo(),
                tp.getId(),
                (course != null ? course.getId() : null));
    }

    /**
     * Supprime le dossier physique du TP sur le disque, s'il existe.
     */
    private void deleteTpDirectoryIfExists(TP tp, Course course) {
        try {
            Path tpRoot = null;

            // Cas 1 : on a une submission avec un pathStorage → on en déduit le dossier TPx
            if (tp.getSubmission() != null && tp.getSubmission().getPathStorage() != null) {
                Path originalZip = Paths.get(tp.getSubmission().getPathStorage());
                tpRoot = originalZip.getParent(); // .../DocumentsZip/69-23/TP4
            } else if (course != null && course.getCode() != null) {
                // Cas 2 : pas de submission → on reconstruit le chemin
                // ex: DocumentsZip/69-23
                Path courseRoot = Paths.get(zipStoragePath, course.getCode());
                // ex: .../TP4
                tpRoot = courseRoot.resolve("TP" + tp.getNo());
            }

            if (tpRoot != null && Files.exists(tpRoot)) {
                FileSystemUtils.deleteRecursively(tpRoot);
                log.info("Deleted TP folder: {}", tpRoot);
            } else {
                log.info("No TP folder found for TP {} (resolved path: {})", tp.getId(), tpRoot);
            }
        } catch (IOException e) {
            log.error("Failed to delete TP folder for TP {}: {}", tp.getId(), e.getMessage(), e);
        }
    }




}
