package ru.library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "person")
public class Person {
    @Id
    @Column(name  =  "id")
    @GeneratedValue(strategy  =  GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Name is required")
    @Size(max=50, message="Name must be less than 50 characters")
    @Pattern(regexp = "^[A-Za-z]+ [A-Za-z]+ [A-Za-z]+$", message = "Name must contain full name (first, middle and last name)")
    @Column(name = "full_name")
    private String name;

    @NotEmpty(message = "Age is required")
    @Min(value = 10, message = "Age must be greater than 10")
    @Column(name = "age")
    private Integer age;

    @NotEmpty(message = "Email is required")
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\\\.[a-z]{2,}$", message =
    "Email should be valid")
    private String email;

    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Phone number must contain only numbers")
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotEmpty(message = "Password is required")
    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "created_person")
    private String createdPerson;

    @Column(name = "removed_person")
    private String removedPerson;

    @OneToMany(mappedBy = "bookOwner", fetch = FetchType.LAZY)
    private List<Book> books;
}


