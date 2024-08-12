package ru.library.exceptions.book_exp;

public class BookNotCreatedException extends RuntimeException {
    public BookNotCreatedException(String message) {
        super(message);
    }
}
