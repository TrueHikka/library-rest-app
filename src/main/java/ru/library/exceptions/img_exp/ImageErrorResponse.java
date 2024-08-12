package ru.library.exceptions.img_exp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ImageErrorResponse {
    private String message;
    private Date date;
}
