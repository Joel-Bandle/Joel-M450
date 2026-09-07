package ch.schule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    // Account ist abstrakt, SavingsAccount als konkrete Unterklasse ohne
    // Zusatzregeln fürs Testen des Basisverhaltens verwendet.
    private Account account;

    @BeforeEach
    void setUp() {
        account = new SavingsAccount("S-1");
    }

    @Test
    void neuesKontoHatSaldoNull() {
        assertEquals(0, account.getBalance());
    }

    @Test
    void depositErhoehtSaldo() {
        assertTrue(account.deposit(0, 10000));
        assertEquals(10000, account.getBalance());
    }

    @Test
    void depositMitNegativemBetragSchlaegtFehl() {
        assertFalse(account.deposit(0, -500));
        assertEquals(0, account.getBalance());
    }

    @Test
    void withdrawVerringertSaldo() {
        account.deposit(0, 10000);
        assertTrue(account.withdraw(1, 4000));
        assertEquals(6000, account.getBalance());
    }

    @Test
    void withdrawMitNegativemBetragSchlaegtFehl() {
        assertFalse(account.withdraw(0, -100));
    }

    @Test
    void ersteTransaktionIstImmerErlaubt() {
        assertTrue(account.canTransact(0));
    }

    @Test
    void transaktionMitFruehererDatumAlsLetzteBuchungSchlaegtFehl() {
        account.deposit(10, 1000);
        // Datum 5 liegt vor der letzten Buchung (Datum 10)
        assertFalse(account.canTransact(5));
        assertFalse(account.deposit(5, 1000));
    }

    @Test
    void transaktionMitGleichemDatumIstErlaubt() {
        account.deposit(10, 1000);
        assertTrue(account.canTransact(10));
    }

    @Test
    void printGibtKontoauszugAus() {
        account.deposit(0, 10000);
        account.withdraw(1, 3000);

        var out = new java.io.ByteArrayOutputStream();
        var originalOut = System.out;
        System.setOut(new java.io.PrintStream(out));
        try {
            account.print();
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString();
        assertTrue(output.contains("Kontoauszug 'S-1'"));
        assertTrue(output.contains(BankUtils.formatAmount(10000)));
    }
}
