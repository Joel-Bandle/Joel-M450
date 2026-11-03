package ch.tbz.library.service;

import ch.tbz.library.entity.Book;
import ch.tbz.library.entity.Loan;
import ch.tbz.library.entity.Member;
import ch.tbz.library.exception.ConflictException;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.repository.BookRepository;
import ch.tbz.library.repository.LoanRepository;
import ch.tbz.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 3, 10);

    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private FeeCalculator feeCalculator;

    private LoanService service;
    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(zone).toInstant(), zone);
        service = new LoanService(bookRepository, memberRepository, loanRepository, feeCalculator, fixedClock);

        book = new Book("9780132350884", "Clean Code", "Robert C. Martin");
        member = new Member("Anna", "anna@tbz.ch");
    }

    @Test
    void borrowSetztRueckgabedatumAuf21TageUndMarkiertBuchAlsAusgeliehen() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(2L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(call -> call.getArgument(0));

        Loan loan = service.borrow(1L, 2L);

        assertEquals(TODAY, loan.getBorrowedAt());
        assertEquals(TODAY.plusDays(21), loan.getDueDate());
        assertFalse(book.isAvailable());
        verify(bookRepository).save(book);
    }

    @Test
    void borrowMitZweiAktivenAusleihenIstNochErlaubt() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(2L)).thenReturn(2L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(call -> call.getArgument(0));

        Loan loan = service.borrow(1L, 2L);

        assertEquals(member, loan.getMember());
    }

    @ParameterizedTest
    @ValueSource(longs = {3, 4})
    void borrowAbDreiAktivenAusleihenWirdAbgelehnt(long aktiveAusleihen) {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(2L)).thenReturn(aktiveAusleihen);

        assertThrows(ConflictException.class, () -> service.borrow(1L, 2L));

        verify(loanRepository, never()).save(any());
        assertTrue(book.isAvailable());
    }

    @Test
    void borrowEinesAusgeliehenenBuchsWirdAbgelehnt() {
        book.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

        assertThrows(ConflictException.class, () -> service.borrow(1L, 2L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowMitUnbekanntemBuchWirftNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.borrow(99L, 2L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowMitUnbekanntemMitgliedWirftNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.borrow(1L, 99L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void giveBackSetztRueckgabedatumUndGibtBuchFrei() {
        Loan loan = openLoan(TODAY.plusDays(11));
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
        when(feeCalculator.calculate(loan.getDueDate(), TODAY)).thenReturn(new BigDecimal("0.00"));

        ReturnResult result = service.giveBack(5L);

        assertEquals(TODAY, result.loan().getReturnedAt());
        assertTrue(book.isAvailable());
        assertEquals(new BigDecimal("0.00"), result.fee());
        verify(bookRepository).save(book);
    }

    @Test
    void giveBackReichtFaelligkeitUndHeutigesDatumAnDenFeeCalculatorWeiter() {
        Loan loan = openLoan(TODAY.minusDays(10));
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
        when(feeCalculator.calculate(TODAY.minusDays(10), TODAY)).thenReturn(new BigDecimal("5.00"));

        ReturnResult result = service.giveBack(5L);

        assertEquals(new BigDecimal("5.00"), result.fee());
    }

    @Test
    void giveBackEinerBereitsZurueckgegebenenAusleiheWirdAbgelehnt() {
        Loan loan = openLoan(TODAY);
        loan.setReturnedAt(TODAY.minusDays(1));
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));

        assertThrows(ConflictException.class, () -> service.giveBack(5L));

        verify(feeCalculator, never()).calculate(any(), any());
    }

    @Test
    void giveBackMitUnbekannterAusleiheWirftNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.giveBack(99L));
    }

    private Loan openLoan(LocalDate dueDate) {
        book.setAvailable(false);
        return new Loan(book, member, TODAY.minusDays(21), dueDate);
    }
}
