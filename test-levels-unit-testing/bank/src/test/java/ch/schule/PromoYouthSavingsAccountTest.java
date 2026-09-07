package ch.schule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PromoYouthSavingsAccountTest {

    private PromoYouthSavingsAccount account;

    @BeforeEach
    void setUp() {
        account = new PromoYouthSavingsAccount("Y-1");
    }

    @Test
    void depositGibtEinProzentBonus() {
        account.deposit(0, 10000);
        // 10000 + 1% Bonus (10000/100 = 100) = 10100
        assertEquals(10100, account.getBalance());
    }

    @Test
    void depositMitNegativemBetragSchlaegtWeiterhinFehl() {
        assertFalse(account.deposit(0, -100));
        assertEquals(0, account.getBalance());
    }
}
