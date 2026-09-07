package ch.schule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankUtilsTest {

    @Test
    void formatBankDateFuerTagNull() {
        assertEquals("01.01.1970", BankUtils.formatBankDate(0));
    }

    @Test
    void formatBankDateNachEinemVollenJahr() {
        // 360 Banktage = genau 1 Jahr weiter
        assertEquals("01.01.1971", BankUtils.formatBankDate(360));
    }

    @Test
    void formatAmountFormatiertMillirappenAlsBetrag() {
        // 10'000 Millirappen = 0.10 Einheiten
        assertEquals(BankUtils.AMOUNT_FORMAT.format(0.10), BankUtils.formatAmount(10000).trim());
    }
}
