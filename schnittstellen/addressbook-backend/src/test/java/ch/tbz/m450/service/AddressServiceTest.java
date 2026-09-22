package ch.tbz.m450.service;

import ch.tbz.m450.repository.Address;
import ch.tbz.m450.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Die H2-Datenbank wird hier komplett weggemockt: AddressRepository ist ein
 * Mockito-Mock, es findet also nie ein echter DB-Zugriff statt. So testen wir
 * ausschliesslich die Logik von AddressService, isoliert von der Persistenz.
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    private Address mueller;
    private Address adler;

    @BeforeEach
    void setUp() {
        mueller = new Address(1, "Bruno", "Mueller", "071 111 11 11", new Date());
        adler = new Address(2, "Zora", "Adler", "071 333 33 33", new Date());
    }

    // --- Stub-artig: dem Mock vorgeben, was er zurückgeben soll, und das
    //     Resultat des Service (Zustand) prüfen ---

    @Test
    void getAllGibtAdressenSortiertZurueck() {
        when(addressRepository.findAll()).thenReturn(List.of(mueller, adler));

        List<Address> result = addressService.getAll();

        // Comparator sortiert nach Nachname: Adler vor Mueller
        assertEquals(List.of(adler, mueller), result);
    }

    @Test
    void getAddressGibtGefundeneAdresseZurueck() {
        when(addressRepository.findById(1)).thenReturn(Optional.of(mueller));

        Optional<Address> result = addressService.getAddress(1);

        assertTrue(result.isPresent());
        assertEquals("Mueller", result.get().getLastname());
    }

    @Test
    void getAddressGibtLeeresOptionalZurueckWennNichtGefunden() {
        when(addressRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Address> result = addressService.getAddress(99);

        assertFalse(result.isPresent());
    }

    @Test
    void saveGibtDieGespeicherteAdresseZurueck() {
        when(addressRepository.save(mueller)).thenReturn(mueller);

        Address result = addressService.save(mueller);

        assertEquals(mueller, result);
    }

    // --- Mock-artig: verifizieren, dass der Service das Repository korrekt
    //     aufgerufen hat (Verhalten statt nur Ergebnis prüfen) ---

    @Test
    void saveRuftRepositorySaveGenauEinmalMitDerAdresseAuf() {
        when(addressRepository.save(mueller)).thenReturn(mueller);

        addressService.save(mueller);

        verify(addressRepository).save(mueller);
    }

    @Test
    void getAddressRuftRepositoryFindByIdMitDerRichtigenIdAuf() {
        when(addressRepository.findById(1)).thenReturn(Optional.of(mueller));

        addressService.getAddress(1);

        verify(addressRepository).findById(1);
    }
}
