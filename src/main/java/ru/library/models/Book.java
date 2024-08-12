package ru.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy  =  GenerationType.IDENTITY)
    @Column(name  =  "book_id")
    private Long bookId;

    @NotEmpty(message = "Title should not be empty")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 50 symbols")
    @Column(name  = "title")
    private String title;

    @NotEmpty(message = "Author should not be empty")
    @Size(min=2, max=50, message="Author name must be between 2 and 50 symbols")
    @Column(name   = "author")
    private String author;

    @NotNull(message = "Year should not be null")
    @Min(value = 1000, message = "Year must be at least 1000")
    @Max(value = 9999, message = "Year must be a four-digit number")
    @Column(name  = "year_of_production")
    private Integer yearOfProduction;

    @NotEmpty(message = "Description should not be empty")
    @Length(max = 65535)
    @Column(name = "annotation")
    private String annotation;

    @Column(name = "cover_image", columnDefinition = "bytea")
    private byte[] coverImage;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "created_person")
    private String createdPerson;

    @Column(name = "updated_person")
    private String updatedPerson;

    @Column(name = "removed_person")
    private String removedPerson;

    @ManyToOne
    @JoinColumn(name="person_id", referencedColumnName = "id")
    @JsonBackReference
    private Person bookOwner;
}
