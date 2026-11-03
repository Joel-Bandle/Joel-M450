package ch.tbz.library.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeeCalculatorTest {

    private final FeeCalculator calculator = new FeeCalculator();
    private final LocalDate dueDate = LocalDate.of(2025, 3, 10);

    @DisplayName("Mahngebühr nach Tagen Verspätung")
    @ParameterizedTest(name = "{0} Tage Verspätung = CHF {1}")
    @CsvSource({
            "-5, 0.00",
            "0, 0.00",
            "1, 2.00",
            "7, 2.00",
            "8, 5.00",
            "14, 5.00",
            "15, 10.00",
            "100, 10.00"
    })
    void gebuehrNachTagenVerspaetung(int tageVerspaetung, String erwartet) {
        BigDecimal fee = calculator.calculate(dueDate, dueDate.plusDays(tageVerspaetung));

        assertEquals(new BigDecimal(erwartet), fee);
    }
}
