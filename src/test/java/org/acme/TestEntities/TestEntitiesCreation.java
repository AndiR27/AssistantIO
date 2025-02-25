package org.acme.TestEntities;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.repository.*;
import org.acme.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;


@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestEntitiesCreation {

    @Inject
    CoursRepository coursRepository;

    @Inject
    ServiceCours serviceCours;

    /**
     * JUnit créera un dossier temporaire unique pour chaque test,
     * et le supprimera (avec tout son contenu) une fois le test fini.
     */

    @TempDir
    Path tempDir;

    //Panache (et Hibernate) exigent une transaction active pour persist.
    @BeforeEach
    @Transactional
    void setUp(){
        coursRepository.deleteAll();
        //Préparer les données en créant un cours, un TP, et quelques étudiants
        //Ajouter les relations
        Cours c = new Cours("Approfondissement de la programmation", "62-21",
                TypeSemestre.Printemps, 2025, TypeCours.Java);
        TravailPratique tp = new TravailPratique(1, c, null);
        Etudiant e1 = new Etudiant("Scout Mark", "mark@hesge.ch", TypeEtude.temps_plein);
        Etudiant e2 = new Etudiant("Riggs Helly", "helly@hesge.ch", TypeEtude.temps_partiel);
        Etudiant e3 = new Etudiant("George Dylan", "dylan@hesge.ch", TypeEtude.temps_plein);
        Etudiant e4 = new Etudiant("Bailiff Irving", "irving@hesge.ch", TypeEtude.temps_plein);
        c.addEtudiant(e1);c.addEtudiant(e2);c.addEtudiant(e3);c.addEtudiant(e4);
        c.addTravailPratique(tp);
        coursRepository.persist(c);
    }

    @Test
    @Order(1)
    public void testSetup(){
        //Vérifier que les données ont bien été créées
        Assertions.assertEquals(1, coursRepository.count());
        Cours c = coursRepository.findAll().firstResult();
        Assertions.assertEquals(4, c.etudiantsInscrits.size());
        Assertions.assertEquals(1, c.travauxPratiques.size());
    }

    @Test
    @Order(2)
    void testCreateFile() throws IOException {
        // 1) Définir le chemin du fichier dans le dossier temporaire
        Path targetFile = tempDir.resolve("monFichier.txt");

        // 2) Appeler la méthode de ton code qui crée/écrit dans un fichier
        //    Ici, on fait un simple exemple d'écriture
        Files.writeString(targetFile, "Bonjour le monde !");

        // 3) Vérifier que le fichier est bien créé
        assert Files.exists(targetFile) : "Le fichier devrait exister !";

        // 4) Vérifier le contenu
        String content = Files.readString(targetFile);
        assert content.equals("Bonjour le monde !") : "Le contenu du fichier est incorrect";
    }

    @Test
    @Order(3)
    @Transactional
    public void testCreationCours(){
        //Créer un cours
        serviceCours.creerCours("Programmation collaborative", "63-21",
                String.valueOf(TypeSemestre.Automne), 2025, String.valueOf(TypeCours.Java));
        Assertions.assertEquals(2, coursRepository.count());
        Assertions.assertEquals("63-21",coursRepository.findAll().list().get(1).code);
        Cours c = coursRepository.findCoursByCode("63-21");
        Assertions.assertEquals("Programmation collaborative", c.nom);


    }
}
