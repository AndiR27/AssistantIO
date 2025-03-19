package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.acme.mapping.CoursMapper;
import org.acme.mapping.EtudiantMapper;
import org.acme.mapping.EvaluationMapper;
import org.acme.mapping.TravailPratiqueMapper;
import org.acme.models.*;
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

    @Inject
    CoursMapper coursMapper;
    @Inject
    EtudiantMapper etudiantMapper;
    @Inject
    TravailPratiqueMapper tpMapper;
    @Inject
    EvaluationMapper evaluationMapper;
    @Inject
    ServiceEtudiant serviceEtudiant;

    /**
     * Creation d'un cours
     * Le chemin vers un nouveau dépot pour les zips doit être créé
     */
    public CoursDTO creerCours(CoursDTO coursDTO) {
        Cours cours = coursMapper.toEntity(coursDTO);
        //Cours cours = new Cours(nom, code, TypeSemestre.valueOf(semestre), annee, TypeCours.valueOf(typeCours));
        coursRepository.persist(cours);

        //Creation du repertoire servant de depot pour les zips grâce au code
        //On est dans un cours, donc le path est celui de base : "DocumentsZip/"
        creerDossierZip(cours.code, zipStoragePath);
        return coursMapper.toDto(cours);
    }

    /**
     * Methode permettant de retrouver un cours selon l'id
    **/
    public CoursDTO findCours(Long id) {
        Cours cours = coursRepository.findById(id);
        return coursMapper.toDto(cours);
    }
    /**
     * Méthode permettant d'ajouter un étudiant spécifique existant à un cours existant
     */
    public EtudiantDTO ajouterEtudiant(CoursDTO coursDTO, EtudiantDTO etudiantDTO) {

        try {
            // Récupérer le cours, ou lever une NotFoundException si non présent
            Cours cours = coursRepository.findByIdOptional(coursDTO.getId())
                    .orElseThrow(() -> new NotFoundException("Cours non trouvé (ID=" + coursDTO.getId() + ")"));

            Etudiant etudiant;
            if (etudiantDTO.getId() != null) {
                // Cas : l'étudiant existe déjà
                etudiant = etudiantRepository.findByIdOptional(etudiantDTO.getId())
                        .orElseThrow(() -> new NotFoundException("Étudiant non trouvé (ID=" + etudiantDTO.getId() + ")"));
            } else {
                // Cas : l'étudiant n'existe pas => on le crée
                EtudiantDTO etudiantDTO1 = serviceEtudiant.addEtudiant(etudiantDTO);
                etudiant = etudiantMapper.toEntity(etudiantDTO1);
            }
            // Établir la relation bidirectionnelle
            cours.addEtudiant(etudiant);
            etudiant.addCours(cours);

            // Persister en base
            coursRepository.persist(cours);
            //etudiantRepository.persistAndFlush(etudiant);

            return etudiantMapper.toDto(etudiant);

        } catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        catch (Exception e) {
            // Pour toute autre erreur imprévue
            System.out.print("Erreur inattendue lors de l'ajout de l'étudiant au cours" + e.getMessage());
        }

        return null;
    }

    /**
     * Methode permettant d'ajouter une liste d'étudiants à un cours
     * à partir d'un fichier TXT
     */
    @Transactional
    public void addAllStudentsFromFile(CoursDTO coursDTO, String[] data) {
        try {
            Cours cours = coursMapper.toEntity(coursDTO);
            //Cours cours = coursRepository.findById(idCours);
            List<Etudiant> nouveauxEtudiants = new ArrayList<>();
            for (String row : data) {
                String[] etudiantData = row.split(";");
                //Etudiant etudiant = new Etudiant(etudiantData[0], etudiantData[1], TypeEtude.valueOf(etudiantData[2]));
                EtudiantDTO etudiantDTO = new EtudiantDTO(null, etudiantData[0], etudiantData[1], TypeEtudeDTO.valueOf(etudiantData[2]), new ArrayList<>());
                EtudiantDTO etuAdded = serviceEtudiant.addEtudiant(etudiantDTO);
                ajouterEtudiant(coursDTO, etuAdded);

            }
        } catch (RuntimeException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Methode permettant de récupérer tous les étudiants inscrits à un cours
     */
    public List<EtudiantDTO> getEtudiantsInscrits(Long idCours) {
        //Récupérer la liste des étudiants inscrits à un cours avec le repo et convertir en liste de DTO
        List<Etudiant> etudiants = coursRepository.findById(idCours).etudiantsInscrits;
        return etudiants.stream()
                .map(etudiantMapper::toDto)
                .toList();
        //return coursRepository.findEtudiantsInscrits(idCours);
    }

    /**
     * Methode permettant d'ajouter un TP à un cours en le créant
     */
    public TravailPratiqueDTO ajouterTP(CoursDTO coursDTO, int no) {
        TravailPratiqueDTO tpDTO = null;
        try {
            Cours cours = coursRepository.findById(coursDTO.getId());
            TravailPratique tp = new TravailPratique(no, cours, null);
            cours.travauxPratiques.add(tp);
            tp.cours = cours;
            //travailPratiqueRepository.persist(tp);
            coursRepository.persist(cours);

            coursRepository.getEntityManager().flush();

            //Si un TP est créé, il faudra aussi y ajouter un dossier pour les rendus
            creerDossierZip("TP" + no, zipStoragePath + "/" + cours.code);

            tpDTO = tpMapper.toDto(tp);

        } catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return tpDTO;
    }

    /**
     * Methode permettant d'ajouter une évaluation à un cours en la créant : Examen
     */
    public ExamenDTO ajouterExamen(CoursDTO coursDTO, ExamenDTO examenDTO) {
        ExamenDTO examDTO = null;
        try {
            Cours cours = coursRepository.findById(coursDTO.getId());
            Examen examen = evaluationMapper.toEntityExamen(examenDTO);
            examen.cours = cours;
            //Evaluation examen = new Examen(nom, date, cours, null, TypeSemestre.valueOf(semestre));
            cours.evaluations.add(examen);
            coursRepository.persist(cours);
            coursRepository.getEntityManager().flush();

            //Creation du repertoire servant de depot pour les zips grâce au nom
            creerDossierZip(examen.nom, zipStoragePath + "/" + cours.code);
            examDTO = evaluationMapper.toDtoExamen(examen);

        } catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return examDTO;
    }

    /**
     * Methode permettant d'ajouter une évaluation à un cours en la créant : Controle
     * Continu
     */
    public ControleContinuDTO addCC(CoursDTO coursDTO, ControleContinuDTO ccDTO) {

        try {
            Cours cours = coursRepository.findById(coursDTO.getId());
            ControleContinu cc = evaluationMapper.toEntityControleContinu(ccDTO);

            cc.cours = cours;
            cours.evaluations.add(cc);
            coursRepository.persist(cours);
            coursRepository.getEntityManager().flush();

            //Creation du repertoire servant de depot pour les zips grâce au nom : /CC1
            creerDossierZip(cc.nom, zipStoragePath + "/" + cours.code);


            return evaluationMapper.toDtoControleContinu(cc);
        } catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return null;
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
                System.out.println("Erreur lors de la creation du dossier: " + e.getMessage());
            }
        } else {
            System.out.println("Le dossier" + nomDossier + " existe déjà");
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


    public List<CoursDTO> listCours() {
        //Récupérer la liste des cours avec le repo et convertir en liste de DTO avec stream
        return coursRepository.listAll().stream()
                .map(coursMapper::toDto)
                .toList();
    }
}
