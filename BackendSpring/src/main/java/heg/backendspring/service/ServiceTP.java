package heg.backendspring.service;

import heg.backendspring.entity.*;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.models.TPDto;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private final MapperTP mapperTP;


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
     * Méthode permettant de créer la liste des status des TP pour chaque étudiant
     */
    @Transactional
    public TPDto addTPStatusListToTP(TP tp, Set<Student> students) {
        log.info("addTPStatusListToTP");
        List<String> renduEtudiants = serviceSubmission.getStudentsSubmission(tp.getSubmission());
        for (Student student : students) {
            //Créer le TPStatus pour chaque étudiant
            log.info("Infos TPStatus : TP {} - Étudiant : {}", tp.getNo(), student.getEmail());
            TPStatus tpStatus = new TPStatus(student, tp, false);
            String nomEtudiantRefomated = student.getName().replaceAll(" ", "").toLowerCase();
            //Vérifier si l'étudiant a rendu son TP
            if (renduEtudiants.contains(nomEtudiantRefomated)) {
                log.info("Student submission : {}", nomEtudiantRefomated);
                tpStatus.setStudentSubmission(true);
            }
            tp.getStatusStudents().add(tpStatus);
        }
        repositoryTP.save(tp);
        return mapperTP.toDto(tp);
    }


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
