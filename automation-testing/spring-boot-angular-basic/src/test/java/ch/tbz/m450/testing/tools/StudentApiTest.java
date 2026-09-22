package ch.tbz.m450.testing.tools;

import ch.tbz.m450.testing.tools.repository.StudentRepository;
import ch.tbz.m450.testing.tools.repository.entities.Student;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Uebung 1: automatisierter REST-API-Test mit JUnit 5 + REST-Assured.
 * Die App laeuft fuer die ganze Testklasse in derselben In-Memory-H2-DB;
 * damit die Tests trotzdem unabhaengig voneinander sind (F.I.R.S.T:
 * "isoliert", Reihenfolge darf keine Rolle spielen), setzt @BeforeEach die
 * DB vor JEDEM einzelnen Test explizit auf denselben Ausgangszustand zurueck
 * (die 5 bekannten Studenten) statt sich auf die Ausfuehrungsreihenfolge zu
 * verlassen.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentApiTest {

    private static final String[] SEED_NAMES = {"Jonas", "Patrick", "Yves", "Peter", "Ann"};

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        studentRepository.deleteAll();
        Stream.of(SEED_NAMES).forEach(name ->
                studentRepository.save(new Student(name, name.toLowerCase() + "@tbz.ch")));
    }

    @Test
    void getStudentsGibt200UndDieFuenfGeseedetenStudentenZurueck() {
        given()
        .when()
            .get("/students")
        .then()
            .statusCode(200)
            .body("size()", equalTo(5))
            .body("name", hasItems("Jonas", "Patrick", "Yves", "Peter", "Ann"));
    }

    @Test
    void postStudentLegtNeuenStudentenAnUndErscheintDanachInDerListe() {
        Student neu = new Student("Automation", "automation@tbz.ch");

        given()
            .contentType("application/json")
            .body(neu)
        .when()
            .post("/students")
        .then()
            .statusCode(200);

        given()
        .when()
            .get("/students")
        .then()
            .statusCode(200)
            .body("size()", equalTo(6))
            .body("name", hasItem("Automation"))
            .body("find { it.name == 'Automation' }.email", equalTo("automation@tbz.ch"));
    }

    @Test
    void jederStudentInDerListeHatEineIdUndEineEmail() {
        given()
        .when()
            .get("/students")
        .then()
            .statusCode(200)
            .body("id", everyItem(notNullValue()))
            .body("email", everyItem(containsString("@")));
    }

    // Bonus-Feature: Input-Validierung + Error Handling

    @Test
    void postStudentMitLeeremNamenWirdMit400UndFehlermeldungAbgelehnt() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"\", \"email\": \"gueltig@tbz.ch\"}")
        .when()
            .post("/students")
        .then()
            .statusCode(400)
            .body("name", equalTo("Name darf nicht leer sein"));
    }

    @Test
    void postStudentMitUngueltigerEmailWirdMit400UndFehlermeldungAbgelehnt() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"Test\", \"email\": \"keine-email\"}")
        .when()
            .post("/students")
        .then()
            .statusCode(400)
            .body("email", equalTo("Email muss ein gueltiges Format haben"));
    }

    @Test
    void ungueltigerStudentWirdNichtGespeichert() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"\", \"email\": \"keine-email\"}")
        .when()
            .post("/students");

        given()
        .when()
            .get("/students")
        .then()
            // weiterhin nur die 5 geseedeten, der ungueltige wurde nicht gespeichert
            .body("size()", equalTo(5));
    }
}
