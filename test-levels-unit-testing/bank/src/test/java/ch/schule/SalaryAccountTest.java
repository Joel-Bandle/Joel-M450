package ch.schule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaryAccountTest {

    private SalaryAccount account;

    @BeforeEach
    void setUp() {
        // Kreditlimite -1000: Saldo darf bis auf -1000 absinken
        account = new SalaryAccount("P-1", -1000);
    }

    @Test
    void withdrawInnerhalbDerKreditlimiteIstErlaubt() {
        assertTrue(account.withdraw(0, 500));
        assertTrue(account.withdraw(1, 500));
    }

    @Test
    void withdrawGenauBisZurKreditlimiteIstErlaubt() {
        assertTrue(account.withdraw(0, 1000));
    }

    @Test
    void withdrawUeberDieKreditlimiteHinausSchlaegtFehl() {
        assertFalse(account.withdraw(0, 1001));
    }
}
