package ch.tbz.library.integration;

import ch.tbz.library.client.IsbnInfo;
import ch.tbz.library.client.IsbnLookupClient;
import ch.tbz.library.entity.Loan;
import ch.tbz.library.repository.BookRepository;
import ch.tbz.library.repository.LoanRepository;
import ch.tbz.library.repository.MemberRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private IsbnLookupClient isbnLookupClient;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        loanRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void ausleihenUndZurueckgebenImRahmenDerFristKostetNichts() {
        long memberId = createMember("Anna", "anna@tbz.ch");
        long bookId = createBook("9780132350884", "Clean Code");

        long loanId = given().contentType("application/json")
                .body(loanBody(bookId, memberId))
                .when().post("/api/loans")
                .then().statusCode(201)
                .body("dueDate", equalTo(LocalDate.now().plusDays(21).toString()))
                .extract().jsonPath().getLong("id");

        given().when().get("/api/books/" + bookId)
                .then().statusCode(200).body("available", is(false));

        given().when().post("/api/loans/" + loanId + "/return")
                .then().statusCode(200)
                .body("fee", equalTo(0.0f))
                .body("loan.returnedAt", equalTo(LocalDate.now().toString()));

        given().when().get("/api/books/" + bookId)
                .then().statusCode(200).body("available", is(true));
    }

    @Test
    void verspaeteteRueckgabeKostetGebuehr() {
        long memberId = createMember("Anna", "anna@tbz.ch");
        long bookId = createBook("9780132350884", "Clean Code");
        long loanId = borrow(bookId, memberId);

        Loan loan = loanRepository.findById(loanId).orElseThrow();
        loan.setDueDate(LocalDate.now().minusDays(10));
        loanRepository.save(loan);

        given().when().post("/api/loans/" + loanId + "/return")
                .then().statusCode(200).body("fee", equalTo(5.0f));
    }

    @Test
    void bereitsAusgeliehenesBuchKannNichtNochmalAusgeliehenWerden() {
        long anna = createMember("Anna", "anna@tbz.ch");
        long ben = createMember("Ben", "ben@tbz.ch");
        long bookId = createBook("9780132350884", "Clean Code");
        borrow(bookId, anna);

        given().contentType("application/json")
                .body(loanBody(bookId, ben))
                .when().post("/api/loans")
                .then().statusCode(409)
                .body("error", equalTo("Das Buch ist bereits ausgeliehen"));
    }

    @Test
    void viertesBuchWirdAbgelehnt() {
        long memberId = createMember("Anna", "anna@tbz.ch");
        borrow(createBook("isbn-1", "Buch 1"), memberId);
        borrow(createBook("isbn-2", "Buch 2"), memberId);
        borrow(createBook("isbn-3", "Buch 3"), memberId);
        long viertesBuch = createBook("isbn-4", "Buch 4");

        given().contentType("application/json")
                .body(loanBody(viertesBuch, memberId))
                .when().post("/api/loans")
                .then().statusCode(409);
    }

    @Test
    void zweimalZurueckgebenWirdAbgelehnt() {
        long memberId = createMember("Anna", "anna@tbz.ch");
        long loanId = borrow(createBook("isbn-1", "Buch 1"), memberId);

        given().when().post("/api/loans/" + loanId + "/return").then().statusCode(200);
        given().when().post("/api/loans/" + loanId + "/return").then().statusCode(409);
    }

    @Test
    void buchOhneTitelWirdUeberDenIsbnDienstErgaenzt() {
        when(isbnLookupClient.lookup("9780132350884"))
                .thenReturn(Optional.of(new IsbnInfo("Clean Code", "Robert C. Martin")));

        given().contentType("application/json")
                .body("{\"isbn\": \"9780132350884\"}")
                .when().post("/api/books")
                .then().statusCode(201)
                .body("title", equalTo("Clean Code"))
                .body("author", equalTo("Robert C. Martin"));
    }

    @Test
    void unbekannteIsbnGibt404() {
        when(isbnLookupClient.lookup("0000")).thenReturn(Optional.empty());

        given().contentType("application/json")
                .body("{\"isbn\": \"0000\"}")
                .when().post("/api/books")
                .then().statusCode(404);
    }

    @Test
    void ungueltigeEmailBeimMitgliedGibt400() {
        given().contentType("application/json")
                .body("{\"name\": \"Anna\", \"email\": \"keine-email\"}")
                .when().post("/api/members")
                .then().statusCode(400)
                .body("email", equalTo("Email muss ein gültiges Format haben"));
    }

    private long createMember(String name, String email) {
        return given().contentType("application/json")
                .body("{\"name\": \"" + name + "\", \"email\": \"" + email + "\"}")
                .when().post("/api/members")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createBook(String isbn, String title) {
        return given().contentType("application/json")
                .body("{\"isbn\": \"" + isbn + "\", \"title\": \"" + title + "\", \"author\": \"Autor\"}")
                .when().post("/api/books")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long borrow(long bookId, long memberId) {
        return given().contentType("application/json")
                .body(loanBody(bookId, memberId))
                .when().post("/api/loans")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private String loanBody(long bookId, long memberId) {
        return "{\"bookId\": " + bookId + ", \"memberId\": " + memberId + "}";
    }
}
