package ru.library.exceptions.book_exp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class BookErrorResponse {
    private String message;
    private Date date;
}
