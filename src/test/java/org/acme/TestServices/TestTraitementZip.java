package org.acme.TestServices;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.models.CoursDTO;
import org.acme.models.TravailPratiqueDTO;
import org.acme.models.TypeCoursDTO;
import org.acme.models.TypeSemestreDTO;
import org.acme.repository.*;
import org.acme.service.*;
import org.acme.service.ServiceTravailPratique;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@QuarkusTest
public class TestTraitementZip {

    @Inject
    ServiceCours serviceCours;
    @Inject
    ServiceRendu serviceRendu;
    @Inject
    CoursRepository repositoryCours;
    @Inject
    ServiceTravailPratique serviceTravailPratique;

    @Inject
    TP_StatusRepository repositoryTP_Status;
    @Inject
    EtudiantRepository etudiantRepository;

    @Inject
    TPRepository repositoryTravailPratique;
    @Inject
    RenduRepository renduRepository;

    @BeforeEach
    @Transactional
    void setUp() throws IOException {
        //Gérer le repository en supprimant les données
        Path testZip = Paths.get("src/test/resources/testZips");
        if (Files.exists(testZip)) {
            Files.walk(testZip)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // supprimer d'abord les fichiers
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
        Files.createDirectories(testZip);
        /**
        renduRepository.deleteAll();
        repositoryTravailPratique.deleteAll();
        etudiantRepository.deleteAll();
        repositoryCours.deleteAll();
         */
    }


    @Test
    @Transactional
    public void testerTraitementZip() throws IOException {
        //Creation des entités
        CoursDTO cours = new CoursDTO(null,
                "Approfondissement de la programmation",
                "62-21", TypeSemestreDTO.Printemps, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);

        serviceCours.ajouterTP(coursDTO, 1);

        //Creer l'input pour le zip
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        //Ajout d'un rendu
        Cours c = repositoryCours.findById(coursDTO.getId());
        TravailPratique tp = repositoryCours.findTpByNo(c.id, 1);
        serviceTravailPratique.creerRenduTP(tp, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        serviceCours.lancerTraitementRenduZip(c.id, tp.id);

        //Vérifier que le rendu a bien été traité
        Rendu rendu = tp.rendu;
        Assertions.assertNotNull(rendu);
        String cheminVersZip = rendu.cheminStockage;
        String cheminVersZipRestructure = rendu.cheminFichierStructure;
        // Normaliser les chemins pour éviter les problèmes de séparateurs ('/' vs '\\')
        Path expectedPath1 = Paths.get("src/test/resources/testZips/62-21/TP1/TP1_RenduCyberlearn.zip");
        Path expectedPath2 = Paths.get("src/test/resources/testZips/62-21/TP1/TP1_RenduRestructuration.zip");

        Path actualPath1 = Paths.get(cheminVersZip);
        Path actualPath2 = Paths.get(cheminVersZipRestructure);

        // Comparaison normalisée des chemins
        Assertions.assertTrue(expectedPath1.equals(actualPath1),
                "Le chemin du ZIP ne correspond pas : " + actualPath1);
        Assertions.assertTrue(expectedPath2.equals(actualPath2),
                "Le chemin du ZIP restructuré ne correspond pas : " + actualPath2);
    }

    @Test
    @Transactional
    public void testerContenuZip() throws IOException {
        //Creation des entités
        CoursDTO cours = new CoursDTO(null,
                "Python Introduction",
                "61-13", TypeSemestreDTO.Automne, 2025, "Stettler", TypeCoursDTO.Python, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);

        serviceCours.ajouterTP(coursDTO, 1);

        //Creer l'input pour le zip
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);
        //Ajout d'un rendu
        Cours c = repositoryCours.findCoursByCode("61-13");
        TravailPratique tp = repositoryCours.findTpByNo(c.id, 1);
        serviceTravailPratique.creerRenduTP(tp, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        serviceCours.lancerTraitementRenduZip(c.id, tp.id);

        //tests
        Rendu rendu = tp.rendu;
        Assertions.assertNotNull(rendu);

        //Vérifier si le rendu de george dylan ne contient que des fichiers .py
        Path pathRendu = Paths.get(rendu.cheminFichierStructure);
        Assertions.assertTrue(Files.exists(pathRendu));

        Path etudiantPath = pathRendu.resolve("georgedylan");
        // Vérifier le contenu du ZIP sans extraction
        try (ZipFile zipFile = new ZipFile(pathRendu.toFile())) {
            boolean hasStudentFolder = false;
            boolean containsOtherThanPy = false;
            boolean containsSrcFolder = false;

            for (ZipEntry entry : zipFile.stream().toList()) {
                String entryName = entry.getName();

                // Vérifier si le dossier de l'étudiant existe
                if (entryName.startsWith("georgedylan/")) {
                    hasStudentFolder = true;
                }

                // Vérifier que tous les fichiers sous 'georgedylan/' sont bien des .py
                if (entryName.startsWith("georgedylan/") && !entry.isDirectory()) {
                    if (!entryName.endsWith(".py")) {
                        containsOtherThanPy = true;
                    }
                }

                //Vérification pour le contenu d'un projet java : tester si un dossier src existe
                if (entryName.startsWith("scoutmark/") && entry.isDirectory() && entryName.endsWith("/src/")) {
                    containsSrcFolder = true;
                }
            }

            Assertions.assertTrue(hasStudentFolder, "Le dossier de l'étudiant georgedylan doit exister dans le ZIP.");
            Assertions.assertFalse(containsOtherThanPy, "Tous les fichiers dans 'georgedylan/' doivent être des .py, y compris dans les sous-dossiers.");
            Assertions.assertTrue(containsSrcFolder, "Le dossier 'src' doit exister dans le dossier 'scoutmark'.");
        }

        /**
        try (Stream<Path> files = Files.walk(etudiantPath)) {
            boolean containsOnlyPy = files
                    .filter(Files::isRegularFile)
                    .allMatch(file -> file.toString().endsWith(".py"));
            Assertions.assertTrue(containsOnlyPy, "Le rendu doit contenir uniquement des fichiers .py");

        }*/
    }

    /**
     * Tester la mise à jour des status des étudiants pour voir qui a fait et qui a pas fait
     */
    @Test
    @Transactional
    public void testStatusUpdateEtudiants() throws IOException {

        //Créer les données de test
        CoursDTO cours = new CoursDTO(null,
                "Approfondissement de la programmation",
                "62-21", TypeSemestreDTO.Printemps, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = serviceCours.creerCours(cours);

        TravailPratiqueDTO tpDTO = serviceCours.ajouterTP(coursDTO, 1);

        String[] etudiants = {
                "Scout Mark;mark@hesge.ch;temps_plein",
                "Riggs Helly;helly@hesge.ch;temps_partiel",
                "George Dylan;dylan@hesge.ch;temps_plein",
                "Bailiff Irving;irving@hesge.ch;temps_plein",
        };
        serviceCours.addAllStudentsFromFile(coursDTO, etudiants);

        //Utilisation d'un zip pour ajouter un rendu
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);

        //Ajout d'un rendu
        Cours c = repositoryCours.findById(coursDTO.getId());
        TravailPratique tp = repositoryCours.findTpByNo(c.id, 1);
        serviceTravailPratique.creerRenduTP(tp, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        serviceCours.lancerTraitementRenduZip(c.id, tp.id);

        //tests
        Rendu rendu = tp.rendu;
        Assertions.assertNotNull(rendu);

        //Lancement de la methode pour traiter les TP_Status avec gestionRendusTP dans serviceTravailPratique
        tp = serviceTravailPratique.gestionRendusTP(c, tp, c.etudiantsInscrits);

        //première vérification : tester si les 4 étudiants ont bien un TP_Status
        List<TP_Status> tpStatusList = repositoryTP_Status.findByTP(tp.id);
        Assertions.assertEquals(4, tpStatusList.size());

        //Parcours de la liste : normalement 3 étudiants sur 4 ont rendu, Irvin n'a pas rendu
        for (TP_Status tpStatus : tpStatusList) {
            if (tpStatus.etudiant.nom.equals("Bailiff Irving")) {
                Assertions.assertFalse(tpStatus.renduEtudiant);
            }
            else{
                Assertions.assertTrue(tpStatus.renduEtudiant);
            }
        }
    }


}
