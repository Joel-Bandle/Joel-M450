package ch.tbz.library.service;

import ch.tbz.library.entity.Book;
import ch.tbz.library.entity.Loan;
import ch.tbz.library.entity.Member;
import ch.tbz.library.exception.ConflictException;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.repository.BookRepository;
import ch.tbz.library.repository.LoanRepository;
import ch.tbz.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
public class LoanService {

    static final int LOAN_DAYS = 21;
    static final int MAX_ACTIVE_LOANS = 3;

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final FeeCalculator feeCalculator;
    private final Clock clock;

    public LoanService(BookRepository bookRepository, MemberRepository memberRepository,
                       LoanRepository loanRepository, FeeCalculator feeCalculator, Clock clock) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.feeCalculator = feeCalculator;
        this.clock = clock;
    }

    @Transactional
    public Loan borrow(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Buch " + bookId + " nicht gefunden"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Mitglied " + memberId + " nicht gefunden"));

        if (!book.isAvailable()) {
            throw new ConflictException("Das Buch ist bereits ausgeliehen");
        }
        if (loanRepository.countByMemberIdAndReturnedAtIsNull(memberId) >= MAX_ACTIVE_LOANS) {
            throw new ConflictException("Ein Mitglied darf höchstens " + MAX_ACTIVE_LOANS + " Bücher ausleihen");
        }

        LocalDate today = LocalDate.now(clock);
        book.setAvailable(false);
        bookRepository.save(book);
        return loanRepository.save(new Loan(book, member, today, today.plusDays(LOAN_DAYS)));
    }

    @Transactional
    public ReturnResult giveBack(Long loanId) {
        Loan loan = get(loanId);
        if (loan.getReturnedAt() != null) {
            throw new ConflictException("Das Buch wurde bereits zurückgegeben");
        }

        LocalDate today = LocalDate.now(clock);
        loan.setReturnedAt(today);
        loan.getBook().setAvailable(true);
        bookRepository.save(loan.getBook());
        loanRepository.save(loan);

        BigDecimal fee = feeCalculator.calculate(loan.getDueDate(), today);
        return new ReturnResult(loan, fee);
    }

    public Loan get(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Ausleihe " + loanId + " nicht gefunden"));
    }
}
