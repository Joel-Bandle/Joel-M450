package ch.tbz.library.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class FeeCalculator {

    static final BigDecimal NO_FEE = new BigDecimal("0.00");
    static final BigDecimal FIRST_WEEK_FEE = new BigDecimal("2.00");
    static final BigDecimal SECOND_WEEK_FEE = new BigDecimal("5.00");
    static final BigDecimal MAX_FEE = new BigDecimal("10.00");

    public BigDecimal calculate(LocalDate dueDate, LocalDate returnedAt) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnedAt);

        if (daysLate <= 0) {
            return NO_FEE;
        }
        if (daysLate <= 7) {
            return FIRST_WEEK_FEE;
        }
        if (daysLate <= 14) {
            return SECOND_WEEK_FEE;
        }
        return MAX_FEE;
    }
}
