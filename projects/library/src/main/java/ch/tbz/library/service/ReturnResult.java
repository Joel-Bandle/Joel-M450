package ch.tbz.library.service;

import ch.tbz.library.entity.Loan;

import java.math.BigDecimal;

public record ReturnResult(Loan loan, BigDecimal fee) {
}
