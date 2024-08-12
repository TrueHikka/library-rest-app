package ru.library.exceptions.img_exp;

public class MalformedUrlException extends RuntimeException {
    public MalformedUrlException(String message) {
        super(message);
    }
}
