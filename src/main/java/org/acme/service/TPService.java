package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.entity.TP;
import org.acme.mapping.TPMapper;
import org.acme.models.TP_DTO;
import org.acme.repository.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

@ApplicationScoped
public class TPService {

    @Inject
    TPRepository travailPratiqueRepository;

    @Inject
    SubmissionService submissionService;

    @Inject
    TPStatusRepository tpStatusRepository;

    @Inject
    TPMapper tpMapper;

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
            Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e){
            LOG.error("Unable to copy zip file", e);
        }

        //Creation du rendu
        Submission submission = new Submission(nomFichier, cheminVersZip.toString()
                , null);
        tp.submission = submission;
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
    public TP manageSubmissionsTP(Course course, TP tp,
                                  List<Student> etudiantsList){
        List<String> rendus = submissionService.getStudentsSubmission(tp.submission);

        for(Student student : etudiantsList){
            TPStatus tpStatus = new TPStatus(student, tp, false);
            String nomEtudiantRefomated = student.name.replaceAll(" ", "").toLowerCase();
            if(rendus.contains(nomEtudiantRefomated)){
                tpStatus.studentSubmission = true;
            }
            tp.addTPStatus(tpStatus);
        }
        travailPratiqueRepository.persist(tp);
        return tp;
    }


    public List<TPStatus> findByTP(Long id) {
        return tpStatusRepository.findByTP(id);
    }
}
