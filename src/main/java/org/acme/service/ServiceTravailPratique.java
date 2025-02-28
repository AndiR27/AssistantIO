package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.entity.TravailPratique;
import org.acme.repository.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

@ApplicationScoped
public class ServiceTravailPratique {

    @Inject
    TPRepository travailPratiqueRepository;

    @Inject

    ServiceRendu serviceRendu;

    @Inject
    @ConfigProperty(name = "zip-storage.path")
    String zipStoragePath;


    /**
     * Methode permettant d'ajouter un rendu au TP afin de pouvoir le stocker
     * On va utiliser un InputStream pour stocker le fichier zip : cela permet de ne pas
     * stocker le fichier en mémoire et de le stocker directement sur le disque
     *
     * De plus, la couche service ne dois pas connaitre le protocole HTTP ou le format
     * multipart, c'est pourquoi on utilise un InputStream
     */

    @Transactional
    public void creerRenduTP(TravailPratique tp, InputStream zipFile){
        //nom du fichier
        String nomFichier = "TP" + tp.no + "_RenduCyberlearn.zip";

        //chemin vers le fichier
        Path tpFolder = Paths.get(zipStoragePath, tp.cours.code, "TP" + tp.no);

        //Chemin complet vers le fichier
        Path cheminVersZip = tpFolder.resolve(nomFichier);

        //Copier le stream
        try{
            Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e){
            throw new RuntimeException("Impossible de copier le fichier zip");
        }

        //Creation du rendu
        Rendu rendu = new Rendu(nomFichier, cheminVersZip.toString()
                , null);
        tp.rendu = rendu;
        travailPratiqueRepository.persist(tp);
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
    public void gestionRendusTP(Cours cours, TravailPratique tp,
                                List<Etudiant> etudiantsList){
        List<String> rendus = serviceRendu.getListRendus(tp.rendu);

        for(Etudiant etudiant : etudiantsList){
            TP_Status tpStatus = new TP_Status(etudiant, tp, false);
            String nomEtudiantRefomated = etudiant.nom.replaceAll(" ", "").toLowerCase();
            if(rendus.contains(nomEtudiantRefomated)){
                tpStatus.renduEtudiant = true;
            }
            tp.addTPStatus(tpStatus);
        }
        travailPratiqueRepository.persist(tp);
    }


}
