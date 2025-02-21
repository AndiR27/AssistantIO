package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.repository.*;
import org.acme.entity.*;

@ApplicationScoped
public class ServiceCours {
    @Inject
    CoursRepository coursRepository;

    @Inject
    EtudiantRepository etudiantRepository;

    /**
     * Méthode permettant d'ajouter un étudiant spécifique à un cours
     */
    public void ajouterEtudiant(Long idCours, Long idEtudiant){
        try{
            Cours cours = coursRepository.findById(idCours);
            Etudiant etudiant = etudiantRepository.findById(idEtudiant);
            cours.ajouterEtudiant(etudiant);
            coursRepository.persist(cours);
        }
        catch(NotFoundException e) {
            ;
        }



    }

}
