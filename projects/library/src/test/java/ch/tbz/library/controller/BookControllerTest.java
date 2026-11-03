package ch.tbz.library.controller;

import ch.tbz.library.entity.Book;
import ch.tbz.library.exception.ConflictException;
import ch.tbz.library.exception.ExternalServiceException;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void postGibt201UndDasGespeicherteBuchZurueck() throws Exception {
        when(bookService.add("9780132350884", "Clean Code", "Robert C. Martin"))
                .thenReturn(new Book("9780132350884", "Clean Code", "Robert C. Martin"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "9780132350884", "title": "Clean Code", "author": "Robert C. Martin"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.available").value(true));

        verify(bookService).add("9780132350884", "Clean Code", "Robert C. Martin");
    }

    @Test
    void postOhneIsbnGibt400MitFehlermeldungUndRuftDenServiceNichtAuf() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Clean Code"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").value("ISBN darf nicht leer sein"));

        verifyNoInteractions(bookService);
    }

    @Test
    void postMitUnbekannterIsbnGibt404() throws Exception {
        when(bookService.add("0000", null, null)).thenThrow(new NotFoundException("Die ISBN 0000 ist unbekannt"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "0000"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Die ISBN 0000 ist unbekannt"));
    }

    @Test
    void postMitVorhandenerIsbnGibt409() throws Exception {
        when(bookService.add("1111", "Titel", "Autor")).thenThrow(new ConflictException("existiert bereits"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "1111", "title": "Titel", "author": "Autor"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void postBeiAusfallDesIsbnDienstesGibt502() throws Exception {
        when(bookService.add("2222", null, null))
                .thenThrow(new ExternalServiceException("ISBN-Dienst ist nicht erreichbar", null));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "2222"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("ISBN-Dienst ist nicht erreichbar"));
    }

    @Test
    void getAllGibt200UndDieListeZurueck() throws Exception {
        when(bookService.getAll()).thenReturn(List.of(new Book("9780132350884", "Clean Code", "Robert C. Martin")));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("9780132350884"));
    }

    @Test
    void getMitUnbekannterIdGibt404() throws Exception {
        when(bookService.get(99L)).thenThrow(new NotFoundException("Buch 99 nicht gefunden"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }
}
