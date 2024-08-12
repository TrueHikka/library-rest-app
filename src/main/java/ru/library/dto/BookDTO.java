package ru.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    private String title;
    private Integer yearOfProduction;
    private String author;
    private String annotation;
    private String coverImageURL;
}
