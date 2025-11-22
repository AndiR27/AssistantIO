package heg.backendspring.service;

import heg.backendspring.entity.Student;
import heg.backendspring.entity.Submission;
import heg.backendspring.entity.TP;
import heg.backendspring.entity.TPStatus;
import heg.backendspring.mapping.MapperStudent;
import heg.backendspring.mapping.MapperTP;
import heg.backendspring.models.TPDto;
import heg.backendspring.repository.RepositoryStudent;
import heg.backendspring.repository.RepositoryTP;
import heg.backendspring.repository.RepositoryTPStatus;
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
    private final ServiceCourse serviceCourse;

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
     *
     * De plus, la couche service ne dois pas connaitre le protocole HTTP ou le format
     * multipart, c'est pourquoi on utilise un InputStream
     */
    @Transactional
    public TPDto addSubmissionToTP(Long idTP, InputStream zipFile){
        Optional<TPDto> tpDtoOptional = findTP(idTP);
        if(tpDtoOptional.isPresent()){
            TPDto tpDto = tpDtoOptional.get();
            //nom du fichier
            String nomFichier = "TP" + tpDto.no() + "_RenduCyberlearn.zip";
            String codeCours = this.serviceCourse.findCourseById(tpDto.courseId()).get().code();
            //chemin vers le fichier
            Path tpFolder = Paths.get(zipStoragePath, codeCours, "TP" + tpDto.no());

            //Chemin complet vers le fichier
            Path cheminVersZip = tpFolder.resolve(nomFichier);
            //Copier le stream
            try{
                if (zipFile == null) {
                    log.info("Zip file is null");
                }
                Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e){
                log.error("Unable to copy zip file", e);
            }

            //Creer le rendu
            TP tp = mapperTP.toEntity(tpDto);
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
        List<String> renduEtudiants = serviceSubmission.getStudentsSubmission(tp.getSubmission());
        for( Student student : students){
            //Créer le TPStatus pour chaque étudiant
            log.info("Infos TPStatus : TP {} - Étudiant : {}", tp.getNo(), student.getEmail());
            TPStatus tpStatus = new TPStatus(student, tp, false);
            String nomEtudiantRefomated = student.getName().replaceAll(" ", "").toLowerCase();
            //Vérifier si l'étudiant a rendu son TP
            if(renduEtudiants.contains(nomEtudiantRefomated)){
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
        if(submission != null){
            Path pathFile = Paths.get(submission.getPathFileStructured());
            return pathFile.toFile();
        }
        return null;
    }


}
