package ch.tbz.library.controller;

import ch.tbz.library.entity.Member;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void postGibt201UndDasGespeicherteMitgliedZurueck() throws Exception {
        when(memberService.add("Anna", "anna@tbz.ch")).thenReturn(new Member("Anna", "anna@tbz.ch"));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Anna", "email": "anna@tbz.ch"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void postMitLeeremNamenUndUngueltigerEmailGibt400MitBeidenFehlern() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "email": "keine-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name darf nicht leer sein"))
                .andExpect(jsonPath("$.email").value("Email muss ein gültiges Format haben"));

        verifyNoInteractions(memberService);
    }

    @Test
    void getMitUnbekannterIdGibt404() throws Exception {
        when(memberService.get(99L)).thenThrow(new NotFoundException("Mitglied 99 nicht gefunden"));

        mockMvc.perform(get("/api/members/99"))
                .andExpect(status().isNotFound());
    }
}
