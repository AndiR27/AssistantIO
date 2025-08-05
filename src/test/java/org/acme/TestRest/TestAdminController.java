package org.acme.TestRest;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
public class TestAdminController {

    private static final String BASE = "/admin/v2/courses";
    private Long createdCourseId;

    @BeforeEach
    @TestTransaction
    public void setup() {
        String uniqueCode = "PRE-" + UUID.randomUUID().toString().substring(0, 8);
        // Insert a course before each test and store its ID
        Integer intId = given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "id": null,
                  "name": "Pré-course",
                  "code": "%s",
                  "semester": "AUTOMNE",
                  "year_course": 2025,
                  "teacher": "Prof Test",
                  "courseType": "JAVA",
                  "studentList": [],
                  "tpsList": [],
                  "evaluations": []
                }
            """.formatted(uniqueCode))
                .when().post("/admin/v2/courses/addCourse")
                .then().statusCode(200)
                .extract().path("id");
        // Convert the ID to Long
        this.createdCourseId = intId.longValue();
    }


    @Test
    @TestTransaction
    public void testAddCourseSuccess() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": null,
                          "name": "Programmation",
                          "code": "add_001",
                          "semester": "AUTOMNE",
                          "year_course": 2024,
                          "teacher": "Stettler",
                          "courseType": "JAVA",
                          "studentList": [],
                          "tpsList": [],
                          "evaluations": []
                        }
                        """)
                .when().post(BASE + "/addCourse")
                .then()
                .statusCode(200)
                .body("name", equalTo("Programmation"));
    }

    @Test
    @TestTransaction
    public void testGetAllCourses() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                            {
                              "id": null,
                              "name": "Test Course",
                              "code": "TC-001",
                              "semester": "PRINTEMPS",
                              "year_course": 2025,
                              "teacher": "Testeur",
                              "courseType": "JAVA",
                              "studentList": [],
                              "tpsList": [],
                              "evaluations": []
                            }
                        """)
                .when().post("/admin/v2/courses/addCourse")
                .then().statusCode(200);
        given()
                .when().get(BASE + "/all")
                .then()
                .statusCode(200)
                .body("$", notNullValue())
                .body("size()", greaterThan(0));  // At least one item in the list
    }

    @Test
    @TestTransaction
    public void testGetCourseById_Success() {
        // Suppose un cours avec ID 1 existe déjà
        System.out.println(createdCourseId.getClass());
        given()
                .when().get(BASE + "/" + createdCourseId.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(createdCourseId.intValue()));
    }

    @Test
    @TestTransaction
    public void testGetCourseById_NotFound() {
        given()
                .when().get(BASE + "/9999")
                .then()
                .statusCode(404); // NotFound
    }

    @Test
    @TestTransaction
    public void testUpdateCourseSuccess() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": 1,
                          "name": "Programmation collaborative avancée",
                          "code": "63-21",
                          "semester": "PRINTEMPS",
                          "year_course": 2025,
                          "teacher": "Dupont",
                          "courseType": "JAVA",
                          "studentList": [],
                          "tpsList": [],
                          "evaluations": []
                        }
                        """)
                .when().put(BASE + "/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Programmation collaborative avancée"));
    }

    @Test
    @TestTransaction
    public void testUpdateCourseNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": 9999,
                          "name": "Fake",
                          "code": "X",
                          "semester": "AUTOMNE",
                          "year_course": 2025,
                          "teacher": "Inconnu",
                          "courseType": "JAVA",
                          "studentList": [],
                          "tpsList": [],
                          "evaluations": []
                        }
                        """)
                .when().put(BASE + "/9999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    public void testDeleteCourseSuccess() {
        // Crée un cours d'abord pour être sûr qu’il existe
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": null,
                          "name": "Temp Course",
                          "code": "TEMP",
                          "semester": "AUTOMNE",
                          "year_course": 2025,
                          "teacher": "Test",
                          "courseType": "JAVA",
                          "studentList": [],
                          "tpsList": [],
                          "evaluations": []
                        }
                        """)
                .when().post(BASE + "/addCourse")
                .then()
                .statusCode(200)
                .extract().path("id");

        given()
                .when().delete(BASE + "/" + id.toString())
                .then()
                .statusCode(204);
    }

    @Test
    public void testDeleteCourseNotFound() {
        given()
                .when().delete(BASE + "/9999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testAddCourseInvalidPayload() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post(BASE + "/addCourse")
                .then()
                .statusCode(500); // dépend du bean validation
    }

}
