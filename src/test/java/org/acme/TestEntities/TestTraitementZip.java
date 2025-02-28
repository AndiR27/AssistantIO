package org.acme.TestEntities;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.entity.Cours;
import org.acme.entity.Rendu;
import org.acme.entity.TravailPratique;
import org.acme.repository.CoursRepository;
import org.acme.repository.EtudiantRepository;
import org.acme.service.*;
import org.acme.service.ServiceTravailPratique;
import org.junit.jupiter.api.*;
import org.wildfly.common.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTraitementZip {

    @Inject
    ServiceCours serviceCours;
    @Inject
    ServiceRendu serviceRendu;
    @Inject
    CoursRepository repositoryCours;
    @Inject
    ServiceTravailPratique serviceTravailPratique;

    @BeforeEach
    @TestTransaction
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

    }


    @Test
    @TestTransaction
    public void testerTraitementZip() throws IOException {
        //Creation des entités
        serviceCours.creerCours("Approfondissement de la programmation", "62-21",
                "Printemps", 2025, "Java");
        Cours c = repositoryCours.findCoursByCode("62-21");
        serviceCours.ajouterTP(c.id, 1);

        //Creer l'input pour le zip
        Path path = Paths.get("src/test/resources/mockinginputstreams/test_zip.zip");
        InputStream inputStream = Files.newInputStream(path);
        //Ajout d'un rendu
        TravailPratique tp = repositoryCours.findTpByNo(c.id, 1);
        serviceTravailPratique.creerRenduTP(tp, inputStream);

        //Lancement de la methode des traitements de zip pour un cours et un TP :
        serviceCours.lancerTraitementRenduZip(c.id, tp.id);

        //Vérifier que le rendu a bien été traité
        Rendu rendu = tp.rendu;
        Assertions.assertNotNull(rendu);
        Assertions.assertEquals("src/test/resources/testZips/62-21/TP1/TP1_RenduCyberlearn.zip", rendu.cheminStockage);
        Assertions.assertEquals("src/test/resources/testZips/62-21/TP1/TP1_RenduRestructuration.zip",
                rendu.cheminFichierStructure);
    }


    public void testerContenuZipPycharm() {
        //TODO
    }
}
