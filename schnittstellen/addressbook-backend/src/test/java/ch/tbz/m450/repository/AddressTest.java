package ch.tbz.m450.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddressTest {

    private Address address;
    private Date registrationDate;

    @BeforeEach
    void setUp() {
        registrationDate = new Date();
        address = new Address(1, "Max", "Muster", "079 123 45 67", registrationDate);
    }

    @Test
    void konstruktorSetztAlleFelder() {
        assertEquals(1, address.getId());
        assertEquals("Max", address.getFirstname());
        assertEquals("Muster", address.getLastname());
        assertEquals("079 123 45 67", address.getPhonenumber());
        assertEquals(registrationDate, address.getRegistrationDate());
    }

    @Test
    void settersAendernDieFelder() {
        address.setFirstname("Anna");
        address.setLastname("Beispiel");

        assertEquals("Anna", address.getFirstname());
        assertEquals("Beispiel", address.getLastname());
    }

    @Test
    void noArgsKonstruktorErzeugtLeeresObjekt() {
        Address leer = new Address();
        assertEquals(0, leer.getId());
    }
}
