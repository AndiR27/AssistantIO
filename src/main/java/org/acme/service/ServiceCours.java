package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.resource.spi.ConfigProperty;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.acme.repository.*;
import org.acme.entity.*;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ServiceCours {
    @Inject
    CoursRepository coursRepository;

    @Inject
    EtudiantRepository etudiantRepository;

    /**
     * Creation d'un cours
     * Le chemin vers un nouveau dépot pour les zips doit être créé
     */
    public void creerCours(String nom, String code, String semestre, int annee,
                           String typeCours){
        Cours cours = new Cours(nom, code, TypeSemestre.valueOf(semestre), annee, TypeCours.valueOf(typeCours));
        coursRepository.persist(cours);

        //Creation du repertoire servant de depot pour les zips grâce au code
        creerDossierZip(code);


    }
    /**
     * Methode permettant de créer un dossier pour stocker les zips
     */
    private void creerDossierZip(String nomDossier) {

    }

    /**
     * Méthode permettant d'ajouter un étudiant spécifique à un cours
     */
    public void ajouterEtudiant(Long idCours, Long idEtudiant) {
        try {
            Cours cours = coursRepository.findById(idCours);
            Etudiant etudiant = etudiantRepository.findById(idEtudiant);
            cours.addEtudiant(etudiant);
            etudiant.addCours(cours);
            coursRepository.persist(cours);
            etudiantRepository.persist(etudiant);
        } catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }


    }

    /**
     * Methode permettant d'ajouter une liste d'étudiants à un cours
     * à partir d'un fichier TXT
     */
    @Transactional
    public void addAllStudentsFromFile(Long idCours, String[] data) {
        try {
            Cours cours = coursRepository.findById(idCours);
            List<Etudiant> nouveauxEtudiants = new ArrayList<>();
            for (String row : data) {
                String[] etudiantData = row.split(";");
                Etudiant etudiant = new Etudiant(etudiantData[0], etudiantData[1], TypeEtude.valueOf(etudiantData[2]));
                cours.addEtudiant(etudiant);
                etudiant.addCours(cours);
                nouveauxEtudiants.add(etudiant);
                coursRepository.persist(cours);
                etudiantRepository.persist(nouveauxEtudiants);
            }
        } catch (RuntimeException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Methode permettant de récupérer tous les étudiants inscrits à un cours
     */
    public List<Etudiant> getEtudiantsInscrits(Long idCours){
        return coursRepository.findEtudiantsInscrits(idCours);
    }



}
