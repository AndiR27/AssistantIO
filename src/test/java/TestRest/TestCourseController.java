package TestRest;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class TestCourseController {

    Long courseId;

    @BeforeEach
    @TestTransaction
    public void setup() {
        String code = "COURSE-" + UUID.randomUUID().toString().substring(0, 6);
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "id": null,
                      "name": "Test Course",
                      "code": "%s",
                      "semester": "AUTOMNE",
                      "year_course": 2025,
                      "teacher": "Test Prof",
                      "courseType": "JAVA",
                      "studentList": [],
                      "tpsList": [],
                      "evaluations": []
                    }
                """.formatted(code))
                .when().post("/admin/v2/courses/addCourse")
                .then().statusCode(200)
                .extract().path("id");
        courseId = id.longValue();
    }

    @Test
    @TestTransaction
    public void testGetCourseById() {
        given()
                .when().get("/course/{id}", courseId)
                .then().statusCode(200)
                .body("id", equalTo(courseId.intValue()));
    }

    @Test
    @TestTransaction
    public void testAddStudentToCourse() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "id": null,
                  "name": "Alice",
                  "email": "alice.test@example.com",
                  "studyType": "temps_plein"
                }
            """)
                .when().post("/course/{id}/addStudent", courseId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Alice"));
    }

    @Test
    @TestTransaction
    public void testGetAllStudentsInCourse() {
        given()
                .when().get("/course/{id}/students", courseId)
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @TestTransaction
    public void testAddTP() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/course/{id}/addTP/{tpNumber}", courseId, 3)
                .then()
                .statusCode(200)
                .body("no", equalTo(3));
    }

    @Test
    @TestTransaction
    public void testGetAllTPs() {
        //add 2 TPs for testing
        given()
                .contentType(ContentType.JSON)
                .when().post("/course/{id}/addTP/{tpNumber}", courseId, 2)
                .then()
                .statusCode(200)
                .body("no", equalTo(2));



        given()
                .contentType(ContentType.JSON)
                .when().get("/course/{id}/tps", courseId)
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @TestTransaction
    public void testAddExam() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "id": null,
                        "name": "Examen Final",
                        "date": "2025-06-15T10:00:00",
                        "submission": null
                    }
                        """)
                .when().post("/course/{id}/addExam", courseId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Examen Final"));
    }

    @Test
    @TestTransaction
    public void testAddCC() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "id": null,
                    "name": "CC1",
                    "date": "2025-05-20T09:00:00",
                    "submission": null
                }
            """)
                .when().post("/course/{id}/addCC", courseId)
                .then()
                .statusCode(200)
                .body("name", equalTo("CC1"));
    }

    @Test
    @TestTransaction
    public void testAddStudentsFromFile() {
        File txtFile = new File("src/test/resources/TestAddEtudiantsTxt.txt");
        given()
                .multiPart(new MultiPartSpecBuilder(txtFile)
                        .fileName("students.txt")
                        .controlName("file")
                        .mimeType("text/plain")
                        .build())
                .when().post("/course/{id}/addStudentsFromFile", courseId)
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @TestTransaction
    public void testAddSubmissionToTP() {
        // setup TP
        given()
                .contentType(ContentType.JSON)
                .when().post("/course/{id}/addTP/{tpNumber}", courseId, 5)
                .then().statusCode(200);

        File zipFile = new File("src/test/resources/mockinginputstreams/test_zip.zip");

        given()
                .multiPart("file", zipFile, "application/zip") // nom du champ = "file"
                .multiPart("fileName", "test_zip.zip")         // champ texte "fileName"
                .when().post("/course/{id}/addRendu/{tpNo}", courseId, 5)
                .then()
                .statusCode(200)
                .body("no", equalTo(5));
    }
}
