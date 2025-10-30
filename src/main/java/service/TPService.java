package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import entity.*;
import mapping.*;
import models.*;
import repository.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

@ApplicationScoped
public class TPService {

    @Inject
    TPRepository travailPratiqueRepository;

    @Inject
    CourseRepository courseRepository;

    @Inject
    SubmissionService submissionService;

    @Inject
    TPStatusRepository tpStatusRepository;

    @Inject
    TPMapper tpMapper;
    @Inject
    StudentMapper studentMapper;

    @Inject
    @ConfigProperty(name = "zip-storage.path")
    String zipStoragePath;

    private static final Logger LOG = Logger.getLogger(TPService.class);
    /**
     * Récupérer un travail pratique depuis la base de données
     */
    public TP_DTO findTravailPratique(Long id) {
        return tpMapper.toDto(travailPratiqueRepository.findById(id));
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
    public TP_DTO addSubmissionToTP(TP_DTO tpDTO, InputStream zipFile){
        //Récupérer le TP
        TP tp = travailPratiqueRepository.findById(tpDTO.getId());
        //nom du fichier
        String nomFichier = "TP" + tp.no + "_RenduCyberlearn.zip";

        //chemin vers le fichier
        Path tpFolder = Paths.get(zipStoragePath, tp.course.code, "TP" + tp.no);

        //Chemin complet vers le fichier
        Path cheminVersZip = tpFolder.resolve(nomFichier);

        //Copier le stream
        try{
            if (zipFile == null) {
                LOG.info("Zip file is null");
            }
            Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e){
            LOG.error("Unable to copy zip file", e);
        }

        //Creation du rendu
        tp.submission = new Submission(nomFichier, cheminVersZip.toString()
                , null);
        travailPratiqueRepository.persist(tp);
        return tpMapper.toDto(tp);
    }

    /**
     * Méthode permettant de vérifier les rendus des étudiants et gérer la liste des
     * statuts des rendus pour un cours et un TP donné
     *
     * 1. Récupérer la liste des rendus pour un TP donné : utilisation de ServiceRendu
     * 2. Pour chaque étudiant de la liste, créer un TP_Status et l'ajouter à la liste
     * 3. Vérifier dans la liste des rendus et mettre à jour le statut du TP_Status
     * 4. Persister la liste des TP_Status
     */
    @Transactional
    public TP_DTO manageSubmissionsTP(CourseDTO courseDto, int tp_no){
        LOG.info("Gestion des rendus pour le TP numéro : " + tp_no + " du cours : " + courseDto.getCode());
        //Récupérer le TP
        TP tp = courseRepository.findTpByNo(courseDto.getId(), tp_no);
        List<String> rendus = submissionService.getStudentsSubmission(tp.submission);
        List<Student> etudiantsList = courseRepository.findEtudiantsInscrits(courseDto.getId());
        for(Student student : etudiantsList){
            //map du student
            LOG.info("Infos TpStatus : " + " TP numéro : " + tp.no + ", Student : " + student.name);
            TPStatus tpStatus = new TPStatus(student, tp, false);
            String nomEtudiantRefomated = student.name.replaceAll(" ", "").toLowerCase();
            if(rendus.contains(nomEtudiantRefomated)){
                LOG.info("Rendu etudiantRefomated : " + student.name);
                tpStatus.studentSubmission = true;
            }
            tp.addTPStatus(tpStatus);
        }
        travailPratiqueRepository.persist(tp);
        return tpMapper.toDto(tp);
    }


    public List<TPStatus> findByTP(Long id) {
        return tpStatusRepository.findByTP(id);
    }

    public File getSubmissionFileRestructurated(TP_DTO tp) {
        //Récupérer le TP
        TP travailPratique = travailPratiqueRepository.findById(tp.getId());
        if (travailPratique == null || travailPratique.submission == null) {
            LOG.error("TP or submission not found for id: " + tp.getId());
            return null;
        }

        //Chemin vers le fichier de rendu
        String cheminFichier = travailPratique.submission.pathFileStructured;
        Path path = Paths.get(cheminFichier);

        //Vérifier si le fichier existe
        if (!Files.exists(path)) {
            LOG.error("File does not exist at path: " + cheminFichier);
            return null;
        }

        //Retourner le fichier
        return path.toFile();
    }
}
