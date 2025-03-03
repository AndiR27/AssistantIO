package org.acme.TestEntities;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.repository.*;
import org.acme.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;


@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestServices {

    @Inject
    CoursRepository coursRepository;
    @Inject
    TPRepository travailPratiqueRepository;

    @Inject
    ServiceCours serviceCours;
    @Inject
    EtudiantRepository etudiantRepository;
    @Inject
    ServiceTravailPratique serviceTravailPratique;


    //Panache (et Hibernate) exigent une transaction active pour persist.
    @BeforeEach
    @Transactional
     void setUp(){
        travailPratiqueRepository.deleteAll();
        etudiantRepository.deleteAll();
        coursRepository.deleteAll();
        //Préparer les données en créant un cours, un TP, et quelques étudiants
        //Ajouter les relations
        /**
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
         */
        serviceCours.creerCours("Approfondissement de la programmation", "62-21",
                String.valueOf(TypeSemestre.Printemps), 2025, String.valueOf(TypeCours.Java));
        Cours c = coursRepository.findCoursByCode("62-21");
        serviceCours.ajouterTP(c.id, 1);
        Etudiant e1 = new Etudiant("Scout Mark", "mark@hesge.ch", TypeEtude.temps_plein);
        Etudiant e2 = new Etudiant("Riggs Helly", "helly@hesge.ch", TypeEtude.temps_partiel);
        Etudiant e3 = new Etudiant("George Dylan", "dylan@hesge.ch", TypeEtude.temps_plein);
        Etudiant e4 = new Etudiant("Bailiff Irving", "irving@hesge.ch", TypeEtude.temps_plein);
        c.addEtudiant(e1);c.addEtudiant(e2);c.addEtudiant(e3);c.addEtudiant(e4);
        coursRepository.persist(c);
    }

    @Test
    @Order(1)
    public void testSetup(){
        //Vérifier que les données ont bien été créées
        Assertions.assertEquals(1, coursRepository.count());
        Cours c = coursRepository.findCoursByCode("62-21");
        Assertions.assertEquals(4, c.etudiantsInscrits.size());
        Assertions.assertEquals(1, c.travauxPratiques.size());
    }

    /**
     * JUnit créera un dossier temporaire unique pour chaque test,
     * et le supprimera (avec tout son contenu) une fois le test fini.
     */
    @Test
    @Order(2)
    void testCreateFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.txt");

        List<String> lines = Arrays.asList("howtodoinjava.com");

        Files.write(tempFile, Arrays.asList("howtodoinjava.com"));

        Assertions.assertTrue(Files.exists(tempFile), "Temp File should have been created");
        Assertions.assertEquals(Files.readAllLines(tempFile), lines);
    }

    @Test
    @Order(3)
    @TestTransaction
    public void testCreationCours(){
        //Créer un cours
        serviceCours.creerCours("Programmation collaborative", "63-21",
                String.valueOf(TypeSemestre.Automne), 2025, String.valueOf(TypeCours.Java));
        Assertions.assertEquals(2, coursRepository.count());
        Cours c = coursRepository.findCoursByCode("63-21");
        Assertions.assertEquals("63-21",c.code);
        Assertions.assertEquals("Programmation collaborative", c.nom);
    }

    @Test
    @Order(4)
    @Transactional
    public void testAjoutEtudiants(){
        //Ajouter des étudiants à un cours
        serviceCours.creerCours("Programmation collaborative", "63-21",
                String.valueOf(TypeSemestre.Automne), 2025, String.valueOf(TypeCours.Java));
        Cours c = coursRepository.findCoursByCode("63-21");
        Etudiant e = etudiantRepository.findByEmail("mark@hesge.ch");
        serviceCours.ajouterEtudiant(c.id, e.id);
        Assertions.assertEquals(1, c.etudiantsInscrits.size());
        Assertions.assertEquals(2, e.coursEtudiant.size());

        //Test avec un id inexistant
        Assertions.assertThrows(Exception.class, () -> serviceCours.ajouterEtudiant(100L, 100L));

    }

    @Test
    @Order(5)
    public void testAjoutEtudiantAvecTxt(){
        //Ajouter des étudiants à un cours à partir d'un fichier txt
        Cours c = coursRepository.findCoursByCode("62-21");
        String[] data = {"Walter White;w@hesge.ch;temps_plein",
                "Jesse Pinkman;jesse@hesge.ch;temps_partiel"};
        serviceCours.addAllStudentsFromFile(c.id, data);
        Assertions.assertNotNull(etudiantRepository.findByEmail("w@hesge.ch"));
        Assertions.assertEquals(6, c.etudiantsInscrits.size());
    }

    @Test
    @Order(6)
    public void testAjoutTP(){
        //Ajouter un TP à un cours
        Cours c = coursRepository.findCoursByCode("62-21");
        serviceCours.ajouterTP(c.id, 2);
        Assertions.assertEquals(2, c.travauxPratiques.size());

        //Tester si le TP existe
        int noTpTest = coursRepository.findTpByNo(c.id, 2).no;
        Assertions.assertEquals(2, noTpTest);


    }

    @Test
    @Order(7)
    public void testAjoutEvaluations(){
        //Ajout d'un examen et d'un CC dans le cours 62-21
        Cours c = coursRepository.findCoursByCode("62-21");
        serviceCours.ajouterExamen(c.id, "Examen final", "2025-12-15", "Automne");
        serviceCours.addCC(c.id, "CC1", "2025-10-15", 2, 1);

        //Vérifier que les évaluations ont bien été ajoutées
        Assertions.assertEquals(2, c.evaluations.size());
        Assertions.assertEquals("Examen final", c.evaluations.get(0).nom);
        Assertions.assertEquals("CC1", c.evaluations.get(1).nom);
    }

    @Test
    @Order(8)
    @TestTransaction
    public void testCreationDesDossiers(){
        //Supprimer les précédents dossiers créés si il y en a :
        Path dossierTest = Paths.get("src/test/resources/testZips/63-21");
        try {
            Files.delete(dossierTest);
        } catch (IOException e) {
            System.out.println("Le dossier n'existe pas");
        }

        serviceCours.creerCours("Programmation collaborative", "63-21",
                String.valueOf(TypeSemestre.Automne), 2025, String.valueOf(TypeCours.Java));
        Cours c = coursRepository.findCoursByCode("63-21");

        //Créer un TP
        serviceCours.ajouterTP(c.id, 1);

        //Creer un examen
        serviceCours.ajouterExamen(c.id, "Examen final", "2025-12-15", "Automne");

        //tester si les dossiers ont bien été créés
        Path tpDir = Paths.get("src/test/resources/testZips", c.code, "TP1");
        Assertions.assertTrue(Files.exists(tpDir), "Le dossier TP1 devrait exister !");

        tpDir = Paths.get("src/test/resources/testZips", c.code, "Examen final");
        Assertions.assertTrue(Files.exists(tpDir), "Le dossier Examen final devrait exister !");

    }

    @Test
    @Order(9)
    @TestTransaction
    public void testRenduTp() throws IOException {
        //Créer un TP, et y ajouter un rendu avec le zip
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        serviceCours.creerCours("Programmation collaborative", "63-21",
                String.valueOf(TypeSemestre.Automne), 2025, String.valueOf(TypeCours.Java));
        Cours c = coursRepository.findCoursByCode("63-21");

        //Créer un TP
        serviceCours.ajouterTP(c.id, 1);
        TravailPratique tp01 = coursRepository.findTpByNo(c.id, 1);

        //Créer le rendu du TP via le service de TP
        serviceTravailPratique.creerRenduTP(tp01, inputStream);

        //Vérifier que le rendu a bien été créé
        Assertions.assertNotNull(tp01.rendu);
        Assertions.assertEquals("TP1_RenduCyberlearn.zip", tp01.rendu.nomFichier);

        //tester si les dossiers ont bien été créés
        Path tpDir = Paths.get("src/test/resources/testZips", c.code, "TP1", "TP1_RenduCyberlearn.zip");
        Assertions.assertTrue(Files.exists(tpDir), "Le dossier TP1 devrait exister !");
    }

    @Test
    @Order(10)
    @TestTransaction
    public void testTraitementRenduZip(){

    }

}
