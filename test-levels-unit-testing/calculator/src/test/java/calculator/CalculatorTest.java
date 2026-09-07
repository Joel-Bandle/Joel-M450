package calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private static final double DELTA = 0.0001;

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @ParameterizedTest
    @DisplayName("add() mit verschiedenen Zahlenpaaren")
    @CsvSource({
            "2, 3, 5",
            "-2, -3, -5",
            "-2, 5, 3",
            "0, 0, 0",
            "2.5, 0.5, 3.0"
    })
    void testAdd(double summand1, double summand2, double erwartet) {
        assertEquals(erwartet, calculator.add(summand1, summand2), DELTA);
    }

    @Test
    @DisplayName("subtract() mit positivem Ergebnis")
    void testSubtractPositive() {
        assertEquals(2, calculator.subtract(5, 3), DELTA);
    }

    @Test
    @DisplayName("subtract() mit negativem Ergebnis")
    void testSubtractNegativeResult() {
        assertEquals(-2, calculator.subtract(3, 5), DELTA);
    }

    @Test
    @DisplayName("multiply() mit zwei positiven Zahlen")
    void testMultiplyPositive() {
        assertEquals(15, calculator.multiply(3, 5), DELTA);
    }

    @Test
    @DisplayName("multiply() mit Null ergibt Null")
    void testMultiplyByZero() {
        assertEquals(0, calculator.multiply(42, 0), DELTA);
    }

    @Test
    @DisplayName("multiply() mit negativer Zahl")
    void testMultiplyNegative() {
        assertEquals(-15, calculator.multiply(3, -5), DELTA);
    }

    @Test
    @DisplayName("divide() mit glatter Division")
    void testDivide() {
        assertEquals(4, calculator.divide(8, 2), DELTA);
    }

    @Test
    @DisplayName("divide() mit Rundungsergebnis")
    void testDivideWithRemainder() {
        assertEquals(2.5, calculator.divide(5, 2), DELTA);
    }

    @Test
    @DisplayName("divide() durch Null wirft ArithmeticException")
    void testDivideByZeroThrows() {
        assertThrows(ArithmeticException.class, () -> calculator.divide(5, 0));
    }
}
