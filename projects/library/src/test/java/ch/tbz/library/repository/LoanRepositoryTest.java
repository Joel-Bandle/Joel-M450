package ch.tbz.library.repository;

import ch.tbz.library.entity.Book;
import ch.tbz.library.entity.Loan;
import ch.tbz.library.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class LoanRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 3, 10);

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LoanRepository loanRepository;

    private Member anna;
    private Member ben;

    @BeforeEach
    void setUp() {
        anna = memberRepository.save(new Member("Anna", "anna@tbz.ch"));
        ben = memberRepository.save(new Member("Ben", "ben@tbz.ch"));
    }

    @Test
    void zaehltNurNichtZurueckgegebeneAusleihenDesMitglieds() {
        saveLoan(anna, "isbn-1", false);
        saveLoan(anna, "isbn-2", false);
        saveLoan(anna, "isbn-3", true);
        saveLoan(ben, "isbn-4", false);

        assertEquals(2, loanRepository.countByMemberIdAndReturnedAtIsNull(anna.getId()));
        assertEquals(1, loanRepository.countByMemberIdAndReturnedAtIsNull(ben.getId()));
    }

    @Test
    void mitgliedOhneAusleihenHatNull() {
        assertEquals(0, loanRepository.countByMemberIdAndReturnedAtIsNull(anna.getId()));
    }

    @Test
    void existsByIsbnFindetGespeicherteBuecher() {
        bookRepository.save(new Book("isbn-9", "Titel", "Autor"));

        assertTrue(bookRepository.existsByIsbn("isbn-9"));
        assertFalse(bookRepository.existsByIsbn("isbn-0"));
    }

    private void saveLoan(Member member, String isbn, boolean returned) {
        Book book = bookRepository.save(new Book(isbn, "Titel " + isbn, "Autor"));
        Loan loan = new Loan(book, member, TODAY, TODAY.plusDays(21));
        if (returned) {
            loan.setReturnedAt(TODAY.plusDays(3));
        }
        loanRepository.save(loan);
    }
}
