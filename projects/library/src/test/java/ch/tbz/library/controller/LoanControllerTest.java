package ch.tbz.library.controller;

import ch.tbz.library.entity.Book;
import ch.tbz.library.entity.Loan;
import ch.tbz.library.entity.Member;
import ch.tbz.library.exception.ConflictException;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.service.LoanService;
import ch.tbz.library.service.ReturnResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    private Loan loan() {
        return new Loan(new Book("9780132350884", "Clean Code", "Robert C. Martin"),
                new Member("Anna", "anna@tbz.ch"),
                LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 31));
    }

    @Test
    void postGibt201MitFaelligkeitsdatum() throws Exception {
        when(loanService.borrow(1L, 2L)).thenReturn(loan());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId": 1, "memberId": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value("2025-03-31"))
                .andExpect(jsonPath("$.book.title").value("Clean Code"));
    }

    @Test
    void postOhneBookIdGibt400() throws Exception {
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId": 2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bookId").value("bookId fehlt"));

        verifyNoInteractions(loanService);
    }

    @Test
    void postBeiAusgeliehenemBuchGibt409() throws Exception {
        when(loanService.borrow(1L, 2L)).thenThrow(new ConflictException("Das Buch ist bereits ausgeliehen"));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId": 1, "memberId": 2}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Das Buch ist bereits ausgeliehen"));
    }

    @Test
    void returnGibt200MitGebuehr() throws Exception {
        when(loanService.giveBack(5L)).thenReturn(new ReturnResult(loan(), new BigDecimal("5.00")));

        mockMvc.perform(post("/api/loans/5/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee").value(5.0))
                .andExpect(jsonPath("$.loan.dueDate").value("2025-03-31"));
    }

    @Test
    void returnMitUnbekannterAusleiheGibt404() throws Exception {
        when(loanService.giveBack(99L)).thenThrow(new NotFoundException("Ausleihe 99 nicht gefunden"));

        mockMvc.perform(post("/api/loans/99/return"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGibt200() throws Exception {
        when(loanService.get(5L)).thenReturn(loan());

        mockMvc.perform(get("/api/loans/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.name").value("Anna"));
    }
}
