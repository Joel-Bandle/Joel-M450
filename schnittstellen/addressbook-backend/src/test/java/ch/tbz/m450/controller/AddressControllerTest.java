package ch.tbz.m450.controller;

import ch.tbz.m450.repository.Address;
import ch.tbz.m450.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest startet nur die Web-Schicht (den Controller), der komplette
 * AddressService wird hier durch einen Mockito-Mock ersetzt. So kommt der
 * Test nie in die Nähe von AddressRepository oder der H2-Datenbank.
 */
@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    private Address mueller;

    @BeforeEach
    void setUp() {
        mueller = new Address(1, "Bruno", "Mueller", "071 111 11 11", new Date());
    }

    @Test
    void getAddressesGibt200UndListeZurueck() throws Exception {
        when(addressService.getAll()).thenReturn(List.of(mueller));

        mockMvc.perform(get("/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastname").value("Mueller"));
    }

    @Test
    void getAddressGibt200ZurueckWennGefunden() throws Exception {
        when(addressService.getAddress(1)).thenReturn(Optional.of(mueller));

        mockMvc.perform(get("/address/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Bruno"));
    }

    @Test
    void getAddressGibt404ZurueckWennNichtGefunden() throws Exception {
        when(addressService.getAddress(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/address/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAddressGibt201UndGespeicherteAdresseZurueck() throws Exception {
        when(addressService.save(org.mockito.ArgumentMatchers.any(Address.class))).thenReturn(mueller);

        mockMvc.perform(post("/address")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(mueller)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastname").value("Mueller"));
    }
}
