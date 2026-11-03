package ch.tbz.library.dto;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank(message = "ISBN darf nicht leer sein") String isbn,
        String title,
        String author) {
}
