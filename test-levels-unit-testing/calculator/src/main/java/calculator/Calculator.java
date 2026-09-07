package calculator;

public class Calculator {

    public double add(double summand1, double summand2) {
        return summand1 + summand2;
    }

    public double subtract(double minuend, double subtrahend) {
        return minuend - subtrahend;
    }

    public double multiply(double factor1, double factor2) {
        return factor1 * factor2;
    }

    public double divide(double dividend, double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division durch Null ist nicht erlaubt");
        }
        return dividend / divisor;
    }
}
