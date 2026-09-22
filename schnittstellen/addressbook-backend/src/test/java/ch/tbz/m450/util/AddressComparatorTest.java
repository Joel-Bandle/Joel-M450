package ch.tbz.m450.util;

import ch.tbz.m450.repository.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressComparatorTest {

    private Address mueller;
    private Address muellerAnna;
    private Address adler;

    @BeforeEach
    void setUp() {
        // registrationDate bewusst unterschiedlich, für den Sortiertest nach Datum
        mueller = new Address(1, "Bruno", "Mueller", "071 111 11 11", new Date(3000));
        muellerAnna = new Address(2, "Anna", "Mueller", "071 222 22 22", new Date(2000));
        adler = new Address(3, "Zora", "Adler", "071 333 33 33", new Date(1000));
    }

    // Aufgabe 1: Standard-Sortierung (Nachname, dann Vorname)

    @Test
    void sortiertNachNachname() {
        List<Address> addresses = new ArrayList<>(List.of(mueller, adler));
        addresses.sort(new AddressComparator());

        assertEquals(List.of(adler, mueller), addresses);
    }

    @Test
    void sortiertBeiGleichemNachnameNachVorname() {
        List<Address> addresses = new ArrayList<>(List.of(mueller, muellerAnna));
        addresses.sort(new AddressComparator());

        // Anna vor Bruno, obwohl Bruno zuerst in der Liste war
        assertEquals(List.of(muellerAnna, mueller), addresses);
    }

    // Aufgabe 2: Sortierung nach frei wählbaren zusätzlichen Attributen

    @Test
    void sortiertNachRegistrationDate() {
        List<Address> addresses = new ArrayList<>(List.of(mueller, muellerAnna, adler));
        addresses.sort(new AddressComparator(AddressComparator.SortField.REGISTRATION_DATE));

        assertEquals(List.of(adler, muellerAnna, mueller), addresses);
    }

    @Test
    void sortiertNachPhonenumber() {
        List<Address> addresses = new ArrayList<>(List.of(mueller, adler, muellerAnna));
        addresses.sort(new AddressComparator(AddressComparator.SortField.PHONENUMBER));

        assertEquals(List.of(mueller, muellerAnna, adler), addresses);
    }

    @Test
    void mehrereSortierkriterienWerdenDerReiheNachAngewendet() {
        // Erst nach Vorname, bei Gleichstand nach Nachname
        List<Address> addresses = new ArrayList<>(List.of(mueller, muellerAnna, adler));
        addresses.sort(new AddressComparator(
                AddressComparator.SortField.FIRSTNAME,
                AddressComparator.SortField.LASTNAME
        ));

        assertEquals(List.of(muellerAnna, mueller, adler), addresses);
    }

    @Test
    void gleicheAdresseIstGleichGross() {
        assertTrue(new AddressComparator().compare(mueller, mueller) == 0);
    }
}
