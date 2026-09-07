package ch.schule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingTest {

    @Test
    void gettersGebenKonstruktorwerteZurueck() {
        Booking booking = new Booking(30, 5000);

        assertEquals(30, booking.getDate());
        assertEquals(5000, booking.getAmount());
    }

    @Test
    void printGibtFormatierteBuchungszeileAus() {
        Booking booking = new Booking(0, 10000);

        var out = new java.io.ByteArrayOutputStream();
        var originalOut = System.out;
        System.setOut(new java.io.PrintStream(out));
        try {
            booking.print(0);
        } finally {
            System.setOut(originalOut);
        }

        String expectedDate = BankUtils.formatBankDate(0);
        String expectedAmount = BankUtils.formatAmount(10000);
        assertEquals(true, out.toString().contains(expectedDate));
        assertEquals(true, out.toString().contains(expectedAmount));
    }
}
