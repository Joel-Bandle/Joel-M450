package ch.tbz.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
        @NotBlank(message = "Name darf nicht leer sein") String name,
        @NotBlank(message = "Email darf nicht leer sein")
        @Email(message = "Email muss ein gültiges Format haben") String email) {
}
