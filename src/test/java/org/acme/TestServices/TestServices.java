package org.acme.TestServices;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.models.*;
import org.acme.repository.*;
import org.acme.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class TestServices {

    @Inject
    CoursRepository coursRepository;
    @Inject
    TPRepository travailPratiqueRepository;
    @Inject
    EtudiantRepository etudiantRepository;
    @Inject
    ServiceCours serviceCours;
    @Inject
    ServiceTravailPratique serviceTravailPratique;
    @Inject
    ServiceEtudiant serviceEtudiant;



    /**
     * Nettoyage de la base avant chaque test.
     * On ne recrée pas ici d'entités par défaut
     * pour que chaque test reste indépendant.
     */
    @BeforeEach
    @Transactional
    void cleanDatabase() {
        travailPratiqueRepository.deleteAll();
        etudiantRepository.deleteAll();
        coursRepository.deleteAll();
    }

    /**
     * Vérifie qu'on peut créer un cours et un TP,
     * ainsi que 4 étudiants, puis vérifier leur bonne insertion.
     * // Ajouter des étudiants
     *         Etudiant e1 = new Etudiant("Scout Mark", "mark@hesge.ch", TypeEtude.temps_plein);
     *         Etudiant e2 = new Etudiant("Riggs Helly", "helly@hesge.ch", TypeEtude.temps_partiel);
     *         Etudiant e3 = new Etudiant("George Dylan", "dylan@hesge.ch", TypeEtude.temps_plein);
     *         Etudiant e4 = new Etudiant("Bailiff Irving", "irving@hesge.ch", TypeEtude.temps_plein);
     */


    /**
     * Exemple d'utilisation de @TempDir :
     * JUnit crée un dossier temporaire unique pour chaque test.
     * Il est supprimé automatiquement après.
     */
    @Test
    void testCreateFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.txt");
        List<String> lines = Arrays.asList("howtodoinjava.com");

        Files.write(tempFile, lines);
        Assertions.assertTrue(Files.exists(tempFile), "Temp File should have been created");
        Assertions.assertEquals(lines, Files.readAllLines(tempFile));
    }

    /**
     * Test de création d'un cours, sans interférer avec le setUp.
     * On utilise @TestTransaction pour valider les changements.
     */
    @Test
    @TestTransaction
    public void testCreationCours() {
        CoursDTO cours = new CoursDTO(null,
                "Programmation collaborative",
                "63-21", TypeSemestreDTO.Automne, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);

        //vérifier le DTO
        Assertions.assertNotNull(coursDTO);

        Assertions.assertEquals(1, coursRepository.count());
        Cours c = coursRepository.findCoursByCode("63-21");
        Assertions.assertEquals("63-21", c.code);
        Assertions.assertEquals("Programmation collaborative", c.nom);
    }

    /**
     * Test ajout d'étudiants à un cours existant.
     */
    @Test
    @Transactional
    public void testAjoutEtudiants() {
        // Créer un cours
        CoursDTO cours = new CoursDTO(null,
                "Programmation collaborative",
                "63-21", TypeSemestreDTO.Automne, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);

        Cours c = coursRepository.findCoursByCode("63-21");

        // Créer un étudiant en base
        EtudiantDTO eMark = new EtudiantDTO(null, "Scout Mark", "mark@hesge.ch", TypeEtudeDTO.temps_plein, new ArrayList<>());
        EtudiantDTO eMarkDTO = serviceEtudiant.addEtudiant(eMark);

        // Ajouter Mark au cours 63-21
        EtudiantDTO etuAdded = serviceCours.ajouterEtudiant(coursDTO, eMarkDTO);


        Assertions.assertEquals(1, c.etudiantsInscrits.size());
        Assertions.assertNotNull(etuAdded);

        // On vérifie qu'il est aussi lié côté Etudiant
        Assertions.assertEquals(1, etudiantRepository.findByEmail(etuAdded.getEmail()).coursEtudiant.size());
        Assertions.assertEquals("63-21", etudiantRepository.findByEmail(etuAdded.getEmail()).coursEtudiant.getFirst().code);

        // Test d'un ID inexistant
        //Assertions.assertThrows(Exception.class, () -> serviceCours.ajouterEtudiant(new CoursDTO(), new EtudiantDTO()));
    }

    /**
     * Test de l'ajout d'étudiants à partir d'un fichier .txt.
     */
    @Test
    @Transactional
    public void testAjoutEtudiantAvecTxt() {
        // Créer un cours
        CoursDTO cours = new CoursDTO(null,
                "Approfondissement de la programmation",
                "62-21", TypeSemestreDTO.Printemps, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);
        Cours c = coursRepository.findCoursByCode("62-21");
        // Ajout de quelques étudiants via un pseudo-fichier .txt
        String[] data = {
                "Walter White;w@hesge.ch;temps_plein",
                "Jesse Pinkman;jesse@hesge.ch;temps_partiel"
        };
        serviceCours.addAllStudentsFromFile(coursDTO, data);

        // Vérifications
        Assertions.assertNotNull(etudiantRepository.findByEmail("w@hesge.ch"));
        Assertions.assertNotNull(etudiantRepository.findByEmail("jesse@hesge.ch"));
        Assertions.assertEquals(2, c.etudiantsInscrits.size());
    }

    /**
     * Test d'ajout d'un TP.
     */
    @Test
    @Transactional
    public void testAjoutTP() {
        // Créer un cours
        CoursDTO cours = new CoursDTO(null,
                "Approfondissement de la programmation",
                "62-21", TypeSemestreDTO.Printemps, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);


        // Ajouter un TP
        TravailPratiqueDTO tpDTO = serviceCours.ajouterTP(coursDTO, 2);

        // Vérifier la méthode d'ajout du TP avec le DTO
        Assertions.assertNotNull(tpDTO);
        Assertions.assertEquals(coursDTO.getCode(), travailPratiqueRepository.findById(tpDTO.getId()).cours.code);

        //Vérification du côté de l'entité cours
        Cours c = coursRepository.findCoursByCode("62-21");
        Assertions.assertEquals(1, c.travauxPratiques.size());

        // Tester si le TP existe bien
        TravailPratique tp = coursRepository.findTpByNo(c.id, 2);
        Assertions.assertNotNull(tp);
        Assertions.assertEquals(2, tp.no);
    }

    /**
     * Test d'ajout d'évaluations dans un cours existant.
     */
    @Test
    @Transactional
    public void testAjoutEvaluations() {
        // Créer un cours
        CoursDTO cours = new CoursDTO(null,
                "Approfondissement de la programmation",
                "62-21", TypeSemestreDTO.Printemps, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);
        Cours c = coursRepository.findCoursByCode("62-21");

        // Ajout d'un examen et d'un CC
        ExamenDTO examDTO = serviceCours.ajouterExamen(coursDTO, new ExamenDTO(null, "Examen final", "2025-12-15", coursDTO, null, coursDTO.getSemestre()));
        ControleContinuDTO ccDTO = serviceCours.addCC(coursDTO, new ControleContinuDTO(null, "CC1", "2025-10-15", coursDTO, null, 2, 1));

        //Tester les DTO :
        Assertions.assertNotNull(examDTO);
        Assertions.assertNotNull(ccDTO);


        // Vérifier
        Assertions.assertEquals(2, c.evaluations.size());
        Assertions.assertEquals("Examen final", c.evaluations.get(0).nom);
        Assertions.assertEquals("CC1", c.evaluations.get(1).nom);
    }

    /**
     * Test de création de dossiers (TP, Examen) et vérification de leur existence.
     */
    @Test
    @TestTransaction
    public void testCreationDesDossiers() throws IOException {
        // Nettoyage du répertoire s'il existe
        Path dossierTest = Paths.get("src/test/resources/testZips/63-21");
        if (Files.exists(dossierTest)) {
            Files.walk(dossierTest)
                    .sorted((p1, p2) -> p2.compareTo(p1))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }

        // Créer un cours
        CoursDTO cours = new CoursDTO(null,
                "Programmation collaborative",
                "63-21", TypeSemestreDTO.Automne, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO =serviceCours.creerCours(cours);
        Cours c = coursRepository.findCoursByCode("63-21");

        // Créer un TP et un examen
        serviceCours.ajouterTP(coursDTO, 1);
        serviceCours.ajouterExamen(coursDTO,new ExamenDTO(null, "Examen final", "2025-12-15", coursDTO, null, coursDTO.getSemestre()));

        // Vérifier l'existence des dossiers
        Path tpDir = Paths.get("src/test/resources/testZips", c.code, "TP1");
        Assertions.assertTrue(Files.exists(tpDir), "Le dossier TP1 devrait exister !");

        Path examDir = Paths.get("src/test/resources/testZips", c.code, "Examen final");
        Assertions.assertTrue(Files.exists(examDir), "Le dossier Examen final devrait exister !");
    }

    /**
     * Test ajout de rendu d'un TP (copie de fichier ZIP) et vérification.
     */
    @Test
    @TestTransaction
    public void testRenduTp() throws IOException {
        // Créer un cours et un TP
        CoursDTO cours = new CoursDTO(null,
                "Programmation collaborative",
                "63-21", TypeSemestreDTO.Automne, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);
        Cours c = coursRepository.findCoursByCode("63-21");

        TravailPratiqueDTO tp01DTO = serviceCours.ajouterTP(coursDTO, 1);

        //TravailPratique tp01 = coursRepository.findTpByNo(c.id, 1);

        // Préparer un rendu (ZIP)
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        // Créer le rendu du TP via le service
        TravailPratiqueDTO tp01DTP = serviceTravailPratique.creerRenduTP(tp01DTO, inputStream);

        // Vérifier
        Assertions.assertNotNull(tp01DTP.getRendu());
        Assertions.assertEquals("TP1_RenduCyberlearn.zip",tp01DTP.getRendu().getNomFichier());

        Path tpZipPath = Paths.get("src/test/resources/testZips", c.code, "TP1", "TP1_RenduCyberlearn.zip");
        Assertions.assertTrue(Files.exists(tpZipPath), "Le dossier TP1 devrait exister !");
    }
}
