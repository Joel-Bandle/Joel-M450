package ch.tbz.m450.testing.tools.repository.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@AllArgsConstructor
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Name darf nicht leer sein")
    private final String name;

    @NotBlank(message = "Email darf nicht leer sein")
    @Email(message = "Email muss ein gueltiges Format haben")
    private final String email;

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Student() {
        this.name = "";
        this.email = "";
    }
}
