package org.acme.TestRest;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.models.CoursDTO;
import org.acme.models.TypeCoursDTO;
import org.acme.models.TypeSemestreDTO;
import org.acme.service.ServiceCours;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CoursControllerTest {

    @Inject
    ServiceCours coursService;
    @BeforeEach
    @Transactional
    void setup() {
        // 1. Créer un cours en base, si besoin
        // via ServiceCours ou un repository direct
        // ex: coursService.creerCours(...)
        CoursDTO cours = new CoursDTO(null,
                "Programmation collaborative",
                "63-21", TypeSemestreDTO.Automne, 2025, "Stettler", TypeCoursDTO.Java, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        CoursDTO coursDTO = coursService.creerCours(cours);
    }

    /**
     * Test: récupérer un cours par son ID
     */
    @Test
    public void testGetCoursById() {
        // Supposons qu'un cours avec ID=1 existe via le setup
        given()
                .when()
                .get("/cours/1")
                .then()
                .statusCode(200)
                // Vérifie que le HTML contient un bloc du cours
                .body(containsString("63-21"), not(containsString("Cours non trouvé")));
                // On peut vérifier qu'une zone de la page contient 'INFO101', etc.
    }

    /**
     * Test: ajouter un étudiant via un form POST
     */
    @Test
    @Transactional
    public void testAddEtudiant() {
        // On suppose qu'un cours ID=1 existe
        given()
                .formParam("nom", "Alice Dupont")
                .formParam("email", "alice@example.com")
                .formParam("typeEtude", "temps_plein")
                .contentType(ContentType.URLENC)
                .when()
                .post("/cours/1/addEtudiant")
                .then()
                .statusCode(200)
                // On renvoie probablement un fragment HTML listant les étudiants
                // => vérifier qu'il contient 'Alice Dupont'
                .body(containsString("Alice Dupont"))
                .body(containsString("alice@example.com"));
    }


    /**
     * Test: ajouter un TP
     */
    @Test
    public void testAddTP() {
        // On suppose qu'un cours ID=1 existe
        given()
                .formParam("numero", 3)
                .contentType(ContentType.URLENC)
                .when()
                .post("/cours/1/addTP")
                .then()
                .statusCode(200)
                // La page HTML renvoyée inclut le detail du cours => vérifier qu'il y a un 'TP3' ou un index 3
                .body(containsString("TP 3"));
    }

    /**
     * Test: cours non trouvé
     * Vérifie qu'on obtient NotFound ou un affichage d'erreur
     */
    @Test
    public void testGetCoursNotFound() {
        given()
                .when()
                .get("/cours/9999") // id inconnu
                .then()
                .statusCode(404); // NotFound
    }

    /**
     * Test: ajouter un Examen
     */
    @Test
    public void testAddExamen() {
        // Suppose cours id=1
        given()
                .formParam("nom", "Examen Final")
                .formParam("date", "2025-06-01")
                .formParam("semestre", "PRINTEMPS")
                .contentType(ContentType.URLENC)
                .when()
                .post("/cours/1/addExamen")
                .then()
                .statusCode(200)
                .body(containsString("Examen Final"));
    }
}
