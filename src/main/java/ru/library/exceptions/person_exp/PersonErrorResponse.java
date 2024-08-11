package ru.library.exceptions.person_exp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class PersonErrorResponse {
    private String message;
    private Date date;
}
