package ch.tbz.library.dto;

import jakarta.validation.constraints.NotNull;

public record LoanRequest(
        @NotNull(message = "bookId fehlt") Long bookId,
        @NotNull(message = "memberId fehlt") Long memberId) {
}
