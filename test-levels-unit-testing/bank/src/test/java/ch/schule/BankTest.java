package ch.schule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = new Bank();
    }

    @Test
    void createSavingsAccountVergibtIdMitSPrefix() {
        String id = bank.createSavingsAccount();

        assertTrue(id.startsWith("S-"));
        assertEquals(0, bank.getBalance(id));
    }

    @Test
    void createPromoYouthSavingsAccountVergibtIdMitYPrefix() {
        String id = bank.createPromoYouthSavingsAccount();

        assertTrue(id.startsWith("Y-"));
    }

    @Test
    void createSalaryAccountMitGueltigerKreditlimiteVergibtIdMitPPrefix() {
        String id = bank.createSalaryAccount(-500);

        assertNotNull(id);
        assertTrue(id.startsWith("P-"));
    }

    @Test
    void createSalaryAccountMitPositiverKreditlimiteSchlaegtFehl() {
        assertNull(bank.createSalaryAccount(500));
    }

    @Test
    void getBalanceFuerUnbekannteKontonummerIstNull() {
        assertEquals(0, bank.getBalance("nicht-vorhanden"));
    }

    @Test
    void depositUeberBankFuerUnbekannteKontonummerSchlaegtFehl() {
        assertFalse(bank.deposit("nicht-vorhanden", 0, 1000));
    }

    @Test
    void withdrawUeberBankFuerUnbekannteKontonummerSchlaegtFehl() {
        assertFalse(bank.withdraw("nicht-vorhanden", 0, 1000));
    }

    @Test
    void depositUndWithdrawUeberBankFunktionierenWieAufDemKontoDirekt() {
        String id = bank.createSavingsAccount();

        assertTrue(bank.deposit(id, 0, 10000));
        assertEquals(10000, bank.getBalance(id));

        assertTrue(bank.withdraw(id, 1, 4000));
        assertEquals(6000, bank.getBalance(id));
    }

    @Test
    void gesamtsaldoDerBankIstDieNegativeSummeAllerKonten() {
        // Achtung, vermutlicher Bug in Bank.getBalance(): die Methode
        // verwendet "balance -= aa[i].getBalance()" statt "+=", wodurch
        // hier die NEGATIVE Summe statt der Summe aller Kontostände
        // zurückkommt. Dieser Test dokumentiert das aktuelle (fehlerhafte)
        // Verhalten, siehe Notiz in AUFGABEN4.md.
        String id1 = bank.createSavingsAccount();
        String id2 = bank.createSavingsAccount();
        bank.deposit(id1, 0, 10000);
        bank.deposit(id2, 0, 5000);

        assertEquals(-15000, bank.getBalance());
    }

    @Test
    void printTop5ZeigtKontoMitHoechstemSaldoZuerst() {
        String id1 = bank.createSavingsAccount();
        String id2 = bank.createSavingsAccount();
        bank.deposit(id1, 0, 1000);
        bank.deposit(id2, 0, 5000);

        var out = new java.io.ByteArrayOutputStream();
        var originalOut = System.out;
        System.setOut(new java.io.PrintStream(out));
        try {
            bank.printTop5();
        } finally {
            System.setOut(originalOut);
        }

        String[] lines = out.toString().trim().split("\\r?\\n");
        assertTrue(lines[0].startsWith(id2), "Konto mit höherem Saldo sollte zuerst erscheinen");
    }

    @Test
    void printBottom5ZeigtKontoMitNiedrigstemSaldoZuerst() {
        String id1 = bank.createSavingsAccount();
        String id2 = bank.createSavingsAccount();
        bank.deposit(id1, 0, 1000);
        bank.deposit(id2, 0, 5000);

        var out = new java.io.ByteArrayOutputStream();
        var originalOut = System.out;
        System.setOut(new java.io.PrintStream(out));
        try {
            bank.printBottom5();
        } finally {
            System.setOut(originalOut);
        }

        String[] lines = out.toString().trim().split("\\r?\\n");
        assertTrue(lines[0].startsWith(id1), "Konto mit niedrigerem Saldo sollte zuerst erscheinen");
    }
}
