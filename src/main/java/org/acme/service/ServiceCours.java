package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.acme.repository.*;
import org.acme.entity.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ServiceCours {
    @Inject
    CoursRepository coursRepository;

    @Inject
    EtudiantRepository etudiantRepository;

    @Inject
    TPRepository travailPratiqueRepository;

    @Inject
    ServiceTravailPratique serviceTravailPratique;

    @Inject
    ServiceRendu serviceRendu;

    @Inject
    @ConfigProperty(name = "zip-storage.path")
    String zipStoragePath;

    /**
     * Creation d'un cours
     * Le chemin vers un nouveau dépot pour les zips doit être créé
     */
    public void creerCours(String nom, String code, String semestre, int annee,
                           String typeCours){
        Cours cours = new Cours(nom, code, TypeSemestre.valueOf(semestre), annee, TypeCours.valueOf(typeCours));
        coursRepository.persist(cours);

        //Creation du repertoire servant de depot pour les zips grâce au code
        //On est dans un cours, donc le path est celui de base : "DocumentsZip/"
        creerDossierZip(code, zipStoragePath);

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

    /**
     * Methode permettant d'ajouter un TP à un cours en le créant
     */
    public void ajouterTP(Long idCours, int no) {
        try {
            Cours cours = coursRepository.findById(idCours);
            TravailPratique tp = new TravailPratique(no, cours, null);
            cours.travauxPratiques.add(tp);

            //Si un TP est créé, il faudra aussi y ajouter un dossier pour les rendus
            creerDossierZip("TP" + no, zipStoragePath + "/" + cours.code);

            coursRepository.persist(cours);

        }
        catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Methode permettant d'ajouter une évaluation à un cours en la créant : Examen
     */
    public void ajouterExamen(Long idCours, String nom, String date, String semestre) {
        try {
            Cours cours = coursRepository.findById(idCours);
            Evaluation examen = new Examen(nom, date, cours, null, TypeSemestre.valueOf(semestre));
            cours.evaluations.add(examen);

            //Creation du repertoire servant de depot pour les zips grâce au nom
            creerDossierZip(nom, zipStoragePath + "/" + cours.code);
            coursRepository.persist(cours);
        }
        catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Methode permettant d'ajouter une évaluation à un cours en la créant : Controle
     * Continu
     */
    public void addCC(Long idCours, String nom, String date, int coef, int no) {
        try {
            Cours cours = coursRepository.findById(idCours);
            Evaluation cc = new ControleContinu(nom, date, cours, null, coef, no);
            cours.evaluations.add(cc);

            //Creation du repertoire servant de depot pour les zips grâce au nom : /CC1
            creerDossierZip(nom + no, zipStoragePath + "/" + cours.code);

            coursRepository.persist(cours);
        }
        catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }


    //PRIVATE METHODS
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
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    /**
     * Methode permettant de lancer le traitement du rendu pour un TP
     */
    public void lancerTraitementRenduZip(Long idCours, Long idTp) throws IOException {
        Cours cours = coursRepository.findById(idCours);
        TravailPratique tp = travailPratiqueRepository.findById(idTp);

        serviceRendu.traitementRenduZip(cours, tp);
    }



}
