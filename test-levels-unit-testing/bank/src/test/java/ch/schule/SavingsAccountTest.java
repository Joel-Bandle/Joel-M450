package ch.schule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsAccountTest {

    private SavingsAccount account;

    @BeforeEach
    void setUp() {
        account = new SavingsAccount("S-1");
        account.deposit(0, 10000);
    }

    @Test
    void withdrawBisZumGenauenSaldoIstErlaubt() {
        assertTrue(account.withdraw(1, 10000));
    }

    @Test
    void withdrawUeberDenSaldoHinausSchlaegtFehl() {
        assertFalse(account.withdraw(1, 10001));
    }
}
